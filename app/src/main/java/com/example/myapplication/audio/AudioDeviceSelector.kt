package com.example.myapplication.audio

import android.media.AudioDeviceInfo
import android.media.AudioManager

/**
 * Selects the most appropriate input device using the priority
 * wired microphones > bluetooth SCO > built-in microphones.
 */
class AudioDeviceSelector {

    private val devicePriority = listOf(
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_USB_HEADSET,
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
        AudioDeviceInfo.TYPE_TELEPHONY,
        AudioDeviceInfo.TYPE_BUILTIN_MIC
    )

    fun selectPreferredInput(audioManager: AudioManager): AudioDeviceInfo? {
        val inputs = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        return inputs.sortedBy { devicePriority.indexOf(it.type).let { index -> if (index == -1) Int.MAX_VALUE else index } }
            .firstOrNull()
    }
}
