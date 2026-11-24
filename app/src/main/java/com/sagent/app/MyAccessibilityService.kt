package com.sagent.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // यहाँ हम आगे चलकर टच कमांड्स हैंडल करेंगे
    }
    override fun onInterrupt() {}
}
