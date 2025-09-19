package com.example.offly.dataclass

data class UsageTrend(
    val durationMs: Long,
    val percentChange: Float,
    val arrowUp: Boolean,
    val compareDurationMs: Long = 0L
)