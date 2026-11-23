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

        // रैंडम 6 अक्षरों का कोड बनाओ
        roomCode = (1..6).map { ('A'..'Z').random() }.joinToString("")
        tvRoomCode.text = roomCode

        // Firebase में कोड को सेव करो
        database.child("sessions").child(roomCode).child("status").setValue("waiting")
            .addOnSuccessListener {
                Toast.makeText(this, "Code generated successfully", Toast.LENGTH_SHORT).show()
            }

        // कंट्रोलर के जुड़ने का इंतज़ार करो
        database.child("sessions").child(roomCode).child("controller_connected").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.value == true) {
                    tvStatus.text = "Connected! Moving to next screen..."
                    // जब कंट्रोलर जुड़ जाए, तो अगली स्क्रीन पर जाओ
                    startActivity(Intent(this@ControlledActivity, ControlledActiveActivity::class.java))
                    finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
