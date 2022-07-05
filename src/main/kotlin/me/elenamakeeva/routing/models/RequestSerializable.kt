package me.elenamakeeva.routing.models

@kotlinx.serialization.Serializable
data class RequestSerializable(
    val requestId: Int,
    val placeX: Double,
    val placeY: Double,
    val weight: Double,
    val timeWindow: TimeWindowSerializable? = null,
    val isExpress: Boolean
)

@kotlinx.serialization.Serializable
data class TimeWindowSerializable(
    val start: String,
    val end: String
)