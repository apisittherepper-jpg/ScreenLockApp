package com.example.screenlock

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        val btnPortrait = findViewById<Button>(R.id.btnPortrait)
        val btnLandscape = findViewById<Button>(R.id.btnLandscape)
        val btnSensorAuto = findViewById<Button>(R.id.btnSensorAuto)
        val btnReversePortrait = findViewById<Button>(R.id.btnReversePortrait)
        val btnReverseLandscape = findViewById<Button>(R.id.btnReverseLandscape)

        // 1. ล็อคแนวตั้ง
        btnPortrait.setOnClickListener {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, getString(R.string.status_portrait))
        }

        // 2. ล็อคแนวนอน
        btnLandscape.setOnClickListener {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, getString(R.string.status_landscape))
        }

        // 3. หมุนอัตโนมัติตามเซนเซอร์รอบทิศทาง
        btnSensorAuto.setOnClickListener {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR, getString(R.string.status_sensor))
        }

        // 4. แนวตั้งกลับหัว
        btnReversePortrait.setOnClickListener {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, getString(R.string.status_reverse_portrait))
        }

        // 5. แนวนอนกลับด้าน
        btnReverseLandscape.setOnClickListener {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, getString(R.string.status_reverse_landscape))
        }

        updateStatusText()
    }

    private fun setOrientation(orientation: Int, description: String) {
        requestedOrientation = orientation
        tvStatus.text = getString(R.string.current_status, description)
    }

    private fun updateStatusText() {
        val currentDesc = when (requestedOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> getString(R.string.status_portrait)
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> getString(R.string.status_landscape)
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT -> getString(R.string.status_reverse_portrait)
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> getString(R.string.status_reverse_landscape)
            ActivityInfo.SCREEN_ORIENTATION_SENSOR -> getString(R.string.status_sensor)
            else -> getString(R.string.status_unspecified)
        }
        tvStatus.text = getString(R.string.current_status, currentDesc)
    }
}
