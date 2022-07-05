package me.elenamakeeva.routing.models

import java.time.LocalDateTime

data class Car(
    val id: String,
    val model: String,
    var startTime: LocalDateTime,
    var currentTime: LocalDateTime,
    var distanceTravelled: Double = 0.0,
    var usedCapacity: Double = 0.0,
    var path: MutableList<Node>,
) {
    fun getLastVisited() = path.last().id

    fun resetTime() {
        currentTime = startTime
    }
}