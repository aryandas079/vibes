package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * VibesHaptics provides super strong sensory and tactile haptic feedback
 * across touch interactions, button presses, playback controls, and scrubbers.
 */
object VibesHaptics {

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Super strong click feedback - maximum amplitude (255) for crisp, punchy tactile feel.
     */
    fun strongClick(context: Context, view: View? = null) {
        try {
            val vibrator = getVibrator(context)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(50L, 255)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50L)
                }
            }
        } catch (e: Exception) {
            // Ignored
        }

        try {
            view?.performHapticFeedback(
                HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
        } catch (e: Exception) {
            try {
                view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            } catch (ex: Exception) {
                // Ignored
            }
        }
    }

    /**
     * Tactile Play/Pause haptic feedback with double-pulse punch.
     */
    fun playPause(context: Context, view: View? = null) {
        try {
            val vibrator = getVibrator(context)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Double pulse punch: 35ms on, 25ms off, 45ms on at max amplitude
                    val timings = longArrayOf(0, 35, 25, 45)
                    val amplitudes = intArrayOf(0, 255, 0, 255)
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 40, 25, 40), -1)
                }
            }
        } catch (e: Exception) {
            strongClick(context, view)
        }
    }

    /**
     * Crisp tactile click for navigation tabs and interactive dialog buttons.
     */
    fun mediumClick(context: Context, view: View? = null) {
        try {
            val vibrator = getVibrator(context)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(30L, 230)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(30L)
                }
            }
        } catch (e: Exception) {
            // Ignored
        }

        try {
            view?.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
        } catch (e: Exception) {
            // Ignored
        }
    }

    /**
     * Ultra-responsive tick for audio scrubbers and sliders.
     */
    fun seekTick(context: Context, view: View? = null) {
        try {
            val vibrator = getVibrator(context)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(12L, 180))
                }
            }
        } catch (e: Exception) {
            // Ignored
        }
    }

    /**
     * Success feedback for favoriting or bookmarking tracks.
     */
    fun success(context: Context, view: View? = null) {
        try {
            val vibrator = getVibrator(context)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 30, 40, 55)
                    val amplitudes = intArrayOf(0, 180, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 30, 40, 55), -1)
                }
            }
        } catch (e: Exception) {
            strongClick(context, view)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view?.performHapticFeedback(
                    HapticFeedbackConstants.CONFIRM,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            }
        } catch (e: Exception) {
            // Ignored
        }
    }
}
