package com.example.offly.repository

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.offly.R
import com.example.offly.utils.PrefsManager
import java.util.Calendar

class HomeRepository(private val context: Context) {
    private val prefsManager = PrefsManager(context)

    fun isUsageAccessPermissionGranted(): Boolean {
        return prefsManager.getUsageAccessPermissionGranted()
    }

    fun getTodayUsage(): String {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24*60*60*1000L

        val usageStats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
        var totalMillis = 0L
        usageStats.values.forEach { totalMillis += it.totalTimeInForeground }

        val hours = totalMillis / 1000 / 60 / 60
        val minutes = (totalMillis / 1000 / 60) % 60

        return "${hours}h ${minutes}m"
    }

}