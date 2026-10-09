#!/usr/bin/env python3
"""
Procedurally synthesizes ZenFlow's ambient loops and bowl cues.

Everything is generated from math (noise, filters, additive synthesis), so there is no
third-party recording, sample or license involved. The output is released under CC0 1.0
(see tools/audio/AUDIO_LICENSES.md).

Usage:  python3 tools/audio/generate_audio.py            (needs numpy and ffmpeg with libvorbis)
Output: app/src/main/res/raw/*.ogg  (deterministic: fixed seeds)

Ambient loops are built with circular (FFT) filtering and wrap-around events, so the last
sample flows into the first with no click or crossfade.
"""
import subprocess
import tempfile
import wave
from pathlib import Path

import numpy as np

SR = 44100
LOOP_SECONDS = 32
OUT = Path(__file__).resolve().parents[2] / "app/src/main/res/raw"


# ---------- helpers ----------
def freqs(n):
    return np.fft.rfftfreq(n, 1 / SR)


def shape(noise, gain_fn):
    """Apply a frequency-domain gain curve to a noise buffer (circular => loops seamlessly)."""
    spec = np.fft.rfft(noise)
    spec *= gain_fn(freqs(len(noise)))
    return np.fft.irfft(spec, len(noise))


def smooth_band(f, lo, hi, width=0.35):
    """Soft band-pass in log-frequency (0..1)."""
    f = np.maximum(f, 1.0)
    x = np.log2(f)
    a, b = np.log2(lo), np.log2(hi)
    rise = 1 / (1 + np.exp(-(x - a) / width))
    fall = 1 / (1 + np.exp((x - b) / width))
    return rise * fall


def pink_gain(f):
    return 1 / np.sqrt(np.maximum(f, 20.0))


def normalize(x, peak):
    return x / (np.max(np.abs(x)) + 1e-9) * peak


def loop_env(n, cycles, phase=0.0):
    t = np.arange(n) / n
    return 0.5 * (np.sin(2 * np.pi * (cycles * t + phase)) + 1)  # integer cycles => periodic


def add_wrapped(buf, start, event):
    """Add an event at 'start', wrapping around the end of the loop."""
    n = len(buf)
    idx = (np.arange(len(event)) + start) % n
    np.add.at(buf, idx, event)


def write_ogg(name, samples):
    OUT.mkdir(parents=True, exist_ok=True)
    pcm = (np.clip(samples, -1, 1) * 32767).astype(np.int16)
    with tempfile.TemporaryDirectory() as tmp:
        wav_path = Path(tmp) / f"{name}.wav"
        with wave.open(str(wav_path), "wb") as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(SR)
            w.writeframes(pcm.tobytes())
        subprocess.run(
            ["ffmpeg", "-y", "-loglevel", "error", "-i", str(wav_path),
             "-c:a", "libvorbis", "-q:a", "3", "-ac", "1", str(OUT / f"{name}.ogg")],
            check=True,
        )
    print(f"wrote {name}.ogg ({(OUT / f'{name}.ogg').stat().st_size / 1024:.0f} KB)")


# ---------- ambient loops ----------
def white_noise():
    rng = np.random.default_rng(1)
    n = SR * LOOP_SECONDS
    x = shape(rng.standard_normal(n), lambda f: 1 / (1 + (f / 9000) ** 2))  # soften harsh top end
    return normalize(x, 0.5)


def rain():
    rng = np.random.default_rng(2)
    n = SR * LOOP_SECONDS
    bed = shape(rng.standard_normal(n), lambda f: smooth_band(f, 700, 9000, 0.5) * pink_gain(f) ** 0.4)
    bed = normalize(bed, 0.30)
    drops = np.zeros(n)
    for _ in range(int(LOOP_SECONDS * 55)):  # ~55 drops per second
        start = rng.integers(0, n)
        length = int(SR * rng.uniform(0.004, 0.012))
        env = np.exp(-np.arange(length) / (length / 4))
        tone = rng.standard_normal(length) * env
        add_wrapped(drops, start, tone * rng.uniform(0.05, 0.35))
    drops = shape(drops, lambda f: smooth_band(f, 1500, 12000, 0.5))
    rumble = normalize(shape(rng.standard_normal(n), lambda f: smooth_band(f, 60, 250, 0.4)), 0.08)
    return normalize(bed + normalize(drops, 0.30) + rumble, 0.55)


def ocean():
    rng = np.random.default_rng(3)
    n = SR * LOOP_SECONDS
    low = normalize(shape(rng.standard_normal(n), lambda f: smooth_band(f, 40, 400, 0.5)), 0.30)
    wash = normalize(shape(rng.standard_normal(n), lambda f: pink_gain(f) * smooth_band(f, 120, 2200, 0.6)), 1.0)
    hiss = normalize(shape(rng.standard_normal(n), lambda f: smooth_band(f, 2500, 9000, 0.6)), 1.0)
    wave1 = (0.15 + 0.85 * loop_env(n, 4) ** 1.6)                    # ~8 s swells
    wave2 = (0.15 + 0.85 * loop_env(n, 4, phase=0.08) ** 2.2)        # hiss peaks just after the swell
    drift = 0.8 + 0.2 * loop_env(n, 7, phase=0.3)
    mix = low * (0.5 + 0.5 * wave1) + 0.55 * wash * wave1 * drift + 0.09 * hiss * wave2
    return normalize(mix, 0.55)


def forest():
    rng = np.random.default_rng(4)
    n = SR * LOOP_SECONDS
    wind = normalize(shape(rng.standard_normal(n), lambda f: pink_gain(f) * smooth_band(f, 150, 1400, 0.6)), 0.22)
    wind *= 0.6 + 0.4 * loop_env(n, 3, phase=0.2)
    leaves = shape(rng.standard_normal(n), lambda f: smooth_band(f, 3000, 10000, 0.6))
    leaves = normalize(leaves, 0.05) * (0.3 + 0.7 * loop_env(n, 5) ** 3)
    birds = np.zeros(n)
    species = [(2600, 900), (3600, 1400), (4400, 700)]  # (base Hz, sweep Hz)
    for _ in range(11):
        base, sweep = species[rng.integers(0, len(species))]
        base *= rng.uniform(0.9, 1.1)
        start = rng.integers(0, n)
        notes = int(rng.integers(2, 5))
        gap = int(SR * rng.uniform(0.09, 0.16))
        amp = rng.uniform(0.05, 0.14)
        t_off = 0
        for k in range(notes):
            length = int(SR * rng.uniform(0.07, 0.13))
            t = np.arange(length) / SR
            direction = 1 if k % 2 == 0 else -1
            inst = base + direction * sweep * (t / t[-1])
            phase = 2 * np.pi * np.cumsum(inst) / SR
            env = np.sin(np.pi * np.arange(length) / length) ** 2
            add_wrapped(birds, start + t_off, np.sin(phase) * env * amp)
            t_off += length + gap
    return normalize(wind + leaves + birds, 0.5)


# ---------- bowl / chime cues ----------
def bowl(f0, seconds, partials, strikes=(0.0,), gain=0.8):
    n = int(SR * seconds)
    out = np.zeros(n)
    t = np.arange(n) / SR
    for strike_at in strikes:
        s0 = int(SR * strike_at)
        tt = t[: n - s0]
        voice = np.zeros_like(tt)
        for ratio, amp, decay, beat in partials:
            f = f0 * ratio
            # two slightly detuned oscillators give the characteristic shimmering beat
            osc = np.sin(2 * np.pi * f * tt) + np.sin(2 * np.pi * (f + beat) * tt)
            voice += amp * osc * np.exp(-tt / decay)
        attack = np.minimum(1, tt / 0.012)  # soft 12 ms attack, no click
        out[s0:] += voice * attack
    fade = np.minimum(1, (n - np.arange(n)) / (SR * 0.25))
    return normalize(out * fade, gain)


BOWL_PARTIALS = [(1.0, 1.0, 3.2, 0.6), (2.71, 0.55, 2.0, 0.9), (5.12, 0.25, 1.1, 1.3), (8.4, 0.10, 0.6, 1.7)]
CHIME_PARTIALS = [(1.0, 1.0, 0.9, 0.4), (2.0, 0.35, 0.6, 0.0), (3.0, 0.15, 0.35, 0.0)]


def main():
    write_ogg("ambient_white_noise", white_noise())
    write_ogg("ambient_rain", rain())
    write_ogg("ambient_ocean", ocean())
    write_ogg("ambient_forest", forest())
    write_ogg("cue_bowl_start", bowl(196.0, 7.0, BOWL_PARTIALS, gain=0.7))
    write_ogg("cue_bowl_end", bowl(174.6, 9.0, BOWL_PARTIALS, strikes=(0.0, 1.6, 3.2), gain=0.7))
    write_ogg("cue_chime_phase", bowl(659.3, 2.2, CHIME_PARTIALS, gain=0.45))


if __name__ == "__main__":
    main()
