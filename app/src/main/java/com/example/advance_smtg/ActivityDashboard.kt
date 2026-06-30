package com.example.advance_smtg

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // --- Home ---
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // --- URL Checker ---
        findViewById<LinearLayout>(R.id.navUrlChecker).setOnClickListener {
            startActivity(Intent(this, UrlCheckerActivity::class.java))
        }

        // --- Quiz ---
        findViewById<LinearLayout>(R.id.navQuiz).setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java))
        }

        // --- SMS Log ---
        findViewById<LinearLayout>(R.id.navSmsLog).setOnClickListener {
            startActivity(Intent(this, SmsLogActivity::class.java))
        }
    }
}