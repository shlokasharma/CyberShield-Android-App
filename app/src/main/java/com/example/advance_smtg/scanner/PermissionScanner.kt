package com.example.advance_smtg.scanner

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

data class RiskFeatures(
    var highRiskPermissions: Int = 0,
    var unknownSourceApps: Int = 0,
    var accessibilityAbuse: Int = 0,
    var recentScamAttempts: Int = 0
)

class PermissionScanner(private val context: Context) {

    fun analyzeDevice(): RiskFeatures {
        val features = RiskFeatures()

        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (app in installedApps) {

            // Skip system apps
            if ((app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
                continue
            }

            if ((app.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                continue
            }

            // Skip this app itself
            if (app.packageName == context.packageName) {
                continue
            }

            // Ignore core ecosystem packages (OEM + Android + Google)
            if (app.packageName.startsWith("com.samsung.") ||
                app.packageName.startsWith("com.sec.") ||
                app.packageName.startsWith("com.android.") ||
                app.packageName.startsWith("com.google.")
            ) {
                continue
            }

            val installer = pm.getInstallerPackageName(app.packageName)

            val isSideloaded =
                installer == null ||
                        installer == "com.android.shell"

            if (isSideloaded) {

                features.unknownSourceApps++
                Log.d("RISK_DEBUG", "Sideloaded App Detected: ${app.packageName}")

                val packageInfo = pm.getPackageInfo(
                    app.packageName,
                    PackageManager.GET_PERMISSIONS
                )

                val permissions = packageInfo.requestedPermissions ?: continue

                for (perm in permissions) {
                    if (perm.contains("READ_SMS") ||
                        perm.contains("RECEIVE_SMS") ||
                        perm.contains("SYSTEM_ALERT_WINDOW")
                    ) {
                        features.highRiskPermissions++
                        Log.d("RISK_DEBUG", "  └ Dangerous Permission: $perm")
                    }
                }
            }
        }

        Log.d("RISK_DEBUG", "HighRiskPermissions: ${features.highRiskPermissions}")
        Log.d("RISK_DEBUG", "UnknownApps: ${features.unknownSourceApps}")

        return features
    }
}