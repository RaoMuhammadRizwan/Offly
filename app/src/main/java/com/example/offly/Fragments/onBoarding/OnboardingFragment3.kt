package com.example.offly.Fragments.onBoarding

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.offly.R
import com.example.offly.databinding.FragmentOnboarding3Binding

class onboardingFragment3 : Fragment() {
    private var _binding: FragmentOnboarding3Binding ?= null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       _binding = FragmentOnboarding3Binding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnFinish.setOnClickListener { navigateToNextScreen() }
    }

    private fun navigateToNextScreen() {
        try {
            findNavController().navigate(
                R.id.action_onboardingFragment3_to_homeScreenFragment
            )

        }catch (e : Exception){
            Log.e("OnboardingFragment3" , "Navigation failed : ${e.message}" , e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}