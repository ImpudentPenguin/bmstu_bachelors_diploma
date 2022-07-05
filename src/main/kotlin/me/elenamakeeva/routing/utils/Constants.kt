package me.elenamakeeva.routing.utils

import java.time.format.DateTimeFormatter

typealias ID = Int
typealias CAR_INFO = Pair<String, String>
typealias DISTANCES = MutableList<MutableList<Double>>
typealias INVERSE_DISTANCES = MutableList<MutableList<Double>>

object Constants {
    const val MAX_CARS = 70
    var DEBUGGABLE = false
    var WITH_MODIFICATION = true
    val FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
}
