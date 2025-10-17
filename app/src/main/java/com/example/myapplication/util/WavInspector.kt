package com.example.myapplication.util

import java.io.RandomAccessFile

data class WavMetadata(
    val sampleRate: Int,
    val channelCount: Int,
    val bitsPerSample: Int,
    val durationMillis: Long
)

object WavInspector {
    fun readMetadata(filePath: java.io.File): WavMetadata {
        RandomAccessFile(filePath, "r").use { raf ->
            val header = ByteArray(44)
            raf.readFully(header)
            val sampleRate = header.readIntLE(24)
            val bitsPerSample = header.readShortLE(34).toInt()
            val channels = header.readShortLE(22).toInt()
            val dataSize = header.readIntLE(40)
            val bytesPerSample = bitsPerSample / 8
            val frameSize = bytesPerSample * channels
            val totalFrames = if (frameSize == 0) 0 else dataSize / frameSize
            val durationSeconds = if (sampleRate == 0) 0.0 else totalFrames.toDouble() / sampleRate
            return WavMetadata(
                sampleRate = sampleRate,
                channelCount = channels,
                bitsPerSample = bitsPerSample,
                durationMillis = (durationSeconds * 1000).toLong()
            )
        }
    }

    private fun ByteArray.readIntLE(offset: Int): Int {
        return (this[offset].toInt() and 0xff) or
            ((this[offset + 1].toInt() and 0xff) shl 8) or
            ((this[offset + 2].toInt() and 0xff) shl 16) or
            ((this[offset + 3].toInt() and 0xff) shl 24)
    }

    private fun ByteArray.readShortLE(offset: Int): Short {
        return (((this[offset + 1].toInt() and 0xff) shl 8) or (this[offset].toInt() and 0xff)).toShort()
    }
}
