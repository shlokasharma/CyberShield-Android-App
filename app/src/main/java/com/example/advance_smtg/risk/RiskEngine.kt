package com.example.advance_smtg.risk

import com.example.advance_smtg.scanner.RiskFeatures

class RiskEngine {

    fun calculateRisk(features: RiskFeatures): Int {
        var score = 0

        score += features.highRiskPermissions * 10
        score += features.unknownSourceApps * 15
        score += features.accessibilityAbuse * 20
        score += features.recentScamAttempts * 25

        if (score > 100) score = 100

        return score
    }

    fun getRiskLevel(score: Int): String {
        return when {
            score < 30 -> "LOW"
            score < 70 -> "MEDIUM"
            else -> "HIGH"
        }
    }
}