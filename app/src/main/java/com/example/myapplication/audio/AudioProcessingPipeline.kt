package com.example.myapplication.audio

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Applies RMS normalisation and a simple noise gate in absence of RNNoise.
 */
class AudioProcessingPipeline(
    private val targetRms: Double = 0.15,
    private val noiseGateThreshold: Double = 0.02
) {

    fun process(input: ShortArray, length: Int): ShortArray {
        val buffer = input.copyOf(length)
        applyRmsNormalization(buffer, length)
        applyNoiseGate(buffer, length)
        return buffer
    }

    private fun applyRmsNormalization(buffer: ShortArray, length: Int) {
        var sum = 0.0
        for (i in 0 until length) {
            sum += buffer[i] * buffer[i]
        }
        val rms = sqrt(sum / length.coerceAtLeast(1)) / Short.MAX_VALUE
        if (rms <= 0.0) return
        val gain = targetRms / rms
        for (i in 0 until length) {
            val amplified = (buffer[i] * gain).toInt()
            buffer[i] = amplified.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun applyNoiseGate(buffer: ShortArray, length: Int) {
        val threshold = (noiseGateThreshold * Short.MAX_VALUE).toInt()
        for (i in 0 until length) {
            val sample = buffer[i].toInt()
            if (abs(sample) < threshold) {
                buffer[i] = 0
            }
        }
    }
}
