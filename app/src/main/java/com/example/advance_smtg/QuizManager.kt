package com.example.advance_smtg

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

data class QuizQuestion(
    val message: String,
    val isScam: Boolean,
    val xaiExplanation: String
)

class QuizManager(private val context: Context) {

    private val allQuestions = mutableListOf<QuizQuestion>()

    init {
        loadDataset()
    }

    private fun loadDataset() {
        try {
            val inputStream = context.assets.open("spam.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val lines = reader.readLines().drop(1)

            for (line in lines) {
                val parts = parseCSVLine(line)
                if (parts.size >= 2) {
                    val label = parts[0].trim().lowercase()
                    val text = parts[1].trim().removeSurrounding("\"")
                    val isScam = label == "spam"

                    if (text.isNotBlank()) {
                        allQuestions.add(
                            QuizQuestion(
                                message = text,
                                isScam = isScam,
                                xaiExplanation = generateXAI(text, isScam)
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadFallbackQuestions()
        }
    }

    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString())
        return result
    }

    private fun generateXAI(text: String, isScam: Boolean): String {
        if (!isScam) return "✅ Legitimate: This message follows normal communication patterns."

        val reasons = mutableListOf<String>()

        if (text.contains(Regex("http://", RegexOption.IGNORE_CASE)))
            reasons.add("uses an insecure HTTP link")
        if (text.contains(Regex("\\b(urgent|immediately|expire|suspended|blocked)\\b", RegexOption.IGNORE_CASE)))
            reasons.add("uses urgency tactics to pressure you")
        if (text.contains(Regex("\\b(otp|pin|password|cvv|verify|confirm)\\b", RegexOption.IGNORE_CASE)))
            reasons.add("requests sensitive personal info")
        if (text.contains(Regex("\\b(txt|text|call|reply|stop|claim)\\b", RegexOption.IGNORE_CASE)))
            reasons.add("pressures you to take immediate action")
        if (text.contains(Regex("\\b(£|\\$|€|cash)\\b", RegexOption.IGNORE_CASE)))
            reasons.add("contains financial bait")

        val explanation = if (reasons.isEmpty())
            "shows patterns consistent with scam messages"
        else
            reasons.joinToString(", ")

        return "🚨 Scam: This message $explanation."
    }

    fun get5RandomQuestions(): List<QuizQuestion> {
        val spamQuestions = allQuestions.filter { it.isScam }.shuffled().take(3)
        val hamQuestions = allQuestions.filter { !it.isScam }.shuffled().take(2)
        return (spamQuestions + hamQuestions).shuffled()
    }

    fun getTotalQuestions() = allQuestions.size

    private fun loadFallbackQuestions() {
        val fallback = listOf(
            QuizQuestion(
                "URGENT: Your bank account suspended. Click http://bit.ly/verify now!",
                true,
                "🚨 Scam: Uses urgency, insecure HTTP link, and URL shortener."
            ),
            QuizQuestion(
                "Hi, your OTP for login is 482910. Valid for 5 minutes.",
                false,
                "✅ Legitimate: Standard OTP message with no suspicious links."
            ),
            QuizQuestion(
                "Your account has been blocked. Verify at http://secure-bank.tk now!",
                true,
                "🚨 Scam: Account blocking threat with suspicious domain."
            ),
            QuizQuestion(
                "Your order #45821 has been shipped. Track at amazon.com/orders",
                false,
                "✅ Legitimate: Uses official domain with no urgency."
            ),
            QuizQuestion(
                "IMPORTANT: Your SIM will be blocked. Call 09061743806 immediately.",
                true,
                "🚨 Scam: Fake SIM block threat with premium rate number."
            )
        )
        allQuestions.addAll(fallback)
    }
}