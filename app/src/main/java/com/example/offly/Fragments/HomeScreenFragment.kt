package com.example.offly.Fragments

import android.content.Context
import android.content.Intent
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.offly.R
import com.example.offly.databinding.FragmentHomeScreenBinding
import com.example.offly.utils.CustomDialog
import com.example.offly.utils.PrefsManager
import com.example.offly.viewModels.HomeViewModel

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

        viewModel.loadTotalUsageSinceMidnight()
        viewModel.loadTopUsedApps()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
