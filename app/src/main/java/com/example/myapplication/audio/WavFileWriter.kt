package com.example.myapplication.audio

import java.io.File
import java.io.RandomAccessFile

/**
 * Utility that writes PCM audio into a WAV container using RandomAccessFile.
 */
class WavFileWriter(
    private val outputFile: File,
    private val sampleRate: Int,
    private val channelCount: Int,
    private val bitsPerSample: Int
) {

    private var raf: RandomAccessFile? = null
    private var dataSize: Int = 0

    fun open() {
        raf = RandomAccessFile(outputFile, "rw").apply {
            setLength(0)
            write(ByteArray(44)) // placeholder header
        }
    }

    fun write(data: ByteArray, length: Int) {
        val file = raf ?: throw IllegalStateException("Writer not opened")
        file.write(data, 0, length)
        dataSize += length
    }

    fun close() {
        val file = raf ?: return
        file.seek(0)
        val byteRate = sampleRate * channelCount * bitsPerSample / 8
        val blockAlign = (channelCount * bitsPerSample / 8).toShort()
        val header = ByteArray(44)
        val totalDataLen = dataSize + 36

        header.writeString("RIFF", 0)
        header.writeIntLE(totalDataLen, 4)
        header.writeString("WAVE", 8)
        header.writeString("fmt ", 12)
        header.writeIntLE(16, 16) // Subchunk1Size for PCM
        header.writeShortLE(1, 20) // Audio format = PCM
        header.writeShortLE(channelCount.toShort(), 22)
        header.writeIntLE(sampleRate, 24)
        header.writeIntLE(byteRate, 28)
        header.writeShortLE(blockAlign, 32)
        header.writeShortLE(bitsPerSample.toShort(), 34)
        header.writeString("data", 36)
        header.writeIntLE(dataSize, 40)

        file.write(header)
        file.close()
        raf = null
    }

    private fun ByteArray.writeString(value: String, offset: Int) {
        value.toByteArray().copyInto(this, offset)
    }

    private fun ByteArray.writeIntLE(value: Int, offset: Int) {
        this[offset] = (value and 0xff).toByte()
        this[offset + 1] = (value shr 8 and 0xff).toByte()
        this[offset + 2] = (value shr 16 and 0xff).toByte()
        this[offset + 3] = (value shr 24 and 0xff).toByte()
    }

    private fun ByteArray.writeShortLE(value: Short, offset: Int) {
        this[offset] = (value.toInt() and 0xff).toByte()
        this[offset + 1] = (value.toInt() shr 8 and 0xff).toByte()
    }
}
