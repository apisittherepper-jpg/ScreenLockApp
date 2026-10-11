package com.example.screenlock.util

import android.content.Context
import android.provider.Settings
import android.view.Surface
import com.example.screenlock.model.OrientationOption

object RotationHelper {

    fun canWriteSettings(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    /**
     * Lock screen orientation permanently or restore auto-rotation.
     */
    fun applyOrientation(context: Context, option: OrientationOption) {
        if (!canWriteSettings(context)) return

        val cr = context.contentResolver
        try {
            when (option) {
                OrientationOption.PORTRAIT -> {
                    // Turn off auto-rotate and lock to portrait (ROTATION_0)
                    Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 0)
                    Settings.System.putInt(cr, Settings.System.USER_ROTATION, Surface.ROTATION_0)
                }
                OrientationOption.LANDSCAPE -> {
                    // Turn off auto-rotate and lock to landscape (ROTATION_90)
                    Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 0)
                    Settings.System.putInt(cr, Settings.System.USER_ROTATION, Surface.ROTATION_90)
                }
                OrientationOption.AUTO -> {
                    // Turn on auto-rotate
                    Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 1)
                }
                OrientationOption.DEFAULT -> {
                    // Revert to auto-rotate
                    Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
