package com.example.screenlock.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.screenlock.R
import com.example.screenlock.model.AppInfo
import com.example.screenlock.model.OrientationOption
import com.google.android.material.button.MaterialButton

class AppAdapter(
    private val onOrientationChanged: (appInfo: AppInfo, option: OrientationOption) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    private val fullList = mutableListOf<AppInfo>()
    private val displayedList = mutableListOf<AppInfo>()

    fun setData(items: List<AppInfo>) {
        fullList.clear()
        fullList.addAll(items)
        displayedList.clear()
        displayedList.addAll(items)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val trimmed = query.trim().lowercase()
        displayedList.clear()
        if (trimmed.isEmpty()) {
            displayedList.addAll(fullList)
        } else {
            displayedList.addAll(
                fullList.filter {
                    it.appName.lowercase().contains(trimmed) ||
                            it.packageName.lowercase().contains(trimmed)
                }
            )
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(displayedList[position])
    }

    override fun getItemCount(): Int = displayedList.size

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAppIcon: ImageView = itemView.findViewById(R.id.ivAppIcon)
        private val tvAppName: TextView = itemView.findViewById(R.id.tvAppName)
        private val tvPackageName: TextView = itemView.findViewById(R.id.tvPackageName)

        private val btnDefault: MaterialButton = itemView.findViewById(R.id.btnOptDefault)
        private val btnPortrait: MaterialButton = itemView.findViewById(R.id.btnOptPortrait)
        private val btnLandscape: MaterialButton = itemView.findViewById(R.id.btnOptLandscape)

        fun bind(item: AppInfo) {
            ivAppIcon.setImageDrawable(item.icon)
            tvAppName.text = item.appName
            tvPackageName.text = item.packageName

            updateButtonStates(item.orientation)

            btnDefault.setOnClickListener {
                if (item.orientation != OrientationOption.DEFAULT) {
                    item.orientation = OrientationOption.DEFAULT
                    updateButtonStates(item.orientation)
                    onOrientationChanged(item, OrientationOption.DEFAULT)
                }
            }

            btnPortrait.setOnClickListener {
                if (item.orientation != OrientationOption.PORTRAIT) {
                    item.orientation = OrientationOption.PORTRAIT
                    updateButtonStates(item.orientation)
                    onOrientationChanged(item, OrientationOption.PORTRAIT)
                }
            }

            btnLandscape.setOnClickListener {
                if (item.orientation != OrientationOption.LANDSCAPE) {
                    item.orientation = OrientationOption.LANDSCAPE
                    updateButtonStates(item.orientation)
                    onOrientationChanged(item, OrientationOption.LANDSCAPE)
                }
            }
        }

        private fun updateButtonStates(current: OrientationOption) {
            val colorSelected = Color.parseColor("#6200EE")
            val colorUnselectedText = Color.parseColor("#444444")
            val colorUnselectedStroke = Color.parseColor("#CCCCCC")

            // Default button
            if (current == OrientationOption.DEFAULT) {
                btnDefault.setBackgroundColor(colorSelected)
                btnDefault.setTextColor(Color.WHITE)
                btnDefault.strokeWidth = 0
            } else {
                btnDefault.setBackgroundColor(Color.TRANSPARENT)
                btnDefault.setTextColor(colorUnselectedText)
                btnDefault.strokeColor = ColorStateList.valueOf(colorUnselectedStroke)
                btnDefault.strokeWidth = 2
            }

            // Portrait button
            if (current == OrientationOption.PORTRAIT) {
                btnPortrait.setBackgroundColor(colorSelected)
                btnPortrait.setTextColor(Color.WHITE)
                btnPortrait.strokeWidth = 0
            } else {
                btnPortrait.setBackgroundColor(Color.TRANSPARENT)
                btnPortrait.setTextColor(colorUnselectedText)
                btnPortrait.strokeColor = ColorStateList.valueOf(colorUnselectedStroke)
                btnPortrait.strokeWidth = 2
            }

            // Landscape button
            if (current == OrientationOption.LANDSCAPE) {
                btnLandscape.setBackgroundColor(colorSelected)
                btnLandscape.setTextColor(Color.WHITE)
                btnLandscape.strokeWidth = 0
            } else {
                btnLandscape.setBackgroundColor(Color.TRANSPARENT)
                btnLandscape.setTextColor(colorUnselectedText)
                btnLandscape.strokeColor = ColorStateList.valueOf(colorUnselectedStroke)
                btnLandscape.strokeWidth = 2
            }
        }
    }
}
