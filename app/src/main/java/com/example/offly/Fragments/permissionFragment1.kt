    package com.example.offly.Fragments

    import android.content.Intent
    import android.net.Uri
    import android.os.Bundle
    import android.provider.Settings
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.fragment.app.Fragment
    import androidx.navigation.fragment.findNavController
    import com.example.offly.R
    import com.example.offly.databinding.FragmentPermission1Binding
    import com.example.offly.utils.CustomDialog
    import com.example.offly.utils.PrefsManager

    class permissionFragment1 : Fragment() {
        private var _binding: FragmentPermission1Binding? = null
        private val binding get() = _binding!!
        private lateinit var prefsManager: PrefsManager
        private val settingsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
            if(hasUsageAccessPermission()){
                prefsManager.setUsageAccessPermissionGranted(granted = true)
                navigateToNextScreen()
            } else {
                showPermissionDialog()
            }
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            _binding = FragmentPermission1Binding.inflate(inflater , container , false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            prefsManager = PrefsManager(requireContext())
            binding.btnGrantCard.setOnClickListener { navigateToSettings() }
            binding.btnSkip.setOnClickListener { navigateToNextScreen() }
        }

        private fun navigateToSettings() {
            try {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    data = Uri.parse("package:${requireContext().packageName}")
                }
                settingsLauncher.launch(intent)

            } catch (e : Exception){
                Log.e("PermissionFragment1" , "Navigation failed : ${e.message}" , e)
                Toast.makeText(requireContext() , "Unable to navigate to settings" , Toast.LENGTH_SHORT).show()
            }
        }

        private fun navigateToNextScreen(){
            try {
                findNavController().navigate(
                    R.id.action_permissionFragment1_to_permissionFragment2
                )
            } catch (e : Exception){
                Log.e("PermissionFragment1" , "Navigation failed : ${e.message}" , e)
            }
        }

        private fun hasUsageAccessPermission() : Boolean {
            return try {
                val appOps = requireContext().getSystemService(android.content.Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                val mode = appOps.checkOpNoThrow(
                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    requireContext().packageName
                )
                mode == android.app.AppOpsManager.MODE_ALLOWED
            } catch (e : Exception){
                Log.e("PermissionFragment1" , "Permission check failed : ${e.message}" , e)
                false
            }
        }

        private fun showPermissionDialog() {
            CustomDialog(requireContext()).showPermissionDialog(
                title = "Permission Required",
                message = "We need usage access permission to track your screen time.",
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