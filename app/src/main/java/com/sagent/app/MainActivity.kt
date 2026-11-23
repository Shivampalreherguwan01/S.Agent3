package com.sagent.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs: SharedPreferences = getSharedPreferences("SAgentPrefs", MODE_PRIVATE)
        val savedMode = prefs.getString("APP_MODE", null)

        // अगर मोड पहले से सेव है, तो सीधे उसी स्क्रीन पर भेजो
        if (savedMode == "CONTROLLER") {
            startActivity(Intent(this, ControllerActivity::class.java))
            finish()
            return
        } else if (savedMode == "CONTROLLED") {
            startActivity(Intent(this, ControlledActivity::class.java))
            finish()
            return
        }

        // अगर पहली बार ऐप खुला है, तो मोड चुनने का ऑप्शन दो
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(80, 200, 80, 50)
            setBackgroundColor(android.graphics.Color.parseColor("#050B14"))
        }

        val title = TextView(this).apply {
            text = "S.Agent\nChoose Your Mode\n(Once selected, cannot be changed)"
            textSize = 20f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 100)
        }

        val btnController = Button(this).apply {
            text = "I want to Control (Controller)"
            setBackgroundColor(android.graphics.Color.parseColor("#1565C0"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                prefs.edit().putString("APP_MODE", "CONTROLLER").apply()
                startActivity(Intent(this@MainActivity, ControllerActivity::class.java))
                finish()
            }
        }

        val btnControlled = Button(this).apply {
            text = "I want to be Controlled (Source)"
            setBackgroundColor(android.graphics.Color.parseColor("#D32F2F"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                prefs.edit().putString("APP_MODE", "CONTROLLED").apply()
                startActivity(Intent(this@MainActivity, ControlledActivity::class.java))
                finish()
            }
        }

        layout.addView(title)
        layout.addView(btnController)
        layout.addView(btnControlled)

        setContentView(layout)
    }
}
