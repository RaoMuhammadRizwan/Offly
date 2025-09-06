package com.example.offly.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.offly.repository.HomeRepository

class HomeViewModel(private val repo : HomeRepository) : ViewModel() {
    val todayUsage = MutableLiveData<String>()
    val isUsageAccessPermissionRequired = MutableLiveData<Boolean>()

    fun loadTodayUsage(){
        if(repo.isUsageAccessPermissionGranted()){
            todayUsage.value = repo.getTodayUsage()
            isUsageAccessPermissionRequired.value = false
        } else {
            isUsageAccessPermissionRequired.value = true
        }
    }

}