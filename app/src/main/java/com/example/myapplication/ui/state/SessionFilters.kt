package com.example.myapplication.ui.state

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class SessionFilters(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val minDurationMinutes: Int? = null,
    val maxDurationMinutes: Int? = null
) {
    val startInstant: Instant?
        get() = startDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()

    val endInstant: Instant?
        get() = endDate?.plusDays(1)?.atStartOfDay(ZoneId.systemDefault())?.toInstant()

    val minDurationMillis: Long?
        get() = minDurationMinutes?.let { it * 60_000L }

    val maxDurationMillis: Long?
        get() = maxDurationMinutes?.let { it * 60_000L }
}
