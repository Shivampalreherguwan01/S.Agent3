package com.sagent.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ControllerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)

        val etRoomCode = findViewById<EditText>(R.id.etRoomCode)
        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val database = FirebaseDatabase.getInstance().reference

        btnConnect.setOnClickListener {
            val code = etRoomCode.text.toString().trim().uppercase()
            if (code.length < 6) {
                Toast.makeText(this, "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase में चेक करो कि ये कोड मौजूद है या नहीं
            database.child("sessions").child(code).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // कोड मिल गया, कनेक्टेड स्टेटस अपडेट करो
                        database.child("sessions").child(code).child("controller_connected").setValue(true)
                        
                        // अब Remote UI (फोटो वाली स्क्रीन) पर जाओ
                        val intent = Intent(this@ControllerActivity, RemoteControlUIActivity::class.java)
                        intent.putExtra("ROOM_CODE", code)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@ControllerActivity, "Invalid Code. Device not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ControllerActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
