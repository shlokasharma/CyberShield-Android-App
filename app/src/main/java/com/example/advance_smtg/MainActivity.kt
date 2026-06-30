package com.example.advance_smtg

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.ImageButton
import android.graphics.Color
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.advance_smtg.scanner.PermissionScanner
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var timeText: TextView
    private lateinit var dateText: TextView
    private lateinit var riskScoreText: TextView
    private lateinit var smsCount: TextView
    private lateinit var quizScore: TextView
    private lateinit var urlCount: TextView
    private lateinit var manipulationCount: TextView

    private val handler = Handler(Looper.getMainLooper())

    private val clockRunnable = object : Runnable {
        override fun run() {
            val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            timeText.text = timeFormat.format(Date())
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- Request Permissions ---
        val permissions = arrayOf(
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.POST_NOTIFICATIONS
        )
        val needsPermission = permissions.any {
            checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (needsPermission) {
            requestPermissions(permissions, 101)
        }

        // --- Bind Views ---
        timeText = findViewById(R.id.timeText)
        dateText = findViewById(R.id.dateText)
        riskScoreText = findViewById(R.id.riskScoreText)
        smsCount = findViewById(R.id.smsCount)
        quizScore = findViewById(R.id.quizScore)
        urlCount = findViewById(R.id.urlCount)
        manipulationCount = findViewById(R.id.manipulationCount)

        // --- Set Date ---
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        dateText.text = dateFormat.format(Date())

        // --- Start Live Clock ---
        handler.post(clockRunnable)

        // --- Menu Button ---
        findViewById<ImageButton>(R.id.menuButton).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        // --- Load Data ---
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val db = DatabaseHelper(this)
        val detector = ScamDetector()
        val scanner = PermissionScanner(this)

        // --- XAI Test ---
        val testUrl = "http://abc.com"
        val xaiResult = detector.analyzeWithXai(testUrl)
        if (xaiResult.isScam) {
            val combinedReasons = xaiResult.reasons.joinToString(", ")
            db.logChatbotInquiry(testUrl, xaiResult.riskScore, combinedReasons)
            Log.d("XAI_DEBUG", "Scam logged: $combinedReasons")
        }

        // --- SMS Count ---
        val smsThreatCount = db.getSmsScamCount()
        smsCount.text = smsThreatCount.toString()

        // --- URL Count ---
        val urlScannedCount = db.getUrlScannedCount()
        urlCount.text = urlScannedCount.toString()

        // --- Manipulation Signals ---
        val riskFeatures = scanner.analyzeDevice()
        manipulationCount.text = riskFeatures.highRiskPermissions.toString()

        // --- Quiz Score ---
        val prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
        val savedScore = prefs.getInt("quiz_score_percent", 0)
        quizScore.text = "$savedScore%"

        // --- Overall Risk Level ---
        val totalThreats = db.getTotalThreatCount()
        val highRiskPerms = riskFeatures.highRiskPermissions

        val ruleRisk = when {
            totalThreats > 5 || highRiskPerms > 3 -> "HIGH"
            totalThreats > 2 || highRiskPerms > 1 -> "MEDIUM"
            else -> "LOW"
        }

        val mlRisk = when {
            savedScore < 40 -> "High"
            savedScore < 70 -> "Medium"
            else -> "Low"
        }

        riskScoreText.text = "Rule: $ruleRisk | ML: $mlRisk"

        val color = when (ruleRisk) {
            "HIGH" -> Color.RED
            "MEDIUM" -> Color.parseColor("#FF8C00")
            else -> Color.parseColor("#2D7A2D")
        }
        riskScoreText.setTextColor(color)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(clockRunnable)
    }
}