package com.sagent.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ControlledActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this).apply {
            text = "Source Mode Active.\nWaiting for Controller to connect...\n(Screen sharing code will go here)"
            textSize = 18f
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#050B14"))
            setPadding(50, 200, 50, 50)
        }
        setContentView(tv)
    }
}
