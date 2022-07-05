package me.elenamakeeva.routing.models

import java.time.LocalDateTime

data class TimeWindow(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)