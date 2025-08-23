package com.example.offly.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.offly.R
import com.example.offly.databinding.FragmentSplashBinding
import com.example.offly.utils.PrefsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    companion object {
        private const val SPLASH_DELAY = 2000L
        private const val TAG = "SplashFragment"
    }

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: PrefsManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeComponents()

        startSplashFlow()
    }

    private fun initializeComponents() {
        try {
            prefsManager = PrefsManager(requireContext())
        } catch (e: Exception) {
            Log.e(TAG, "PrefsManager init failed: ${e.message}")
        }
    }

    private fun startSplashFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                delay(SPLASH_DELAY)
                if (isAdded) {
                    navigateToNextScreen()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Splash delay failed: ${e.message}")
                navigateToHomeScreen()
            }
        }
    }

    private fun navigateToNextScreen() {
        if (!isAdded) return
        try {
            Log.d(TAG, "First time launch : ${prefsManager.isFirstTimeLaunch()}")
            val destination = if (prefsManager.isFirstTimeLaunch()) {
                prefsManager.setFirstTimeLaunch(false)
                Log.d(TAG, "Navigating to onboarding screen")
                R.id.action_splashFragment_to_onboardingFragment1
            } else {
                Log.d(TAG, "Navigating to home screen")
                R.id.action_splashFragment_to_homeScreenFragment
            }
            findNavController().navigate(destination)
        } catch (e: Exception) {
            Log.e(TAG, "Navigation failed: ${e.message}")
        }
    }

    private fun navigateToHomeScreen() {
        if (!isAdded) return
        try {
            findNavController().navigate(R.id.action_splashFragment_to_homeScreenFragment)
        } catch (e: Exception) {
            Log.e(TAG, "Navigation to home failed: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
