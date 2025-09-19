package com.example.offly.viewModels

import android.app.Application
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.offly.dataclass.AppUsage
import com.example.offly.dataclass.UsageTrend
import com.example.offly.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = HomeRepository(application)
    val totalUsageSinceMidnight = MutableLiveData<String>()
    val topUsedApps = MutableLiveData<List<Triple<String, String, Drawable>>>()
    val isUsageAccessPermissionRequired = MutableLiveData<Boolean>()
    val weeklySocialUsage = MutableLiveData<List<Float>>()
    val weeklyTotalScreenTime = MutableLiveData<List<Long>>()
    val allSocialAppsUsageToday = MutableLiveData<List<AppUsage>>()
    val allSocialAppsUsageForDay = MutableLiveData<List<AppUsage>>()

    val dailyTrend = MutableLiveData<UsageTrend?>()
    val weeklyTrend = MutableLiveData<UsageTrend?>()



    fun loadTotalUsageSinceMidnight() {
        if(repo.isUsageAccessPermissionGranted()){
            totalUsageSinceMidnight.value = repo.getTotalUsageSinceMidnight()
            isUsageAccessPermissionRequired.value = false
        } else {
            isUsageAccessPermissionRequired.value = true
        }
    }

    fun loadTopUsedApps() {
        if (repo.isUsageAccessPermissionGranted()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val apps = repo.getTopUsedSocialApps().map { appUsage ->
                        val timeFormatted = formatUsageTime(appUsage.usageTimeMs)
                        Triple(appUsage.appName, timeFormatted, appUsage.appIcon)
                    }
                    topUsedApps.postValue(apps)
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error loading top used apps", e)
                    topUsedApps.postValue(emptyList())
                }
            }
        }
    }

    fun loadWeeklySocialUsage() {
        if (repo.isUsageAccessPermissionGranted()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val usage = repo.getWeeklySocialUsage()
                    weeklySocialUsage.postValue(usage)
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error loading weekly social usage", e)
                    weeklySocialUsage.postValue(emptyList())
                }

            }
        }
    }

    /**
     * Load today's total usage and compare with yesterday & last week
     */
    fun loadUsageTrends() {
        if (!repo.isUsageAccessPermissionGranted()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val todayMs: Long = repo.getTotalUsageForDay(0)
                val yesterdayMs: Long = repo.getTotalUsageForDay(1)
                val lastWeekMs: Long = repo.getTotalUsageForDay(7)

                // vs yesterday
                val changeY = if (yesterdayMs > 0)
                    ((todayMs - yesterdayMs) * 100f / yesterdayMs) else 0f

                // vs last week same day
                val changeW = if (lastWeekMs > 0)
                    ((todayMs - lastWeekMs) * 100f / lastWeekMs) else 0f

                dailyTrend.postValue(
                    UsageTrend(
                        durationMs = todayMs,
                        percentChange = abs(changeY),
                        arrowUp = changeY >= 0,
                        compareDurationMs = yesterdayMs
                    )
                )
                weeklyTrend.postValue(
                    UsageTrend(
                        durationMs = todayMs,
                        percentChange = abs(changeW),
                        arrowUp = changeW >= 0,
                        compareDurationMs = lastWeekMs
                    )
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading usage trends", e)
                dailyTrend.postValue(null)
                weeklyTrend.postValue(null)
            }
        }
    }

    fun loadWeeklyTotalScreenTime() {
        if (!repo.isUsageAccessPermissionGranted()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val totalScreenTime = repo.getWeeklyTotalScreenTime()
                weeklyTotalScreenTime.postValue(totalScreenTime)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading weekly total screen time", e)
                weeklyTotalScreenTime.postValue(emptyList())
            }
        }
    }

    fun loadAllSocialAppsUsageToday() {
        if (!repo.isUsageAccessPermissionGranted()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val usage = repo.getAllSocialAppsUsageToday()
                allSocialAppsUsageToday.postValue(usage)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading all social apps usage today", e)
                allSocialAppsUsageToday.postValue(emptyList())
            }
        }
    }

    fun loadAllSocialAppsUsageForDay(daysAgo: Int){
        if (!repo.isUsageAccessPermissionGranted()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val usage = repo.getAllSocialAppsUsageForDay(daysAgo)
                allSocialAppsUsageForDay.postValue(usage)
            } catch (e : Exception){
            Log.e("HomeViewModel", "Error loading all social apps usage for day", e)
            allSocialAppsUsageForDay.postValue(emptyList())
            }
        }
    }





    private fun formatUsageTime(timeMs: Long): String {
        val minutes = timeMs / 60_000
        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        return when {
            hours > 0 -> "${hours}h ${remainingMinutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "<1m"
        }
    }
}