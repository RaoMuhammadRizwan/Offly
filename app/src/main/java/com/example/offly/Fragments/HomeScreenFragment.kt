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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.offly.databinding.FragmentHomeScreenBinding
import com.example.offly.repository.HomeRepository
import com.example.offly.utils.CustomDialog
import com.example.offly.utils.PrefsManager
import com.example.offly.viewModels.HomeViewModel

class HomeScreenFragment : Fragment() {

    private var _binding: FragmentHomeScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager : PrefsManager
    private val viewModel by lazy { HomeViewModel(HomeRepository(requireContext())) }

    // Activity result for Usage Access settings
    private val usagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (hasUsageAccessPermission()) {
            prefsManager.setUsageAccessPermissionGranted(granted = true)
            hidePermissionMessage()
            viewModel.loadTodayUsage()
        } else {
            prefsManager.setUsageAccessPermissionGranted(granted = false)
            showPermissionRequiredState()
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
        prefsManager = PrefsManager(requireContext())
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isUsageAccessPermissionRequired.observe(viewLifecycleOwner) { required ->
            if (required) {
                showPermissionRequiredState()
                binding.cardTodayUsage.setOnClickListener { showPermissionDialog() }
            } else {
                hidePermissionMessage()
                binding.cardTodayUsage.setOnClickListener {
                    Log.d("HomeScreenFragment", "Today Usage Card Clicked")
                }
            }
        }

        viewModel.todayUsage.observe(viewLifecycleOwner) { usage ->
            binding.tvTodayUsageValue.text = usage
        }

        viewModel.loadTodayUsage()
    }

    private fun showPermissionRequiredState() {
        binding.tvTodayUsageValue.text = "00:00"
        binding.tvPermissionRequired.apply {
            visibility = View.VISIBLE
            text = "Permission required to track usage"
        }
    }

    private fun hidePermissionMessage() {
        binding.tvPermissionRequired.visibility = View.GONE
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
