package com.feri.smartheat.classes

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


data class  HistoryResponseData(
    val furnace_temp: List<Float>,
    val room_temp: List<Float>,
    val room_humidity: List<Float>,
    val fuel_percentage: List<Float>
)

data class  RegistrationResponseData(
    val success: Boolean,
    val message: String?
)

data class DeviceRegistrationPayload(
    val token: String,
    val fuel_critical_point: Int,
    val timestamp: String
)

interface ApiService {
    @GET("history")
    fun getHistory(): Call<HistoryResponseData>

    @POST("register-device")
    fun registerDevice(@Body request: DeviceRegistrationPayload): Call <RegistrationResponseData>
}

object Api{
    private const val BASE_URL ="http://192.168.1.148:8000/api/"

   val retrofit: Retrofit =
         Retrofit.Builder()
            .baseUrl(BASE_URL)
             .addConverterFactory(GsonConverterFactory.create())
            .build();
}



