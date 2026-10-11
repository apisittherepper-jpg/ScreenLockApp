package com.example.screenlock.util

import android.content.Context
import android.content.SharedPreferences
import com.example.screenlock.model.GlobalLockMode
import com.example.screenlock.model.OrientationOption

class PreferencesHelper(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_screen_lock_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GLOBAL_MODE = "global_lock_mode"
        private const val PREFIX_APP_ORIENTATION = "orientation_"
    }

    var globalLockMode: GlobalLockMode
        get() = GlobalLockMode.fromId(prefs.getInt(KEY_GLOBAL_MODE, GlobalLockMode.PER_APP.id))
        set(value) = prefs.edit().putInt(KEY_GLOBAL_MODE, value.id).apply()

    fun getOrientationForApp(packageName: String): OrientationOption {
        val id = prefs.getInt(PREFIX_APP_ORIENTATION + packageName, OrientationOption.DEFAULT.id)
        return OrientationOption.fromId(id)
    }

    fun setOrientationForApp(packageName: String, option: OrientationOption) {
        if (option == OrientationOption.DEFAULT) {
            prefs.edit().remove(PREFIX_APP_ORIENTATION + packageName).apply()
        } else {
            prefs.edit().putInt(PREFIX_APP_ORIENTATION + packageName, option.id).apply()
        }
    }
}
