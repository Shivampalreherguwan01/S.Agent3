package com.sagent.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RemoteControlUIActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_control)

        val roomCode = intent.getStringExtra("ROOM_CODE") ?: "Unknown"
        val tvConnStatus = findViewById<TextView>(R.id.tvConnStatus)
        tvConnStatus.text = "🟢 Connected to : $roomCode"

        findViewById<Button>(R.id.btnDisconnect).setOnClickListener {
            finish() // स्क्रीन बंद करके वापस जाओ
        }
        
        // यहाँ हम आगे चलकर बटन्स में WebRTC कमांड्स जोड़ेंगे
    }
}
