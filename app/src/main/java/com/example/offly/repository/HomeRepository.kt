package com.example.offly.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.example.offly.dataclass.AppUsage
import com.example.offly.utils.PrefsManager
import java.util.Calendar
import kotlin.math.max

class HomeRepository(private val context: Context) {

    private val prefsManager = PrefsManager(context)

    /** ✅ check stored permission flag */
    fun isUsageAccessPermissionGranted(): Boolean =
        prefsManager.getUsageAccessPermissionGranted()

    /** Packages we care about (social list) */
    private val socialApps = setOf(
        "com.google.android.youtube",
        "com.whatsapp",
        "com.instagram.android",
        "com.facebook.katana",
        "com.zhiliaoapp.musically",
        "com.snapchat.android",
        "com.linkedin.android"
    )

    /** Timestamp for today 00:00 */
    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** Build a map of package -> foreground ms for [start,end] */
    private fun usageSince(start: Long, end: Long): Map<String, Long> {
        val mgr = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val events = mgr.queryEvents(start, end)
        val event = UsageEvents.Event()

        val lastStart = mutableMapOf<String, Long>()
        val total = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            if (!events.getNextEvent(event)) break
            val pkg = event.packageName ?: continue

            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND,
                UsageEvents.Event.ACTIVITY_RESUMED ->
                    lastStart[pkg] = max(event.timeStamp, start)

                UsageEvents.Event.MOVE_TO_BACKGROUND,
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    val s = lastStart.remove(pkg) ?: continue
                    val dur = (minOf(event.timeStamp, end) - max(s, start)).coerceAtLeast(0)
                    if (dur > 0) total[pkg] = total.getOrDefault(pkg, 0L) + dur
                }
            }
        }

        // still running
        for ((pkg, s) in lastStart) {
            val dur = (end - max(s, start)).coerceAtLeast(0)
            if (dur > 0) total[pkg] = total.getOrDefault(pkg, 0L) + dur
        }
        return total
    }

    /** ✅ Total screen-on time (all launchable apps) since midnight. */
    fun getTotalUsageSinceMidnight(): String {
        val pm = context.packageManager
        val self = context.packageName
        val usage = usageSince(startOfToday(), System.currentTimeMillis())

        val totalMs = usage.entries.sumOf { (pkg, ms) ->
            if (pkg == self) 0L
            else {
                val ai = try { pm.getApplicationInfo(pkg, 0) } catch (_: Exception) { null }
                if (ai != null && pm.getLaunchIntentForPackage(pkg) != null &&
                    ai.flags and (android.content.pm.ApplicationInfo.FLAG_SYSTEM or
                            android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
                ) ms else 0L
            }
        }
        val h = totalMs / 1000 / 60 / 60
        val m = (totalMs / 1000 / 60) % 60
        return "${h}h ${m}m"
    }

    /** ✅ Top 3 apps from social list with icon, label & minutes. */
    fun getTopUsedSocialApps(limit: Int = 3): List<AppUsage> {
        val pm = context.packageManager
        val usage = usageSince(startOfToday(), System.currentTimeMillis())

        return usage.filterKeys { it in socialApps }
            .mapNotNull { (pkg, ms) ->
                try {
                    val ai = pm.getApplicationInfo(pkg, 0)
                    AppUsage(
                        packageName = pkg,
                        appName = ai.loadLabel(pm).toString(),
                        appIcon = ai.loadIcon(pm),
                        usageTimeMs = ms
                    )
                } catch (_: PackageManager.NameNotFoundException) {
                    null
                }
            }
            .sortedByDescending { it.usageTimeMs }
            .take(limit)
    }

    /** ✅ 7-day history for ONLY the socialApps list (returns hours/day). */
    fun getWeeklySocialUsage(): List<Float> {
        val pm = context.packageManager
        val today = Calendar.getInstance()

        // Move to end of current day
        today.set(Calendar.HOUR_OF_DAY, 23)
        today.set(Calendar.MINUTE, 59)
        today.set(Calendar.SECOND, 59)
        today.set(Calendar.MILLISECOND, 999)

        var end = today.timeInMillis
        val hoursPerDay = mutableListOf<Float>()

        repeat(7) {
            // Start of the day
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)
            val start = today.timeInMillis

            val usage = usageSince(start, end)

            // Sum only apps in the predefined socialApps set
            val totalMs = usage.entries
                .filter { it.key in socialApps }
                .sumOf { it.value }

            hoursPerDay.add(totalMs / 1000f / 60f / 60f) // convert ms → hours (Float)

            // Prepare for previous day
            end = start
            today.add(Calendar.DAY_OF_YEAR, -1)
        }

        return hoursPerDay.reversed() // oldest → newest
    }

    fun getTotalUsageForDay(daysAgo: Int): Long {
        val cal = Calendar.getInstance()
        // End of target day
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val end = cal.timeInMillis

        // Start of target day
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        return usageSince(start, end)
            .filterKeys { it != context.packageName }
            .values.sum()
    }

    fun getWeeklyTotalScreenTime(): List<Long> {
        val pm = context.packageManager
        val cal = Calendar.getInstance()

        // Move to the *end* of today
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)

        var end = cal.timeInMillis
        val dailyTotals = mutableListOf<Long>()

        repeat(7) {
            // Start of current day
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis

            // total usage for this day (exclude your own app, include all launchable apps)
            val totalMs = usageSince(start, end).entries.sumOf { (pkg, ms) ->
                if (pkg == context.packageName) 0L
                else {
                    val ai = try { pm.getApplicationInfo(pkg, 0) } catch (_: Exception) { null }
                    if (ai != null && pm.getLaunchIntentForPackage(pkg) != null) ms else 0L
                }
            }

            dailyTotals.add(totalMs)

            // shift to previous day
            end = start
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        return dailyTotals.reversed()  // oldest → newest
    }
}
