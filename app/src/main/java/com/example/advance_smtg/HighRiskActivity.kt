package com.example.advance_smtg

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HighRiskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            setBackgroundColor(Color.RED)
            orientation = LinearLayout.VERTICAL
            setPadding(50, 100, 50, 50)
        }
        val text = TextView(this).apply {
            text = "⚠️ SCAM DETECTED!\n\nAI Honeypot Active.\nAttacker misled with Fake OTP: ${(100000..999999).random()}"
            setTextColor(Color.WHITE)
            textSize = 24f
        }
        layout.addView(text)
        setContentView(layout)
    }
}