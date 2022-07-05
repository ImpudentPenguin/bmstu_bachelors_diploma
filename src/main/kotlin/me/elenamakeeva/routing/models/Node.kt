package me.elenamakeeva.routing.models

import androidx.compose.runtime.MutableState

interface Node {
    var id: Int
    val requestId: Int
    val placeX: Double
    val placeY: Double
    var weight: Double
    val direction: String
    val timeWindow: TimeWindow?
    var isAdded: Boolean
    var isVisited: Boolean
    var isVisit: MutableState<Boolean>
}