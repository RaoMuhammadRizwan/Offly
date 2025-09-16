package com.example.offly.viewModels

import android.app.Application
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.offly.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = HomeRepository(application)
    val totalUsageSinceMidnight = MutableLiveData<String>()
    val topUsedApps = MutableLiveData<List<Triple<String, String, Drawable>>>()
    val isUsageAccessPermissionRequired = MutableLiveData<Boolean>()
    val weeklySocialUsage = MutableLiveData<List<Float>>()


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