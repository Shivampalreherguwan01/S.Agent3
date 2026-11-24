package com.sagent.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import org.json.JSONObject

class MyAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: MyAccessibilityService? = null
        fun getInstance(): MyAccessibilityService? = instance
    }

    override fun onServiceConnected() {
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() { instance = null }

    fun executeCommand(jsonCommand: String) {
        try {
            val json = JSONObject(jsonCommand)
            val action = json.getString("action")

            when (action) {
                "HOME" -> performGlobalAction(GLOBAL_ACTION_HOME)
                "BACK" -> performGlobalAction(GLOBAL_ACTION_BACK)
                "RECENTS" -> performGlobalAction(GLOBAL_ACTION_RECENTS)
                "TOUCH" -> {
                    val x = json.getDouble("x").toFloat()
                    val y = json.getDouble("y").toFloat()
                    dispatchClick(x, y)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dispatchClick(x: Float, y: Float) {
        val path = Path()
        path.moveTo(x, y)
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 50))
        dispatchGesture(gestureBuilder.build(), null, null)
    }
}
