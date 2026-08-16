package com.example.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

object HapticsHelper {

    fun performClick(context: Context? = null, view: View? = null) {
        try {
            view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            vibrate(context, 15)
        } catch (_: Exception) {}
    }

    fun performImpact(context: Context? = null) {
        try {
            vibrate(context, 35)
        } catch (_: Exception) {}
    }

    fun performSuccess(context: Context? = null) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && context != null) {
                val vibrator = getVibrator(context)
                val effect = VibrationEffect.createWaveform(longArrayOf(0, 20, 50, 30), -1)
                vibrator?.vibrate(effect)
            } else {
                vibrate(context, 50)
            }
        } catch (_: Exception) {}
    }

    private fun vibrate(context: Context?, durationMs: Long) {
        context ?: return
        try {
            val vibrator = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        durationMs,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
