package com.feri.smartheat.classes

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Utils {

    fun calculateRemainingFuel(distance: Int, criticalFuelLevel: Int): Float {
        if (criticalFuelLevel == 0) return 0.0f
        return 1.0f - (distance.toFloat() / criticalFuelLevel)
    }
    fun getCurrentDateTimeString(): String{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        return current.format(formatter)
    }
}