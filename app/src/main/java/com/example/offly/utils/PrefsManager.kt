package com.example.offly.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class PrefsManager(context : Context) {
    private val offlyPrefs : SharedPreferences = context.getSharedPreferences(PREFS_NAME , Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "offlyPrefs"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
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


    /**
     * Clear all preferences stored in the app.
     */
    fun clearAllPreferences() {
        offlyPrefs.edit().clear().apply()
    }


}