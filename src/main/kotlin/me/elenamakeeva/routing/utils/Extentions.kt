package me.elenamakeeva.routing.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

fun Double.kmToMinutes(): Long = ceil(this * 60.0).toLong()

fun LocalDateTime?.timeFormat(): String {
    return this?.toLocalTime()?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
}