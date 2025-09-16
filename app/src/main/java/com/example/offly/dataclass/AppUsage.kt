package com.example.offly.dataclass

import android.graphics.drawable.Drawable

data class AppUsage(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable,
    val usageTimeMs: Long
)
