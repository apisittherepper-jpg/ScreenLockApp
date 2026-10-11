package com.example.screenlock.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.screenlock.R
import com.example.screenlock.model.AppInfo
import com.example.screenlock.model.OrientationOption
import com.google.android.material.button.MaterialButtonToggleGroup

class AppAdapter(
    private val onOrientationChanged: (packageName: String, option: OrientationOption) -> Unit
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
        private val toggleGroup: MaterialButtonToggleGroup =
            itemView.findViewById(R.id.toggleGroupOrientation)

        fun bind(item: AppInfo) {
            ivAppIcon.setImageDrawable(item.icon)
            tvAppName.text = item.appName
            tvPackageName.text = item.packageName

            toggleGroup.clearOnButtonCheckedListeners()

            when (item.orientation) {
                OrientationOption.PORTRAIT -> toggleGroup.check(R.id.btnOptPortrait)
                OrientationOption.LANDSCAPE -> toggleGroup.check(R.id.btnOptLandscape)
                else -> toggleGroup.check(R.id.btnOptDefault)
            }

            toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    val newOption = when (checkedId) {
                        R.id.btnOptPortrait -> OrientationOption.PORTRAIT
                        R.id.btnOptLandscape -> OrientationOption.LANDSCAPE
                        else -> OrientationOption.DEFAULT
                    }
                    item.orientation = newOption
                    onOrientationChanged(item.packageName, newOption)
                }
            }
        }
    }
}
