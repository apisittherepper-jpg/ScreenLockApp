package com.example.screenlock.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.Surface
import com.example.screenlock.model.OrientationOption

object RotationHelper {

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun canWriteSettings(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }
    }

    /**
     * Change system-level rotation settings as a secondary reinforcement.
     */
    fun applySystemRotation(context: Context, option: OrientationOption) {
        if (!canWriteSettings(context)) return

        val cr = context.contentResolver
        try {
            when (option) {
                OrientationOption.PORTRAIT -> {
                    Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 0)
                    Settings.System.putInt(cr, Settings.System.USER_ROTATION, Surface.ROTATION_0)
                }
                OrientationOption.LANDSCAPE -> {
                    Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 0)
                    Settings.System.putInt(cr, Settings.System.USER_ROTATION, Surface.ROTATION_90)
                }
                OrientationOption.AUTO -> {
                    Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 1)
                }
                OrientationOption.DEFAULT -> {
                    Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
