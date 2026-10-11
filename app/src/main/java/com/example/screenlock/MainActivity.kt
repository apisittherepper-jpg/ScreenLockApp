package com.example.screenlock

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.screenlock.model.AppInfo
import com.example.screenlock.model.OrientationOption
import com.example.screenlock.service.AppOrientationAccessibilityService
import com.example.screenlock.ui.AppAdapter
import com.example.screenlock.util.PreferencesHelper
import com.example.screenlock.util.RotationHelper
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var adapter: AppAdapter

    private lateinit var switchMaster: SwitchMaterial
    private lateinit var cardPermSettings: MaterialCardView
    private lateinit var btnGrantSettings: Button
    private lateinit var cardPermAccessibility: MaterialCardView
    private lateinit var btnGrantAccessibility: Button

    private lateinit var btnQuickPortrait: Button
    private lateinit var btnQuickLandscape: Button
    private lateinit var btnQuickAuto: Button

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
        loadInstalledApps()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun initViews() {
        switchMaster = findViewById(R.id.switchMaster)
        cardPermSettings = findViewById(R.id.cardPermSettings)
        btnGrantSettings = findViewById(R.id.btnGrantSettings)
        cardPermAccessibility = findViewById(R.id.cardPermAccessibility)
        btnGrantAccessibility = findViewById(R.id.btnGrantAccessibility)

        btnQuickPortrait = findViewById(R.id.btnQuickPortrait)
        btnQuickLandscape = findViewById(R.id.btnQuickLandscape)
        btnQuickAuto = findViewById(R.id.btnQuickAuto)

        tvAppListCount = findViewById(R.id.tvAppListCount)
        etSearch = findViewById(R.id.etSearch)
        progressBar = findViewById(R.id.progressBar)
        rvApps = findViewById(R.id.rvApps)

        switchMaster.isChecked = prefsHelper.isMasterEnabled
    }

    private fun setupListeners() {
        switchMaster.setOnCheckedChangeListener { _, isChecked ->
            prefsHelper.isMasterEnabled = isChecked
        }

        // 1. Grant Modify System Settings
        btnGrantSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }

        // 2. Grant Accessibility Service
        btnGrantAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        // Quick Controls
        btnQuickPortrait.setOnClickListener {
            RotationHelper.applyOrientation(this, OrientationOption.PORTRAIT)
        }
        btnQuickLandscape.setOnClickListener {
            RotationHelper.applyOrientation(this, OrientationOption.LANDSCAPE)
        }
        btnQuickAuto.setOnClickListener {
            RotationHelper.applyOrientation(this, OrientationOption.AUTO)
        }

        // Search Filter
        etSearch.doAfterTextChanged { text ->
            adapter.filter(text?.toString() ?: "")
        }
    }

    private fun updatePermissionStatus() {
        val hasSettingsPerm = RotationHelper.canWriteSettings(this)
        if (hasSettingsPerm) {
            btnGrantSettings.text = getString(R.string.status_granted)
            btnGrantSettings.isEnabled = false
            cardPermSettings.strokeColor = Color.parseColor("#4CAF50")
        } else {
            btnGrantSettings.text = getString(R.string.btn_grant)
            btnGrantSettings.isEnabled = true
            cardPermSettings.strokeColor = Color.parseColor("#FFC107")
        }

        val hasAccessibilityPerm = AppOrientationAccessibilityService.isServiceEnabled(this)
        if (hasAccessibilityPerm) {
            btnGrantAccessibility.text = getString(R.string.status_granted)
            btnGrantAccessibility.isEnabled = false
            cardPermAccessibility.strokeColor = Color.parseColor("#4CAF50")
        } else {
            btnGrantAccessibility.text = getString(R.string.btn_grant)
            btnGrantAccessibility.isEnabled = true
            cardPermAccessibility.strokeColor = Color.parseColor("#FFC107")
        }
    }

    private fun setupRecyclerView() {
        adapter = AppAdapter { pkgName, option ->
            prefsHelper.setOrientationForApp(pkgName, option)
        }
        rvApps.layoutManager = LinearLayoutManager(this)
        rvApps.adapter = adapter
    }

    private fun loadInstalledApps() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val pm = packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val appList = mutableListOf<AppInfo>()
            val processedPackages = mutableSetOf<String>()

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

            // Sort by app name
            appList.sortBy { it.appName.lowercase() }

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                tvAppListCount.text = getString(R.string.app_list_title, appList.size)
                adapter.setData(appList)
            }
        }
    }
}
