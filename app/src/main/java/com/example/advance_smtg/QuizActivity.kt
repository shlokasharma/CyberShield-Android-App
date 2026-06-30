package com.example.advance_smtg

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    private lateinit var questionText: TextView
    private lateinit var progressText: TextView
    private lateinit var scoreText: TextView
    private lateinit var xaiBox: View
    private lateinit var xaiText: TextView
    private lateinit var btnScam: Button
    private lateinit var btnLegit: Button
    private lateinit var btnNext: Button
    private lateinit var btnFinish: Button

    private lateinit var quizManager: QuizManager
    private var currentQuestion: QuizQuestion? = null
    private val questionsList = mutableListOf<QuizQuestion>()
    private var questionNumber = 0
    private var correct = 0
    private var total = 0
    private val maxQuestions = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Bind views
        questionText = findViewById(R.id.questionText)
        progressText = findViewById(R.id.progressText)
        scoreText = findViewById(R.id.scoreText)
        xaiBox = findViewById(R.id.xaiBox)
        xaiText = findViewById(R.id.xaiText)
        btnScam = findViewById(R.id.btnScam)
        btnLegit = findViewById(R.id.btnLegit)
        btnNext = findViewById(R.id.btnNext)
        btnFinish = findViewById(R.id.btnFinish)

        quizManager = QuizManager(this)

        // Load 5 random questions
        questionsList.addAll(quizManager.get5RandomQuestions())

        loadNextQuestion()

        // --- Answer Buttons ---
        btnScam.setOnClickListener {
            currentQuestion?.let { q -> checkAnswer(true, q) }
        }

        btnLegit.setOnClickListener {
            currentQuestion?.let { q -> checkAnswer(false, q) }
        }

        // --- Next Button ---
        btnNext.setOnClickListener {
            loadNextQuestion()
        }

        // --- Finish Button ---
        btnFinish.setOnClickListener {
            saveScoreAndGoBack()
        }
    }

    private fun loadNextQuestion() {
        if (questionNumber >= maxQuestions) {
            btnFinish.visibility = View.VISIBLE
            btnNext.visibility = View.GONE
            return
        }

        currentQuestion = questionsList[questionNumber]
        questionNumber++
        progressText.text = "Question $questionNumber / $maxQuestions"
        questionText.text = currentQuestion?.message

        // Reset UI
        xaiBox.visibility = View.GONE
        btnNext.visibility = View.GONE
        btnFinish.visibility = View.GONE
        btnScam.visibility = View.VISIBLE
        btnLegit.visibility = View.VISIBLE
        btnScam.isEnabled = true
        btnLegit.isEnabled = true

        // Reset button colors
        btnScam.backgroundTintList = android.content.res.ColorStateList
            .valueOf(Color.parseColor("#F4A0A0"))
        btnLegit.backgroundTintList = android.content.res.ColorStateList
            .valueOf(Color.parseColor("#A8E0D0"))
    }

    private fun checkAnswer(userSaidScam: Boolean, question: QuizQuestion) {
        total++
        val isCorrect = userSaidScam == question.isScam

        if (isCorrect) {
            correct++
            scoreText.text = "Score: $correct ✅"
        } else {
            scoreText.text = "Score: $correct ❌"
        }

        // Highlight correct/wrong buttons
        if (question.isScam) {
            btnScam.backgroundTintList = android.content.res.ColorStateList
                .valueOf(Color.parseColor("#4CAF50"))
            btnLegit.backgroundTintList = android.content.res.ColorStateList
                .valueOf(Color.parseColor("#F44336"))
        } else {
            btnLegit.backgroundTintList = android.content.res.ColorStateList
                .valueOf(Color.parseColor("#4CAF50"))
            btnScam.backgroundTintList = android.content.res.ColorStateList
                .valueOf(Color.parseColor("#F44336"))
        }

        // Show XAI explanation
        xaiText.text = question.xaiExplanation
        xaiBox.visibility = View.VISIBLE

        // Disable answer buttons
        btnScam.isEnabled = false
        btnLegit.isEnabled = false

        // Show next or finish
        if (questionNumber >= maxQuestions) {
            btnFinish.visibility = View.VISIBLE
        } else {
            btnNext.visibility = View.VISIBLE
        }
    }

    private fun saveScoreAndGoBack() {
        val percent = if (total > 0) (correct * 100) / total else 0

        val prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
        prefs.edit().putInt("quiz_score_percent", percent).apply()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}