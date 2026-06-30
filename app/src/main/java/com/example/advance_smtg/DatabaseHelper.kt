package com.example.advance_smtg

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "ScamGuard.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE scam_logs (id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT, content TEXT, reason TEXT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)")
        db?.execSQL("CREATE TABLE chatbot_logs (id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT, risk_score INTEGER, reasons TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS scam_logs")
        db?.execSQL("DROP TABLE IF EXISTS chatbot_logs")
        onCreate(db)
    }

    // Function 1: For SMS Scams
    fun logThreat(type: String, content: String, reason: String) {
        val values = ContentValues().apply {
            put("type", type)
            put("content", content)
            put("reason", reason)
        }
        this.writableDatabase.insert("scam_logs", null, values)
    }

    // Function 2: For Chatbot XAI
    fun logChatbotInquiry(url: String, score: Int, reasons: String) {
        val values = ContentValues().apply {
            put("url", url)
            put("risk_score", score)
            put("reasons", reasons)
        }
        this.writableDatabase.insert("chatbot_logs", null, values)
    }

    // Function 3: The "Super Risk" Calculator
    fun getTotalThreatCount(): Int {
        val db = this.readableDatabase
        val smsCursor = db.rawQuery("SELECT COUNT(*) FROM scam_logs", null)
        val chatCursor = db.rawQuery("SELECT COUNT(*) FROM chatbot_logs WHERE risk_score > 40", null)

        var count = 0
        if (smsCursor.moveToFirst()) count += smsCursor.getInt(0)
        if (chatCursor.moveToFirst()) count += chatCursor.getInt(0)

        smsCursor.close()
        chatCursor.close()
        return count
    }
    // Function 4: Count SMS scams only
    fun getSmsScamCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM scam_logs WHERE type = 'SMS'", null)
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    // Function 5: Count URL scans
    fun getUrlScannedCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM chatbot_logs", null)
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }
    data class SmsLog(val content: String, val reason: String, val timestamp: String)

    fun getAllSmsLogs(): List<SmsLog> {
        val logs = mutableListOf<SmsLog>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT content, reason, timestamp FROM scam_logs ORDER BY timestamp DESC", null
        )
        while (cursor.moveToNext()) {
            logs.add(
                SmsLog(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2)
                )
            )
        }
        cursor.close()
        return logs
    }
}