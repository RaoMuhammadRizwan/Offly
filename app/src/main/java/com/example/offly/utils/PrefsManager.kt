package com.example.offly.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class PrefsManager(context : Context) {
    private val offlyPrefs : SharedPreferences = context.getSharedPreferences(PREFS_NAME , Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "offlyPrefs"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
        private const val IS_USAGE_ACCESS_GRANTED = "IsUsageAccessGranted"
    }

    fun setFirstTimeLaunch(isFirstTime : Boolean){
        offlyPrefs.edit().putBoolean(IS_FIRST_TIME_LAUNCH , isFirstTime).apply()
    }

    fun isFirstTimeLaunch() : Boolean {
        val isFirstTime = offlyPrefs.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        Log.d("TEST RR", "First time launch : $isFirstTime")
        return isFirstTime
    }

    /**
     * Reset first time launch status (useful for testing)
     * This will make the app show onboarding on next launch
     */
    fun resetFirstTimeLaunch() {
        offlyPrefs.edit().putBoolean(IS_FIRST_TIME_LAUNCH, true).apply()
    }

    fun setUsageAccessPermissionGranted(granted : Boolean){
        offlyPrefs.edit().putBoolean(IS_USAGE_ACCESS_GRANTED , granted).apply()
    }

    fun getUsageAccessPermissionGranted() : Boolean {
        val isUsageAccessPermissionGranted = offlyPrefs.getBoolean(IS_USAGE_ACCESS_GRANTED , false)
        Log.d("TEST RR" , "Usage access permission granted : $isUsageAccessPermissionGranted")
        return isUsageAccessPermissionGranted
    }


    /**
     * Clear all preferences stored in the app.
     */
    fun clearAllPreferences() {
        offlyPrefs.edit().clear().apply()
    }


}