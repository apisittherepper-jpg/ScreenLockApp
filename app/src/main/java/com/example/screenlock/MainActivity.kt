package com.example.screenlock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.screenlock.model.AppInfo
import com.example.screenlock.model.GlobalLockMode
import com.example.screenlock.model.OrientationOption
import com.example.screenlock.service.AppOrientationAccessibilityService
import com.example.screenlock.service.OrientationOverlayService
import com.example.screenlock.ui.AppAdapter
import com.example.screenlock.util.PreferencesHelper
import com.example.screenlock.util.RotationHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var adapter: AppAdapter

    // Permission views
    private lateinit var cardPermOverlay: MaterialCardView
    private lateinit var btnGrantOverlay: Button
    private lateinit var cardPermSettings: MaterialCardView
    private lateinit var btnGrantSettings: Button
    private lateinit var cardPermAccessibility: MaterialCardView
    private lateinit var btnGrantAccessibility: Button

    // Master Mode buttons
    private lateinit var btnModePerApp: MaterialButton
    private lateinit var btnModeForcePortrait: MaterialButton
    private lateinit var btnModeForceLandscape: MaterialButton
    private lateinit var btnModeForceAuto: MaterialButton
    private lateinit var btnModeStop: MaterialButton

    // App List views
    private lateinit var tvAppListCount: TextView
    private lateinit var etSearch: TextInputEditText
    private lateinit var progressBar: ProgressBar
    private lateinit var rvApps: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefsHelper = PreferencesHelper(this)

        initViews()
        setupListeners()
        setupRecyclerView()
        checkNotificationPermission()
        loadInstalledApps()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        updateModeButtonHighlights()
    }

    private fun initViews() {
        cardPermOverlay = findViewById(R.id.cardPermOverlay)
        btnGrantOverlay = findViewById(R.id.btnGrantOverlay)
        cardPermSettings = findViewById(R.id.cardPermSettings)
        btnGrantSettings = findViewById(R.id.btnGrantSettings)
        cardPermAccessibility = findViewById(R.id.cardPermAccessibility)
        btnGrantAccessibility = findViewById(R.id.btnGrantAccessibility)

        btnModePerApp = findViewById(R.id.btnModePerApp)
        btnModeForcePortrait = findViewById(R.id.btnModeForcePortrait)
        btnModeForceLandscape = findViewById(R.id.btnModeForceLandscape)
        btnModeForceAuto = findViewById(R.id.btnModeForceAuto)
        btnModeStop = findViewById(R.id.btnModeStop)

        tvAppListCount = findViewById(R.id.tvAppListCount)
        etSearch = findViewById(R.id.etSearch)
        progressBar = findViewById(R.id.progressBar)
        rvApps = findViewById(R.id.rvApps)
    }

    private fun setupListeners() {
        // 1. Grant Overlay Permission
        btnGrantOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

        // 2. Grant Write Settings Permission
        btnGrantSettings.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

        // 3. Grant Accessibility Service Permission
        btnGrantAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        // Mode: Per-App Lock
        btnModePerApp.setOnClickListener {
            if (!ensureOverlayPermission()) return@setOnClickListener
            prefsHelper.globalLockMode = GlobalLockMode.PER_APP
            OrientationOverlayService.start(this)
            updateModeButtonHighlights()
            Toast.makeText(this, "เปิดโหมด: ล็อคตามแอพที่กำหนดแล้ว", Toast.LENGTH_SHORT).show()
        }

        // Mode: Permanent Force Portrait
        btnModeForcePortrait.setOnClickListener {
            if (!ensureOverlayPermission()) return@setOnClickListener
            prefsHelper.globalLockMode = GlobalLockMode.FORCE_PORTRAIT
            val intent = Intent(this, OrientationOverlayService::class.java).apply {
                action = OrientationOverlayService.ACTION_PORTRAIT
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            updateModeButtonHighlights()
            Toast.makeText(this, "ล็อคแนวตั้งถาวร (ทุกแอพ) แล้ว", Toast.LENGTH_SHORT).show()
        }

        // Mode: Permanent Force Landscape
        btnModeForceLandscape.setOnClickListener {
            if (!ensureOverlayPermission()) return@setOnClickListener
            prefsHelper.globalLockMode = GlobalLockMode.FORCE_LANDSCAPE
            val intent = Intent(this, OrientationOverlayService::class.java).apply {
                action = OrientationOverlayService.ACTION_LANDSCAPE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            updateModeButtonHighlights()
            Toast.makeText(this, "ล็อคแนวนอนถาวร (ทุกแอพ) แล้ว", Toast.LENGTH_SHORT).show()
        }

        // Mode: Force Auto Rotate
        btnModeForceAuto.setOnClickListener {
            if (!ensureOverlayPermission()) return@setOnClickListener
            prefsHelper.globalLockMode = GlobalLockMode.FORCE_AUTO
            val intent = Intent(this, OrientationOverlayService::class.java).apply {
                action = OrientationOverlayService.ACTION_AUTO
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            updateModeButtonHighlights()
            Toast.makeText(this, "เปิดโหมดหมุนตามเซนเซอร์อัตโนมัติแล้ว", Toast.LENGTH_SHORT).show()
        }

        // Mode: Stop Service
        btnModeStop.setOnClickListener {
            prefsHelper.globalLockMode = GlobalLockMode.OFF
            OrientationOverlayService.stop(this)
            updateModeButtonHighlights()
            Toast.makeText(this, "ปิดระบบล็อคหน้าจอแล้ว", Toast.LENGTH_SHORT).show()
        }

        // Search Filter
        etSearch.doAfterTextChanged { text ->
            adapter.filter(text?.toString() ?: "")
        }
    }

    private fun ensureOverlayPermission(): Boolean {
        if (!RotationHelper.canDrawOverlays(this)) {
            Toast.makeText(this, getString(R.string.toast_perm_required), Toast.LENGTH_LONG).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }
            return false
        }
        return true
    }

    private fun updatePermissionStatus() {
        val hasOverlay = RotationHelper.canDrawOverlays(this)
        btnGrantOverlay.text = if (hasOverlay) getString(R.string.status_granted) else getString(R.string.btn_grant)
        btnGrantOverlay.isEnabled = !hasOverlay
        cardPermOverlay.strokeColor = if (hasOverlay) Color.parseColor("#4CAF50") else Color.parseColor("#FFC107")

        val hasSettings = RotationHelper.canWriteSettings(this)
        btnGrantSettings.text = if (hasSettings) getString(R.string.status_granted) else getString(R.string.btn_grant)
        btnGrantSettings.isEnabled = !hasSettings
        cardPermSettings.strokeColor = if (hasSettings) Color.parseColor("#4CAF50") else Color.parseColor("#FFC107")

        val hasAccessibility = AppOrientationAccessibilityService.isServiceEnabled(this)
        btnGrantAccessibility.text = if (hasAccessibility) getString(R.string.status_granted) else getString(R.string.btn_grant)
        btnGrantAccessibility.isEnabled = !hasAccessibility
        cardPermAccessibility.strokeColor = if (hasAccessibility) Color.parseColor("#4CAF50") else Color.parseColor("#FFC107")
    }

    private fun updateModeButtonHighlights() {
        val mode = prefsHelper.globalLockMode
        val primaryColor = Color.parseColor("#6200EE")

        setButtonHighlight(btnModePerApp, mode == GlobalLockMode.PER_APP, primaryColor)
        setButtonHighlight(btnModeForcePortrait, mode == GlobalLockMode.FORCE_PORTRAIT, primaryColor)
        setButtonHighlight(btnModeForceLandscape, mode == GlobalLockMode.FORCE_LANDSCAPE, primaryColor)
        setButtonHighlight(btnModeForceAuto, mode == GlobalLockMode.FORCE_AUTO, primaryColor)
    }

    private fun setButtonHighlight(btn: MaterialButton, isSelected: Boolean, activeColor: Int) {
        if (isSelected) {
            btn.setBackgroundColor(activeColor)
            btn.setTextColor(Color.WHITE)
            btn.strokeWidth = 0
        } else {
            btn.setBackgroundColor(Color.TRANSPARENT)
            btn.setTextColor(Color.parseColor("#333333"))
            btn.strokeColor = ColorStateList.valueOf(Color.parseColor("#BBBBBB"))
            btn.strokeWidth = 2
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AppAdapter { appInfo, option ->
            prefsHelper.setOrientationForApp(appInfo.packageName, option)

            val optionName = when (option) {
                OrientationOption.PORTRAIT -> getString(R.string.opt_portrait)
                OrientationOption.LANDSCAPE -> getString(R.string.opt_landscape)
                else -> getString(R.string.opt_default)
            }
            Toast.makeText(
                this,
                getString(R.string.toast_saved, appInfo.appName, optionName),
                Toast.LENGTH_SHORT
            ).show()

            // If we are currently in PER_APP mode, make sure the service is running
            if (prefsHelper.globalLockMode == GlobalLockMode.PER_APP && RotationHelper.canDrawOverlays(this)) {
                OrientationOverlayService.start(this)
            }
        }
        rvApps.layoutManager = LinearLayoutManager(this)
        rvApps.adapter = adapter
    }

    private fun loadInstalledApps() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val pm = packageManager
            val appList = mutableListOf<AppInfo>()
            val processedPackages = mutableSetOf<String>()

            // 1. Primary method: Query launcher activities
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PackageManager.MATCH_ALL
            } else {
                0
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, flags)

            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (pkg == packageName || processedPackages.contains(pkg)) {
                    continue
                }
                processedPackages.add(pkg)

                val appName = info.loadLabel(pm).toString()
                val icon = info.loadIcon(pm)
                val orientation = prefsHelper.getOrientationForApp(pkg)

                appList.add(
                    AppInfo(
                        packageName = pkg,
                        appName = appName,
                        icon = icon,
                        orientation = orientation
                    )
                )
            }

            // 2. Fallback method: If resolveInfos missed apps, query installed packages
            if (appList.isEmpty()) {
                val installedPackages = pm.getInstalledPackages(0)
                for (pkgInfo in installedPackages) {
                    val pkg = pkgInfo.packageName
                    if (pkg == packageName || processedPackages.contains(pkg)) {
                        continue
                    }
                    val launchIntent = pm.getLaunchIntentForPackage(pkg)
                    if (launchIntent != null) {
                        processedPackages.add(pkg)
                        val appName = pkgInfo.applicationInfo.loadLabel(pm).toString()
                        val icon = pkgInfo.applicationInfo.loadIcon(pm)
                        val orientation = prefsHelper.getOrientationForApp(pkg)
                        appList.add(
                            AppInfo(
                                packageName = pkg,
                                appName = appName,
                                icon = icon,
                                orientation = orientation
                            )
                        )
                    }
                }
            }

            // Sort alphabetically by app name
            appList.sortBy { it.appName.lowercase() }

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                tvAppListCount.text = getString(R.string.app_list_title, appList.size)
                adapter.setData(appList)
            }
        }
    }
}
