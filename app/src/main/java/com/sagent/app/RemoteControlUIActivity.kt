package com.sagent.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import org.webrtc.EglBase
import org.webrtc.SurfaceViewRenderer

class RemoteControlUIActivity : AppCompatActivity() {
    private var webRTCClient: WebRTCClient? = null
    private val eglBase = EglBase.create()
    private lateinit var remoteVideoView: SurfaceViewRenderer

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_control)

        val roomCode = intent.getStringExtra("ROOM_CODE") ?: "Unknown"
        findViewById<TextView>(R.id.tvConnStatus).text = "🟢 Connected to : $roomCode"

        remoteVideoView = findViewById(R.id.remoteVideoView)

        // Start WebRTC as Controller
        webRTCClient = WebRTCClient(
            context = this,
            roomCode = roomCode,
            isController = true,
            eglBase = eglBase,
            videoView = remoteVideoView
        )
        webRTCClient?.startConnection()

        // Touch Listener on Video View (for remote control)
        remoteVideoView.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val normX = event.x / view.width.toFloat()
                val normY = event.y / view.height.toFloat()
                
                // Send normalized coordinates to Source
                val json = JSONObject().apply {
                    put("action", "TOUCH")
                    put("x", normX)
                    put("y", normY)
                }
                webRTCClient?.sendCommand(json.toString())
            }
            true
        }

        // Bind Buttons
        findViewById<Button>(R.id.btnHome).setOnClickListener { sendCommand("HOME") }
        findViewById<Button>(R.id.btnBack).setOnClickListener { sendCommand("BACK") }
        findViewById<Button>(R.id.btnMenu).setOnClickListener { sendCommand("RECENTS") }
        
        findViewById<Button>(R.id.btnDisconnect).setOnClickListener {
            finish()
        }
    }

    private fun sendCommand(action: String) {
        val json = JSONObject().apply { put("action", action) }
        webRTCClient?.sendCommand(json.toString())
    }
}
