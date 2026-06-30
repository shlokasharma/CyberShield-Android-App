package com.example.advance_smtg.risk

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class RiskClassifier(context: Context) {
    private var interpreter: Interpreter? = null

    // Scaling values from Python (StandardScaler)
    private val mean = floatArrayOf(7.12f, 4.45f, 2.51f, 0.49f, 0.51f)
    private val std = floatArrayOf(4.28f, 2.87f, 1.72f, 0.49f, 0.50f)

    init {
        val assetFileDescriptor = context.assets.openFd("risk_intelligence.tflite")
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, assetFileDescriptor.startOffset, assetFileDescriptor.declaredLength)
        interpreter = Interpreter(modelBuffer)
    }

    fun predictRisk(input: FloatArray): String {
        // Normalizing input: (x - mean) / std
        val scaledInput = FloatArray(5) { i -> (input[i] - mean[i]) / std[i] }
        val output = Array(1) { FloatArray(3) }

        interpreter?.run(arrayOf(scaledInput), output)

        val classes = arrayOf("High", "Low", "Medium")
        val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: 0
        return classes[maxIdx]
    }
}