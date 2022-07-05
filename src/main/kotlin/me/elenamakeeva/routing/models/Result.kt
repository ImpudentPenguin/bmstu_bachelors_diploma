package me.elenamakeeva.routing.models

data class Result(
    val routes: MutableList<Route> = mutableListOf(),
    var total: Double = 0.0
)