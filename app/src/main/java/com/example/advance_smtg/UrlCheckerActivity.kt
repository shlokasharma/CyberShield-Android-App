package com.example.advance_smtg

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UrlCheckerActivity : AppCompatActivity() {

    private lateinit var inputText: EditText
    private lateinit var btnScan: Button
    private lateinit var btnClear: Button
    private lateinit var resultCard: LinearLayout
    private lateinit var xaiCard: LinearLayout
    private lateinit var safeCard: LinearLayout
    private lateinit var riskBadge: TextView
    private lateinit var riskScore: TextView
    private lateinit var reasonsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url_checker)

        // Bind views
        inputText = findViewById(R.id.inputText)
        btnScan = findViewById(R.id.btnScan)
        btnClear = findViewById(R.id.btnClear)
        resultCard = findViewById(R.id.resultCard)
        xaiCard = findViewById(R.id.xaiCard)
        safeCard = findViewById(R.id.safeCard)
        riskBadge = findViewById(R.id.riskBadge)
        riskScore = findViewById(R.id.riskScore)
        reasonsText = findViewById(R.id.reasonsText)

        // --- Scan Button ---
        btnScan.setOnClickListener {
            val input = inputText.text.toString().trim()
            if (input.isEmpty()) {
                inputText.error = "Please enter a URL or message!"
                return@setOnClickListener
            }
            scanInput(input)
        }

        // --- Clear Button ---
        btnClear.setOnClickListener {
            inputText.text.clear()
            resultCard.visibility = View.GONE
            xaiCard.visibility = View.GONE
            safeCard.visibility = View.GONE
            btnClear.visibility = View.GONE
        }
    }

    private fun scanInput(input: String) {
        val detector = ScamDetector()
        val db = DatabaseHelper(this)
        val result = detector.analyzeWithXai(input)

        // Save to database
        db.logChatbotInquiry(
            input,
            result.riskScore,
            result.reasons.joinToString(", ")
        )

        if (result.isScam) {
            // Show risk card
            safeCard.visibility = View.GONE
            resultCard.visibility = View.VISIBLE
            xaiCard.visibility = View.VISIBLE
            btnClear.visibility = View.VISIBLE

            // Set risk badge text + color
            val level = when {
                result.riskScore >= 70 -> {
                    resultCard.backgroundTintList = android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#F4A0A0"))
                    "🚨 HIGH RISK"
                }
                result.riskScore >= 40 -> {
                    resultCard.backgroundTintList = android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#FFD580"))
                    "⚠️ MEDIUM RISK"
                }
                else -> {
                    resultCard.backgroundTintList = android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#A8E0D0"))
                    "🟡 LOW RISK"
                }
            }

            riskBadge.text = level
            riskScore.text = "Risk Score: ${result.riskScore}/100"

            // Show reasons
            val reasonsFormatted = result.reasons
                .mapIndexed { i, r -> "${i + 1}. $r" }
                .joinToString("\n\n")
            reasonsText.text = reasonsFormatted

        } else {
            // Show safe card
            resultCard.visibility = View.GONE
            xaiCard.visibility = View.GONE
            safeCard.visibility = View.VISIBLE
            btnClear.visibility = View.VISIBLE
        }
    }
}