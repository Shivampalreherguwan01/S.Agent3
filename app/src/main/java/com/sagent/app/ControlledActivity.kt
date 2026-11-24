package com.sagent.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ControlledActivity : AppCompatActivity() {
    private lateinit var tvRoomCode: TextView
    private lateinit var tvStatus: TextView
    private lateinit var database: DatabaseReference
    private var roomCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controlled)

        tvRoomCode = findViewById(R.id.tvRoomCode)
        tvStatus = findViewById(R.id.tvStatus)
        database = FirebaseDatabase.getInstance().reference

        // SharedPreferences से पुराना कोड चेक करो
        val prefs = getSharedPreferences("SAgentPrefs", MODE_PRIVATE)
        val savedCode = prefs.getString("PERMANENT_ROOM_CODE", null)

        if (savedCode != null) {
            roomCode = savedCode // अगर सेव है तो वही इस्तेमाल करो
        } else {
            // नहीं है तो नया बनाओ और सेव कर दो
            roomCode = (1..6).map { ('A'..'Z').random() }.joinToString("")
            prefs.edit().putString("PERMANENT_ROOM_CODE", roomCode).apply()
        }

        tvRoomCode.text = roomCode

        // Firebase में कोड सेव करो
        database.child("sessions").child(roomCode).child("status").setValue("waiting")
            .addOnFailureListener {
                Toast.makeText(this, "Firebase Error: Check Rules", Toast.LENGTH_LONG).show()
            }

        // कंट्रोलर के जुड़ने का इंतज़ार
        database.child("sessions").child(roomCode).child("controller_connected").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.value == true) {
                    tvStatus.text = "Connected! Moving to next screen..."
                    startActivity(Intent(this@ControlledActivity, ControlledActiveActivity::class.java))
                    finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
