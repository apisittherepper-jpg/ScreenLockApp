package com.example.screenlock.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import com.example.screenlock.model.OrientationOption
import com.example.screenlock.util.PreferencesHelper
import com.example.screenlock.util.RotationHelper

class AppOrientationAccessibilityService : AccessibilityService() {

    private lateinit var prefsHelper: PreferencesHelper
    private var lastPackageName: String? = null

    override fun onCreate() {
        super.onCreate()
        prefsHelper = PreferencesHelper(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        if (!prefsHelper.isMasterEnabled) {
            return
        }

        val pkgName = event.packageName?.toString() ?: return

        // Skip internal/system input methods or rapid duplicates
        if (pkgName == lastPackageName || pkgName == packageName) {
            return
        }
        lastPackageName = pkgName

        val targetOrientation = prefsHelper.getOrientationForApp(pkgName)

        when (targetOrientation) {
            OrientationOption.PORTRAIT -> {
                RotationHelper.applyOrientation(this, OrientationOption.PORTRAIT)
            }
            OrientationOption.LANDSCAPE -> {
                RotationHelper.applyOrientation(this, OrientationOption.LANDSCAPE)
            }
            OrientationOption.AUTO -> {
                RotationHelper.applyOrientation(this, OrientationOption.AUTO)
            }
            OrientationOption.DEFAULT -> {
                // If the app doesn't have a specific lock, don't force or let system handle it
            }
        }
    }

    override fun onInterrupt() {
        // Required callback
    }

    companion object {
        fun isServiceEnabled(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${AppOrientationAccessibilityService::class.java.canonicalName}"
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedServiceName, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }
}
