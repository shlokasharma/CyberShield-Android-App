package com.example.advance_smtg

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SmsLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = android.widget.ScrollView(this)
        scroll.setBackgroundColor(android.graphics.Color.parseColor("#F3F0F7"))

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        val title = TextView(this).apply {
            text = "📩 SMS Threat Log"
            textSize = 24f
            setTextColor(android.graphics.Color.parseColor("#2D2020"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 40)
        }
        layout.addView(title)

        // Load from DB
        val db = DatabaseHelper(this)
        val logs = db.getAllSmsLogs()

        if (logs.isEmpty()) {
            val empty = TextView(this).apply {
                text = "✅ No scam SMS detected yet!"
                textSize = 16f
                setTextColor(android.graphics.Color.parseColor("#888888"))
            }
            layout.addView(empty)
        } else {
            for (log in logs) {
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setBackgroundResource(R.drawable.card_red)
                    setPadding(40, 30, 40, 30)
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 30)
                    layoutParams = params
                }

                val content = TextView(this).apply {
                    text = "📨 ${log.content}"
                    textSize = 14f
                    setTextColor(android.graphics.Color.parseColor("#2D2020"))
                }

                val reason = TextView(this).apply {
                    text = "⚠️ ${log.reason}"
                    textSize = 12f
                    setTextColor(android.graphics.Color.parseColor("#666666"))
                    setPadding(0, 8, 0, 0)
                }

                val time = TextView(this).apply {
                    text = "🕐 ${log.timestamp}"
                    textSize = 11f
                    setTextColor(android.graphics.Color.parseColor("#999999"))
                    setPadding(0, 4, 0, 0)
                }

                card.addView(content)
                card.addView(reason)
                card.addView(time)
                layout.addView(card)
            }
        }

        scroll.addView(layout)
        setContentView(scroll)
    }
}