package com.example.screenlock.model

import android.graphics.drawable.Drawable

enum class OrientationOption(val id: Int) {
    DEFAULT(0),
    PORTRAIT(1),
    LANDSCAPE(2),
    AUTO(3);

    companion object {
        fun fromId(id: Int): OrientationOption {
            return values().firstOrNull { it.id == id } ?: DEFAULT
        }
    }
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    var orientation: OrientationOption = OrientationOption.DEFAULT
)
