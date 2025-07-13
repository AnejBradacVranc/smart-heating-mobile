package com.feri.smartheat.classes

import kotlinx.serialization.Serializable

@Serializable
data class DeviceRegistrationPayload(
    val token: String,
    val fuelCriticalPoint: Int,
    val timestamp: String
)

