package me.elenamakeeva.routing.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class Request(
    override var id: Int, // внутренний id
    override val requestId: Int, // id заявки из БД
    override val placeX: Double,
    override val placeY: Double,
    override var weight: Double,
    override val direction: String,
    override var isAdded: Boolean = false,
    override val timeWindow: TimeWindow? = null,
    override var isVisited: Boolean = false,
    override var isVisit: MutableState<Boolean> = mutableStateOf(false),
    val isExpress: Boolean = false,
) : Node {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Request

        if (id != other.id) return false
        if (placeX != other.placeX) return false
        if (placeY != other.placeY) return false
        if (timeWindow != other.timeWindow) return false
        if (isExpress != other.isExpress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + placeX.hashCode()
        result = 31 * result + placeY.hashCode()
        result = 31 * result + (timeWindow?.hashCode() ?: 0)
        result = 31 * result + isExpress.hashCode()
        return result
    }
}