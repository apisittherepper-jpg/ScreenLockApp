package com.example.screenlock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.screenlock.MainActivity
import com.example.screenlock.R
import com.example.screenlock.model.GlobalLockMode
import com.example.screenlock.model.OrientationOption
import com.example.screenlock.util.PreferencesHelper
import com.example.screenlock.util.RotationHelper

class OrientationOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var prefsHelper: PreferencesHelper

    private var currentOption: OrientationOption = OrientationOption.DEFAULT

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefsHelper = PreferencesHelper(this)

        createNotificationChannel()
        setupOverlayView()
        startForeground(NOTIFICATION_ID, buildNotification(getString(R.string.opt_default)))
    }

    private fun setupOverlayView() {
        if (!RotationHelper.canDrawOverlays(this)) {
            return
        }

        overlayView = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        layoutParams = WindowManager.LayoutParams(
            0,
            0,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            screenOrientation = OrientationOption.DEFAULT.screenOrientation
        }

        try {
            windowManager.addView(overlayView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_PORTRAIT -> {
                prefsHelper.globalLockMode = GlobalLockMode.FORCE_PORTRAIT
                applyOrientation(OrientationOption.PORTRAIT)
            }
            ACTION_LANDSCAPE -> {
                prefsHelper.globalLockMode = GlobalLockMode.FORCE_LANDSCAPE
                applyOrientation(OrientationOption.LANDSCAPE)
            }
            ACTION_AUTO -> {
                prefsHelper.globalLockMode = GlobalLockMode.FORCE_AUTO
                applyOrientation(OrientationOption.AUTO)
            }
            ACTION_SET_APP_ORIENTATION -> {
                val optId = intent.getIntExtra(EXTRA_ORIENTATION_ID, OrientationOption.DEFAULT.id)
                val opt = OrientationOption.fromId(optId)
                applyOrientation(opt)
            }
            ACTION_STOP -> {
                prefsHelper.globalLockMode = GlobalLockMode.OFF
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // Apply based on current global mode
                when (prefsHelper.globalLockMode) {
                    GlobalLockMode.FORCE_PORTRAIT -> applyOrientation(OrientationOption.PORTRAIT)
                    GlobalLockMode.FORCE_LANDSCAPE -> applyOrientation(OrientationOption.LANDSCAPE)
                    GlobalLockMode.FORCE_AUTO -> applyOrientation(OrientationOption.AUTO)
                    GlobalLockMode.PER_APP -> applyOrientation(OrientationOption.DEFAULT)
                    GlobalLockMode.OFF -> {
                        stopSelf()
                        return START_NOT_STICKY
                    }
                }
            }
        }

        return START_STICKY
    }

    private fun applyOrientation(option: OrientationOption) {
        currentOption = option

        // 1. Force screen orientation via WindowManager Overlay
        if (overlayView != null) {
            layoutParams.screenOrientation = option.screenOrientation
            try {
                windowManager.updateViewLayout(overlayView, layoutParams)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (RotationHelper.canDrawOverlays(this)) {
            setupOverlayView()
            if (overlayView != null) {
                layoutParams.screenOrientation = option.screenOrientation
                try {
                    windowManager.updateViewLayout(overlayView, layoutParams)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 2. Secondary reinforcement via System Settings
        RotationHelper.applySystemRotation(this, option)

        // 3. Update persistent notification
        val label = when (option) {
            OrientationOption.PORTRAIT -> getString(R.string.opt_portrait)
            OrientationOption.LANDSCAPE -> getString(R.string.opt_landscape)
            OrientationOption.AUTO -> getString(R.string.mode_force_auto)
            OrientationOption.DEFAULT -> getString(R.string.opt_default)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(label))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Screen lock controls notification"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pPortrait = createServicePendingIntent(ACTION_PORTRAIT, 1)
        val pLandscape = createServicePendingIntent(ACTION_LANDSCAPE, 2)
        val pAuto = createServicePendingIntent(ACTION_AUTO, 3)
        val pStop = createServicePendingIntent(ACTION_STOP, 4)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_rotate)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_content, statusText))
            .setContentIntent(pOpenApp)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, getString(R.string.notif_action_portrait), pPortrait)
            .addAction(0, getString(R.string.notif_action_landscape), pLandscape)
            .addAction(0, getString(R.string.notif_action_auto), pAuto)
            .addAction(0, getString(R.string.notif_action_stop), pStop)
            .build()
    }

    private fun createServicePendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, OrientationOverlayService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayView = null
        // Restore system auto rotation
        RotationHelper.applySystemRotation(this, OrientationOption.AUTO)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "channel_screen_lock"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PORTRAIT = "com.example.screenlock.ACTION_PORTRAIT"
        const val ACTION_LANDSCAPE = "com.example.screenlock.ACTION_LANDSCAPE"
        const val ACTION_AUTO = "com.example.screenlock.ACTION_AUTO"
        const val ACTION_SET_APP_ORIENTATION = "com.example.screenlock.ACTION_SET_APP"
        const val ACTION_STOP = "com.example.screenlock.ACTION_STOP"
        const val EXTRA_ORIENTATION_ID = "extra_orientation_id"

        fun start(context: Context) {
            val intent = Intent(context, OrientationOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun setAppOrientation(context: Context, option: OrientationOption) {
            val intent = Intent(context, OrientationOverlayService::class.java).apply {
                action = ACTION_SET_APP_ORIENTATION
                putExtra(EXTRA_ORIENTATION_ID, option.id)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, OrientationOverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
