package com.feri.smartheat.classes

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET


data class  HistoryResponseData(
    val furnace_temp: List<Float>,
    val room_temp: List<Float>,
    val room_humidity: List<Float>,
    val fuel_percentage: List<Float>
)

interface ApiService {
    @GET("history")
    fun getHistory(): Call<HistoryResponseData>
}

object Api{
    private const val BASE_URL ="http://192.168.1.148/api/"

   val retrofit: Retrofit =
         Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build();
}



