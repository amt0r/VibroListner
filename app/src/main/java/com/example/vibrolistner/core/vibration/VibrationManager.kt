package com.example.vibrolistner.core.vibration

import android.content.Context
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.VibratorManager

/**
 * Manages device vibration using Android 16's VibratorManager API.
 * Uses VibrationAttributes.USAGE_ALARM to bypass silent mode and DND restrictions.
 */
class VibrationManager(context: Context) {

    private val vibrator = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
        .defaultVibrator

    private val alarmAttributes = VibrationAttributes.Builder()
        .setUsage(VibrationAttributes.USAGE_ALARM)
        .build()

    // Delay 0ms, Vibrate 2000ms, Pause 4000ms
    private val waveformEffect = VibrationEffect.createWaveform(
        longArrayOf(0, 2000L, 4000L),
        0 // Repeat from index 0
    )

    /**
     * Starts a continuous vibration loop using ALARM usage
     * to bypass silent mode restrictions.
     */
    fun startVibrationLoop() {
        vibrator.vibrate(waveformEffect, alarmAttributes)
    }

    /**
     * Cancels any ongoing vibration.
     */
    fun cancel() {
        vibrator.cancel()
    }
}
