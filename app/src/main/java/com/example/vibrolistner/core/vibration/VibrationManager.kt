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

    private val oneShotEffect = VibrationEffect.createOneShot(
        2000L, // 1 second
        VibrationEffect.DEFAULT_AMPLITUDE,
    )

    /**
     * Triggers a single 1-second vibration pulse using ALARM usage
     * to bypass silent mode restrictions.
     */
    fun vibrateOnce() {
        vibrator.vibrate(oneShotEffect, alarmAttributes)
    }

    /**
     * Cancels any ongoing vibration.
     */
    fun cancel() {
        vibrator.cancel()
    }
}
