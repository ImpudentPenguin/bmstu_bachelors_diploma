package me.elenamakeeva.routing.utils

import kotlin.math.atan2

object Coordinates {

    fun getDirection(x: Double, y: Double): String {
        val (centerX, centerY) = 55.558741 to 37.378847 // center in Moscow
        var angle = atan2(y - centerY, x -centerX)
        angle += Math.PI
        angle /= Math.PI / 4
        var halfQuarter = angle.toInt()
        halfQuarter %= 8

        val direction = when (halfQuarter) {
            0 -> "Север"
            4 -> "Юг"
            6 -> "Восток"
            2 -> "Запад"
            7 -> "Северо - восток"
            1 -> "Северо - запад"
            5 -> "Юго - восток"
            3 -> "Юго - запад"
            else -> "Не определено"
        }

        return direction
    }
}