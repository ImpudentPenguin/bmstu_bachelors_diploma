package me.elenamakeeva.routing.models

import java.time.LocalDateTime

data class Route(
    val carId: String,
    val model: String,
    var distance: Double = 0.0,
    var date: LocalDateTime,
    var hasExpress: Boolean = false,
    var startTime: LocalDateTime = date,
    var path: MutableList<Node> = mutableListOf()
)