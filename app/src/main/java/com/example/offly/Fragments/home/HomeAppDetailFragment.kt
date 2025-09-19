package com.example.offly.Fragments.home

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.offly.R
import com.example.offly.adapters.SocialUsageAdapter
import com.example.offly.databinding.FragmentHomeAppDetailBinding
import com.example.offly.databinding.FragmentHomeScreenBinding
import com.example.offly.dataclass.AppUsage
import com.example.offly.utils.PrefsManager
import com.example.offly.viewModels.HomeViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.DateFormat
import java.util.Calendar

class HomeAppDetailFragment : Fragment() {
    private var _binding: FragmentHomeAppDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: SocialUsageAdapter
    private var dayOffset = 0 // 0 = today

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAppDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        adapter = SocialUsageAdapter(requireContext(), emptyList())
        binding.rvAppsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeAppDetailFragment.adapter
        }

        setupDateNavigation()
        observeViewModel()
        viewModel.loadAllSocialAppsUsageForDay(dayOffset)

    }
    private fun observeViewModel() {
        viewModel.allSocialAppsUsageForDay.observe(viewLifecycleOwner) { apps ->
            adapter.setData(apps)
            updateLineChart(apps)
        }
    }


    private fun setupDateNavigation() {
        updateDateText(dayOffset)

        binding.btnPreviousDate.setOnClickListener {
            dayOffset++
            updateDateText(dayOffset)
            viewModel.loadAllSocialAppsUsageForDay(dayOffset)
        }

        binding.btnNextDate.setOnClickListener {
            if (dayOffset > 0) {
                dayOffset--
                updateDateText(dayOffset)
                viewModel.loadAllSocialAppsUsageForDay(dayOffset)
            }
        }
    }


    private fun updateDateText(offset: Int) {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
        val label = if (offset == 0) "Today"
        else android.text.format.DateFormat.format("EEE, MMM d", cal).toString()
        binding.tvCurrentDate.text = label
    }

    private fun updateLineChart(apps: List<AppUsage>) {
        if (apps.isEmpty()) return

        val sortedApps = apps.sortedByDescending { it.usageTimeMs }

        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        val colors = mutableListOf<Int>()

        sortedApps.forEachIndexed { index, app ->
            val hours = app.usageTimeMs / (1000f * 60 * 60) // hours as float
            entries.add(Entry(index.toFloat(), hours))
            labels.add(getAppShortName(app.appName))
            colors.add(getAppBrandColor(app.appName))
        }

        val dataSet = LineDataSet(entries, "App Usage").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            lineWidth = 2f
            circleRadius = 6f
            setDrawCircleHole(false)
            setDrawValues(true)
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            color = Color.parseColor("#1877F2") // default line color
            setCircleColors(colors)
            valueFormatter = object : ValueFormatter() {
                override fun getPointLabel(entry: Entry?): String {
                    val v = entry?.y ?: 0f
                    val hoursInt = v.toInt()
                    val minutes = ((v - hoursInt) * 60).toInt()
                    return if (hoursInt > 0) "$hoursInt h $minutes m" else "$minutes m"
                }
            }
            setDrawFilled(true)
            fillColor = Color.parseColor("#4285F4")
            fillAlpha = 50
        }

        val data = LineData(dataSet)

        binding.lineChart.apply {
            this.data = data
            description.isEnabled = false

            // Disable pinch zoom & scaling
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)

            // X-axis = app names
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor = Color.WHITE
                textSize = 12f
                valueFormatter = IndexAxisValueFormatter(labels)
                setDrawGridLines(false)
            }

            // Y-axis = show proper hours/minutes
            axisLeft.apply {
                textColor = Color.WHITE
                axisMinimum = 0f
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val hoursInt = value.toInt()
                        val minutes = ((value - hoursInt) * 60).toInt()
                        return if (hoursInt > 0) "$hoursInt h" else "$minutes m"
                    }
                }
            }
            axisRight.isEnabled = false

            legend.apply {
                isEnabled = true
                textColor = Color.WHITE
                textSize = 12f
                form = Legend.LegendForm.LINE
            }

            animateY(1200, Easing.EaseInOutQuad)
            invalidate()
        }
    }


    private fun getAppShortName(appName: String): String = when (appName.lowercase()) {
        "youtube" -> "YT"
        "instagram" -> "IG"
        "facebook" -> "FB"
        "whatsapp" -> "WA"
        "snapchat" -> "SC"
        "tiktok"   -> "TT"
        "twitter"  -> "TW"
        "chrome"   -> "CR"
        else -> appName.take(3).uppercase()
    }

    private fun getAppBrandColor(appName: String): Int = when (appName.lowercase()) {
        "youtube"  -> Color.parseColor("#FF0000")
        "instagram"-> Color.parseColor("#E4405F")
        "facebook" -> Color.parseColor("#1877F2")
        "whatsapp" -> Color.parseColor("#25D366")
        "snapchat" -> Color.parseColor("#FFFC00")
        "tiktok"   -> Color.parseColor("#000000")
        "twitter"  -> Color.parseColor("#1DA1F2")
        "chrome"   -> Color.parseColor("#4285F4")
        else       -> Color.parseColor("#9E9E9E")
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}