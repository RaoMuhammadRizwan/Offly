package com.example.offly.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.offly.databinding.ItemHomeAppDetailBinding
import com.example.offly.dataclass.AppUsage
import kotlin.math.roundToInt

class SocialUsageAdapter(
    private val context : Context,
    private var items : List<AppUsage>
) : RecyclerView.Adapter<SocialUsageAdapter.ViewHolder>() {
    private var totalUsageMs : Long = 0

    fun setData(newItems : List<AppUsage>) {
        items = newItems
        totalUsageMs = items.sumOf { it.usageTimeMs }
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemHomeAppDetailBinding ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeAppDetailBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apps = items[position]
        holder.binding.apply {
            ivAppIcon.setImageDrawable(apps.appIcon)
            tvAppName.text = apps.appName
            tvTimeSpent.text = formatTime(apps.usageTimeMs)
            val percent = if (totalUsageMs > 0) (apps.usageTimeMs * 100f / totalUsageMs).roundToInt() else 0
            pbUsageProgress.progress = percent        }
    }

    /** Format ms → "xh ym" string */
    private fun formatTime(ms: Long): String {
        val totalMin = ms / 1000 / 60
        val h = totalMin / 60
        val m = totalMin % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }


}