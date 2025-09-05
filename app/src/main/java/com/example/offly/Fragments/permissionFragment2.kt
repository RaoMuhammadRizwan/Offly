package com.example.offly.Fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.offly.R
import com.example.offly.Services.OfflyAccessibilityService
import com.example.offly.databinding.FragmentPermission2Binding
import com.example.offly.utils.CustomDialog
import com.example.offly.utils.PrefsManager

class permissionFragment2 : Fragment() {
    private var _binding : FragmentPermission2Binding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: PrefsManager
    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(hasAccessibilityServiceEnabled()){
            prefsManager.setAccessibilityPermissionGranted(granted = true)
            navigateToNextScreen()
        } else {
            showPermissionDialog()

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPermission2Binding.inflate(inflater , container , false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsManager = PrefsManager(requireContext())
        binding.btnGrantAccessibility.setOnClickListener{
            navigateToSettings()
        }
        binding.btnSkipAccessibility.setOnClickListener{
            navigateToNextScreen()
        }
    }

    private fun navigateToSettings(){
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            settingsLauncher.launch(intent)
        } catch (e : Exception){
            Log.e("AccessibilityFragment", "Navigation failed: ${e.message}", e)
            Toast.makeText(requireContext(), "Unable to navigate to settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToNextScreen(){
        try {
            findNavController().navigate(
                R.id.action_permissionFragment2_to_homeScreenFragment
            )
        } catch (e : Exception){
            Log.e("AccessibilityFragment", "Navigation failed: ${e.message}", e)
        }
    }

    private fun hasAccessibilityServiceEnabled() : Boolean {
        return try {
            val service = requireContext().packageName + "/" + OfflyAccessibilityService::class.java.name
            val enabledServices = Settings.Secure.getString(
                requireContext().contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            enabledServices?.contains(service) == true
        } catch (e : Exception){
            Log.e("AccessibilityFragment", "Permission check failed: ${e.message}", e)
            false
        }
    }

    private fun showPermissionDialog(){
        CustomDialog(requireContext()).showPermissionDialog(
            title = "Accessibility Permission Required",
            message = "We need accessibility permission to track your screen time.",
            positiveBtnText = "Grant Now",
            negativeBtnText = "Not Now",
            onPositiveButtonClick = { navigateToSettings() },
            onNegativeButtonClick = { navigateToNextScreen() }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}