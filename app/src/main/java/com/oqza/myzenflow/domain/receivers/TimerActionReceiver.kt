package com.oqza.myzenflow.domain.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Broadcast receiver for handling timer notification actions
 */
class TimerActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PAUSE = "com.oqza.myzenflow.ACTION_PAUSE"
        const val ACTION_RESUME = "com.oqza.myzenflow.ACTION_RESUME"
        const val ACTION_STOP = "com.oqza.myzenflow.ACTION_STOP"
        const val ACTION_START_NEXT = "com.oqza.myzenflow.ACTION_START_NEXT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PAUSE -> {
                // Send broadcast to pause timer
                val pauseIntent = Intent("com.oqza.myzenflow.TIMER_CONTROL")
                pauseIntent.putExtra("action", "pause")
                context.sendBroadcast(pauseIntent)
            }
            ACTION_RESUME -> {
                // Send broadcast to resume timer
                val resumeIntent = Intent("com.oqza.myzenflow.TIMER_CONTROL")
                resumeIntent.putExtra("action", "resume")
                context.sendBroadcast(resumeIntent)
            }
            ACTION_STOP -> {
                // Send broadcast to stop timer
                val stopIntent = Intent("com.oqza.myzenflow.TIMER_CONTROL")
                stopIntent.putExtra("action", "stop")
                context.sendBroadcast(stopIntent)
            }
            ACTION_START_NEXT -> {
                // Send broadcast to start next session
                val startIntent = Intent("com.oqza.myzenflow.TIMER_CONTROL")
                startIntent.putExtra("action", "start_next")
                context.sendBroadcast(startIntent)
            }
        }
    }
}
