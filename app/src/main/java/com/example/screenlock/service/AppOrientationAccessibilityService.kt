package com.example.screenlock.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import com.example.screenlock.model.GlobalLockMode
import com.example.screenlock.model.OrientationOption
import com.example.screenlock.util.PreferencesHelper

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

        // Only manage orientation dynamically if user selected PER_APP mode
        if (prefsHelper.globalLockMode != GlobalLockMode.PER_APP) {
            return
        }

        val pkgName = event.packageName?.toString() ?: return

        // Skip our own app or rapid duplicate window events
        if (pkgName == packageName || pkgName == lastPackageName) {
            return
        }
        lastPackageName = pkgName

        val targetOrientation = prefsHelper.getOrientationForApp(pkgName)

        // Forward orientation to OrientationOverlayService
        OrientationOverlayService.setAppOrientation(this, targetOrientation)
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
