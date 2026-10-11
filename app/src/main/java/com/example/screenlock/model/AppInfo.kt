package com.example.screenlock.model

import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable

enum class OrientationOption(val id: Int, val screenOrientation: Int) {
    DEFAULT(0, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED),
    PORTRAIT(1, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
    LANDSCAPE(2, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
    AUTO(3, ActivityInfo.SCREEN_ORIENTATION_SENSOR);

    companion object {
        fun fromId(id: Int): OrientationOption {
            return values().firstOrNull { it.id == id } ?: DEFAULT
        }
    }
}

enum class GlobalLockMode(val id: Int) {
    PER_APP(0),
    FORCE_PORTRAIT(1),
    FORCE_LANDSCAPE(2),
    FORCE_AUTO(3),
    OFF(4);

    companion object {
        fun fromId(id: Int): GlobalLockMode {
            return values().firstOrNull { it.id == id } ?: PER_APP
        }
    }
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    var orientation: OrientationOption = OrientationOption.DEFAULT
)
