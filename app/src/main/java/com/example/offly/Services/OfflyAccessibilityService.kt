package com.example.offly.Services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class OfflyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        TODO("Not yet implemented")
        Log.d("MyAccessibilityService", "Event received: $event")
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
        Log.d("MyAccessibilityService", "Service interrupted")
    }
}