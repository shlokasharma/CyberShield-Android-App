package com.example.advance_smtg

data class XaiResult(
    val isScam: Boolean,
    val riskScore: Int,
    val reasons: List<String>
)

class ScamDetector {

    fun analyzeWithXai(input: String): XaiResult {
        val reasons = mutableListOf<String>()
        var score = 0
        val lower = input.lowercase()

        // XAI Logic: Checking for specific red flags
        if (lower.contains("http://")) {
            score += 40
            reasons.add("Non-secure protocol (HTTP instead of HTTPS) detected.")
        }
        if (lower.contains(".xyz") || lower.contains(".tk") || lower.contains(".pw")) {
            score += 30
            reasons.add("Uses a low-reputation Top-Level Domain (TLD) often used for phishing.")
        }
        if (lower.contains("win") || lower.contains("gift") || lower.contains("prize")) {
            score += 20
            reasons.add("Contains 'Urgency/Reward' keywords typical of social engineering.")
        }
        if (lower.contains("bit.ly") || lower.contains("t.co")) {
            score += 10
            reasons.add("Uses a URL shortener to hide the final destination.")
        }

        return XaiResult(score > 30, score, reasons)
    }
}