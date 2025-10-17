package com.example.myapplication.data.local

import androidx.room.TypeConverter
import com.example.myapplication.domain.model.TranscriptionStatus
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromStatus(status: TranscriptionStatus?): String? = status?.name

    @TypeConverter
    fun toStatus(name: String?): TranscriptionStatus? = name?.let { TranscriptionStatus.valueOf(it) }
}
