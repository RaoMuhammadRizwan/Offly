package com.example.offly.Fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.offly.R
import com.example.offly.databinding.FragmentHomeScreenBinding
import com.example.offly.utils.CustomDialog
import com.example.offly.utils.PrefsManager
import com.example.offly.viewModels.HomeViewModel
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

class HomeScreenFragment : Fragment() {

    private var _binding: FragmentHomeScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager : PrefsManager
    private lateinit var viewModel: HomeViewModel

    // Activity result for Usage Access settings
    private val usagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (hasUsageAccessPermission()) {
            prefsManager.setUsageAccessPermissionGranted(granted = true)
            unlockCards()
            viewModel.loadTotalUsageSinceMidnight()
            viewModel.loadTopUsedApps()
            viewModel.loadWeeklySocialUsage()
            viewModel.loadUsageTrends()
            viewModel.loadWeeklyTotalScreenTime()
        } else {
            prefsManager.setUsageAccessPermissionGranted(granted = false)
            lockCards()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        prefsManager = PrefsManager(requireContext())
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isUsageAccessPermissionRequired.observe(viewLifecycleOwner) { required ->
            if (required) {
                lockCards()
            } else {
                unlockCards()
            }
        }

        viewModel.totalUsageSinceMidnight.observe(viewLifecycleOwner) { usage ->
            binding.tvTodayUsageValue.text = usage
        }

        viewModel.weeklySocialUsage.observe(viewLifecycleOwner) { usage ->
            // usage = List<Float> with hours (e.g. 1.5f = 1h30m)

            // --- figure out day labels so that *today* is the last label ---
            val allDays = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
            val todayIdx = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK).let {
                // Calendar.SUNDAY = 1 … Calendar.SATURDAY = 7
                when (it) {
                    java.util.Calendar.MONDAY    -> 0
                    java.util.Calendar.TUESDAY   -> 1
                    java.util.Calendar.WEDNESDAY -> 2
                    java.util.Calendar.THURSDAY  -> 3
                    java.util.Calendar.FRIDAY    -> 4
                    java.util.Calendar.SATURDAY  -> 5
                    else                         -> 6 // Sunday
                }
            }
            val orderedDays = allDays.drop(todayIdx + 1) + allDays.take(todayIdx + 1)
            // e.g. if today is Wed (idx=2) => ["Thu","Fri","Sat","Sun","Mon","Tue","Wed"]

            val entries = usage.mapIndexed { index, hours ->
                BarEntry(index.toFloat(), hours * 60f) // store minutes
            }

            val dataSet = BarDataSet(entries, "Weekly Social Usage").apply {
                setGradientColor(
                    ContextCompat.getColor(requireContext(), R.color.chart_bar_start),
                    ContextCompat.getColor(requireContext(), R.color.chart_bar_end)
                )
                valueTextColor = Color.WHITE
                valueTextSize = 12f
                setDrawValues(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getBarLabel(barEntry: BarEntry?): String {
                        val totalMin = barEntry?.y?.toInt() ?: 0
                        return "${totalMin}m"          // ✅ minutes only
                    }
                }
            }

            val barData = BarData(dataSet).apply { barWidth = 0.8f }

            binding.weeklyChart.apply {
                data = barData
                description.isEnabled = false
                legend.isEnabled = false
                setFitBars(true)
                axisRight.isEnabled = false

                // --- prevent zoom / scale ---
                setScaleEnabled(false)
                setPinchZoom(false)
                isDoubleTapToZoomEnabled = false

                // --- X Axis (days, today last) ---
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = Color.WHITE
                    textSize = 12f
                    setDrawGridLines(false)
                    valueFormatter = IndexAxisValueFormatter(orderedDays)
                }

                // --- Y Axis (minutes only) ---
                axisLeft.apply {
                    textColor = Color.WHITE
                    setDrawGridLines(true)
                    axisMinimum = 0f
                    val maxMin = (usage.maxOrNull() ?: 0f) * 60f
                    axisMaximum = (maxMin + 30).coerceAtLeast(60f)
                    granularity = 15f // 15-minute steps
                    valueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                            return "${value.toInt()}m"   // ✅ minutes only
                        }
                    }
                }

                animateY(800)
                invalidate()
            }
        }


        viewModel.topUsedApps.observe(viewLifecycleOwner) { apps ->
            val container = binding.layoutTopAppsList
            container.removeAllViews()

            apps.forEach { (name, time, icon) ->
                val row = layoutInflater.inflate(R.layout.item_top_app, container, false)

                val iconView = row.findViewById<ImageView>(R.id.ivAppIcon)
                val nameView = row.findViewById<TextView>(R.id.tvAppName)
                val timeView = row.findViewById<TextView>(R.id.tvAppTime)

                iconView.setImageDrawable(icon)
                nameView.text = name
                timeView.text = time

                container.addView(row)
            }
        }

        viewModel.dailyTrend.observe(viewLifecycleOwner) { trend ->
            if (trend != null) {
                val today = formatUsageTime(trend.durationMs)
                val yesterday = formatUsageTime(trend.compareDurationMs)
                binding.tvTodayTime.text = today
                binding.tvYesterdayTime.text = yesterday

                binding.tvPercentChange.apply {
                    text = if (trend.arrowUp) "↑${trend.percentChange.toInt()}%"
                    else "↓${trend.percentChange.toInt()}%"
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            if (trend.arrowUp) R.color.red else R.color.green
                        )
                    )
                }
            }
        }

        viewModel.weeklyTrend.observe(viewLifecycleOwner) { trend ->
            if (trend != null) {
                val today = formatUsageTime(trend.durationMs)
                val lastWeek = formatUsageTime(trend.compareDurationMs)
                binding.tvTodayTime.text = today
                binding.tvLastWeekTime.text = lastWeek
            }
        }

        viewModel.weeklyTotalScreenTime.observe(viewLifecycleOwner) { totalScreenTimeList ->
            val total = totalScreenTimeList.sum()
            val average = if (totalScreenTimeList.isNotEmpty()) total / totalScreenTimeList.size else 0L
            binding.tvWeeklyTotal.text = formatUsageTime(total)
            binding.tvWeeklyAverage.text = formatUsageTime(average)

        }



        viewModel.loadTotalUsageSinceMidnight()
        viewModel.loadTopUsedApps()
        viewModel.loadWeeklySocialUsage()
        viewModel.loadUsageTrends()
        viewModel.loadWeeklyTotalScreenTime()
    }

    private fun showPermissionDialog() {
        CustomDialog(requireContext()).showPermissionDialog(
            title = "Permission Required",
            message = "We need Usage Access permission to calculate your screen time accurately.",
            positiveBtnText = "Give Permission",
            negativeBtnText = "Not Now",
            onPositiveButtonClick = { openUsageAccessSettings() },
            onNegativeButtonClick = {  }
        )
    }

    private fun openUsageAccessSettings() {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            usagePermissionLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("HomeScreenFragment", "Failed to open settings: ${e.message}", e)
        }
    }

    private fun hasUsageAccessPermission(): Boolean {
        return try {
            val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                requireContext().packageName
            )
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    private fun lockCards() {
        binding.cardTodayUsage.setOnClickListener { showPermissionDialog() }
        binding.cardTopApps.setOnClickListener { showPermissionDialog() }

        binding.overlayTodayUsage.visibility = View.VISIBLE
        binding.overlayTopApps.visibility = View.VISIBLE
    }

    private fun unlockCards() {
        binding.overlayTodayUsage.visibility = View.GONE
        binding.overlayTopApps.visibility = View.GONE

        binding.cardTodayUsage.setOnClickListener {
            Log.d("HomeScreenFragment", "Today Usage Card Clicked")
        }

        binding.cardTopApps.setOnClickListener {
            Log.d("HomeScreenFragment", "Top Apps Card Clicked")
        }
    }

    private fun formatUsageTime(ms: Long): String {
        val minutes = ms / 60_000
        val hours = minutes / 60
        val remaining = minutes % 60
        return when {
            hours > 0 -> "${hours}h ${remaining}m"
            minutes > 0 -> "${minutes}m"
            else -> "<1m"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
