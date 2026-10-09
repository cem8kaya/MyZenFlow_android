package com.oqza.myzenflow.domain.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import androidx.annotation.RawRes
import com.oqza.myzenflow.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * Ambient soundscapes and bowl cues for breathing sessions.
 *
 * - Seamless looping ambience from res/raw with fade in/out
 * - Audio focus: pauses for calls/other media, ducks for short notifications
 * - Pauses when headphones are unplugged
 * - Optional sleep timer that fades the ambience out and stops it
 * - Short bowl/chime cues through SoundPool (start, breath phase, end)
 */
@Singleton
class BreathingAudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var fadeJob: Job? = null
    private var sleepJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private var noisyReceiverRegistered = false

    /** Volume the user asked for (0..1); fades and ducking are relative to this. */
    private var targetVolume = 0.5f
    private var currentVolume = 0f
    private var pausedByFocusLoss = false

    private val _sleepTimerRemainingMs = MutableStateFlow<Long?>(null)
    /** Remaining sleep-timer time, or null when no timer is running. */
    val sleepTimerRemainingMs: StateFlow<Long?> = _sleepTimerRemainingMs.asStateFlow()

    /**
     * Ambient sound types. [resId] is 0 for NONE.
     */
    enum class AmbientSound(@RawRes val resId: Int) {
        NONE(0),
        OCEAN_WAVES(R.raw.ambient_ocean),
        RAIN(R.raw.ambient_rain),
        FOREST(R.raw.ambient_forest),
        WHITE_NOISE(R.raw.ambient_white_noise)
    }

    /** Short sound cues. */
    enum class Cue(@RawRes val resId: Int, val volumeScale: Float) {
        START(R.raw.cue_bowl_start, 1f),
        PHASE(R.raw.cue_chime_phase, 0.6f),
        END(R.raw.cue_bowl_end, 1f)
    }

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val soundPool: SoundPool by lazy {
        SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()
    }
    private val cueIds = mutableMapOf<Cue, Int>()
    private val loadedCues = mutableSetOf<Int>()

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pausedByFocusLoss = false
                stopAmbientSound()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (mediaPlayer?.isPlaying == true) {
                    pausedByFocusLoss = true
                    mediaPlayer?.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> applyVolume(currentVolume * DUCK_FACTOR)
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (pausedByFocusLoss) {
                    pausedByFocusLoss = false
                    mediaPlayer?.start()
                }
                applyVolume(currentVolume)
            }
        }
    }

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                mediaPlayer?.pause()
            }
        }
    }

    /**
     * Play ambient sound with fade in.
     */
    fun playAmbientSound(sound: AmbientSound, volume: Float = 0.5f) {
        if (sound == AmbientSound.NONE || sound.resId == 0) {
            stopAmbientSound()
            return
        }

        try {
            releasePlayer()
            targetVolume = volume.coerceIn(0f, 1f)
            if (!requestFocus()) return

            val player = MediaPlayer()
            context.resources.openRawResourceFd(sound.resId).use { afd ->
                player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
            player.setAudioAttributes(audioAttributes)
            player.isLooping = true
            player.prepare()
            player.setVolume(0f, 0f)
            currentVolume = 0f
            player.start()
            mediaPlayer = player
            registerNoisyReceiver()
            fadeTo(targetVolume, FADE_IN_MS)
        } catch (e: Exception) {
            // Missing resource or player error: stay silent rather than crash the session
            releasePlayer()
            abandonFocus()
        }
    }

    /**
     * Stop ambient sound with fade out.
     */
    fun stopAmbientSound() {
        cancelSleepTimer()
        val player = mediaPlayer ?: return
        val fromVolume = currentVolume
        mediaPlayer = null
        fadeJob?.cancel()
        unregisterNoisyReceiver()
        scope.launch {
            fadeVolume(player, fromVolume, 0f, FADE_OUT_MS)
            runCatching { player.stop() }
            player.release()
            // A new sound may have started while this one faded out; keep its focus
            if (mediaPlayer == null) abandonFocus()
        }
    }

    /**
     * Pause ambient sound with fade out.
     */
    fun pauseAmbientSound() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            fadeJob?.cancel()
            val fromVolume = currentVolume
            fadeJob = scope.launch {
                fadeVolume(player, fromVolume, 0f, FADE_OUT_MS)
                if (isActive) runCatching { player.pause() }
            }
        }
    }

    /**
     * Resume ambient sound with fade in.
     */
    fun resumeAmbientSound(volume: Float = 0.5f) {
        val player = mediaPlayer ?: return
        targetVolume = volume.coerceIn(0f, 1f)
        if (!player.isPlaying) {
            player.setVolume(0f, 0f)
            currentVolume = 0f
            runCatching { player.start() }
            fadeTo(targetVolume, FADE_IN_MS)
        }
    }

    /**
     * Set volume (0..1).
     */
    fun setVolume(volume: Float) {
        targetVolume = volume.coerceIn(0f, 1f)
        fadeJob?.cancel()
        currentVolume = targetVolume
        applyVolume(targetVolume)
    }

    /**
     * Fade out and stop the ambience after [minutes]. Pass 0 to cancel.
     */
    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        if (minutes <= 0) return
        val totalMs = minutes * 60_000L
        sleepJob = scope.launch {
            var remaining = totalMs
            while (remaining > 0 && isActive) {
                _sleepTimerRemainingMs.value = remaining
                delay(SLEEP_TICK_MS)
                remaining -= SLEEP_TICK_MS
            }
            _sleepTimerRemainingMs.value = null
            // Longer fade for sleep: 8 seconds
            val player = mediaPlayer
            if (player != null) {
                val fromVolume = currentVolume
                mediaPlayer = null
                unregisterNoisyReceiver()
                fadeVolume(player, fromVolume, 0f, 8_000L)
                runCatching { player.stop() }
                player.release()
                if (mediaPlayer == null) abandonFocus()
            }
        }
    }

    fun cancelSleepTimer() {
        sleepJob?.cancel()
        sleepJob = null
        _sleepTimerRemainingMs.value = null
    }

    /**
     * Play a short cue (bowl or chime). [volume] is the user's master volume (0..1).
     */
    fun playCue(cue: Cue, volume: Float) {
        try {
            val id = cueIds.getOrPut(cue) {
                soundPool.setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) loadedCues.add(sampleId)
                }
                soundPool.load(context, cue.resId, 1)
            }
            if (id in loadedCues) {
                val v = (volume * cue.volumeScale).coerceIn(0f, 1f)
                soundPool.play(id, v, v, 1, 0, 1f)
            }
        } catch (_: Exception) {
            // Cues are decoration; never break the session over them
        }
    }

    /**
     * Preload cues so the first one plays on time.
     */
    fun preloadCues() {
        Cue.entries.forEach { cue ->
            runCatching {
                cueIds.getOrPut(cue) {
                    soundPool.setOnLoadCompleteListener { _, sampleId, status ->
                        if (status == 0) loadedCues.add(sampleId)
                    }
                    soundPool.load(context, cue.resId, 1)
                }
            }
        }
    }

    /**
     * Check if audio is playing.
     */
    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    /**
     * Release resources.
     */
    fun release() {
        cancelSleepTimer()
        fadeJob?.cancel()
        releasePlayer()
        abandonFocus()
        if (cueIds.isNotEmpty()) {
            soundPool.release()
            cueIds.clear()
            loadedCues.clear()
        }
    }

    // ---- internals ----

    private fun applyVolume(volume: Float) {
        val v = max(0f, min(1f, volume))
        mediaPlayer?.setVolume(v, v)
    }

    /** Fade the current player to [target], cancelling any running fade. */
    private fun fadeTo(target: Float, durationMs: Long) {
        val player = mediaPlayer ?: return
        val from = currentVolume
        fadeJob?.cancel()
        fadeJob = scope.launch { fadeVolume(player, from, target, durationMs) }
    }

    /**
     * Linear volume ramp on a specific player. Only the active player updates [currentVolume],
     * so a sound that is fading out cannot disturb the one that replaced it.
     */
    private suspend fun fadeVolume(player: MediaPlayer, from: Float, target: Float, durationMs: Long) {
        val steps = 20
        val stepMs = durationMs / steps
        repeat(steps) { i ->
            val v = max(0f, from + (target - from) * ((i + 1) / steps.toFloat()))
            if (player === mediaPlayer) currentVolume = v
            runCatching { player.setVolume(v, v) }
            delay(stepMs)
        }
        if (player === mediaPlayer) currentVolume = target
    }

    private fun releasePlayer() {
        fadeJob?.cancel()
        unregisterNoisyReceiver()
        mediaPlayer?.let {
            runCatching { it.stop() }
            it.release()
        }
        mediaPlayer = null
        currentVolume = 0f
    }

    private fun requestFocus(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(focusListener)
                .build()
            focusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
            )
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusListener)
        }
    }

    private fun registerNoisyReceiver() {
        if (!noisyReceiverRegistered) {
            context.registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            noisyReceiverRegistered = true
        }
    }

    private fun unregisterNoisyReceiver() {
        if (noisyReceiverRegistered) {
            runCatching { context.unregisterReceiver(noisyReceiver) }
            noisyReceiverRegistered = false
        }
    }

    private companion object {
        const val FADE_IN_MS = 2_000L
        const val FADE_OUT_MS = 1_000L
        const val SLEEP_TICK_MS = 1_000L
        const val DUCK_FACTOR = 0.3f
    }
}
