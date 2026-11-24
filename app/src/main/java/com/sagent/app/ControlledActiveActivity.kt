package com.sagent.app

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.webrtc.EglBase

class ControlledActiveActivity : AppCompatActivity() {
    private var webRTCClient: WebRTCClient? = null
    private val eglBase = EglBase.create()
    private val REQUEST_CODE_SCREEN_CAPTURE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controlled_active)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val roomCode = getSharedPreferences("SAgentPrefs", MODE_PRIVATE).getString("PERMANENT_ROOM_CODE", "")

        tvStatus.text = "Connected! Waiting for controller to start screen share..."

        webRTCClient = WebRTCClient(
            context = this,
            roomCode = roomCode!!,
            isController = false,
            eglBase = eglBase,
            onCommandReceived = { command ->
                runOnUiThread {
                    MyAccessibilityService.getInstance()?.executeCommand(command)
                }
            }
        )
        webRTCClient?.startConnection()

        findViewById<Button>(R.id.btnAccessibilityPermission).setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        // Start MediaProjection Permission
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE_SCREEN_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE && resultCode == RESULT_OK && data != null) {
            // Send Intent directly to WebRTCClient
            webRTCClient?.startScreenCapture(data)
            Toast.makeText(this, "Screen sharing started!", Toast.LENGTH_SHORT).show()
        }
    }
}
