package com.sagent.app

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // बेसिक लेआउट (प्रोग्रामेटिकली ताकि फाइल कम रखें)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            paddingLeft = 50
            paddingTop = 100
        }

        val title = TextView(this).apply {
            text = "S.Agent3 Remote Control"
            textSize = 24f
        }

        val btnSource = Button(this).apply {
            text = "Start as Source (Control this device)"
        }

        val btnViewer = Button(this).apply {
            text = "Start as Viewer (Control other device)"
        }

        layout.addView(title)
        layout.addView(btnSource)
        layout.addView(btnViewer)

        setContentView(layout)
    }
}
