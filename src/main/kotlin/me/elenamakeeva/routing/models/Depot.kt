package me.elenamakeeva.routing.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class Depot(
    override var id: Int = 0,
    override val requestId: Int = -1,
    override val placeX: Double = 55.76,
    override val placeY: Double = 37.73,
    override var weight: Double = 0.0,
    override val direction: String = "Восток",
    override var isAdded: Boolean = false,
    override val timeWindow: TimeWindow? = null,
    override var isVisited: Boolean = false,
    override var isVisit: MutableState<Boolean> = mutableStateOf(false)
) : Node {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Depot

        if (id != other.id) return false
        if (requestId != other.requestId) return false
        if (timeWindow != other.timeWindow) return false
        if (isVisited != other.isVisited) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + requestId
        result = 31 * result + (timeWindow?.hashCode() ?: 0)
        result = 31 * result + isVisited.hashCode()
        return result
    }
}