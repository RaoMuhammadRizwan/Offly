package com.example.offly.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.offly.R
import com.example.offly.databinding.FragmentOnboarding1Binding

class OnboardingFragment1 : Fragment() {

    private var _binding : FragmentOnboarding1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentOnboarding1Binding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.beginJourneyCard.setOnClickListener { navigateToNextScreen() }

    }

    private fun navigateToNextScreen() {
        try {
            findNavController().navigate(
                R.id.action_onboardingFragment1_to_onboardingFragment2
            )
        } catch (e: Exception) {
            Log.e("OnboardingFragment1", "Navigation failed: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}