package com.feri.smartheat.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.feri.smartheat.classes.DeviceRegistrationPayload
import com.feri.smartheat.classes.MQTTClient
import com.feri.smartheat.classes.Utils
import com.google.firebase.installations.FirebaseInstallations
import com.hivemq.client.mqtt.datatypes.MqttQos
import kotlinx.serialization.json.Json
import com.feri.smartheat.classes.Api.retrofit
import com.feri.smartheat.classes.ApiService
import com.feri.smartheat.classes.HistoryResponseData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// Data class to hold timestamp and value


class SharedViewModel : ViewModel() {

    val api: ApiService = retrofit.create(ApiService::class.java)

    private val _isConnected = MutableLiveData<Boolean>(false)
    val isConnected: LiveData<Boolean> = _isConnected

    private val _distance = MutableLiveData<String>()
    private val _fuelPercentageHistory = MutableLiveData<List<Float>>(emptyList())

    val distance: LiveData<String> = _distance
    val fuelPercentageHistory: LiveData<List<Float>> = _fuelPercentageHistory

    // Humidity
    private val _humidity = MutableLiveData<String>()
    private val _humidityHistory = MutableLiveData<List<Float>>(emptyList())

    val humidity: LiveData<String> = _humidity
    val humidityHistory: LiveData<List<Float>> = _humidityHistory

    // Room Temperature
    private val _roomTemp = MutableLiveData<String>()
    private val _roomTempHistory = MutableLiveData<List<Float>>(emptyList())

    val roomTemp: LiveData<String> = _roomTemp
    val roomTempHistory: LiveData<List<Float>> = _roomTempHistory

    // Furnace Temperature
    private val _furnaceTemp = MutableLiveData<String>()
    private val _furnaceTempHistory = MutableLiveData<List<Float>>(emptyList())

    val furnaceTemp: LiveData<String> = _furnaceTemp
    val furnaceTempHistory: LiveData<List<Float>> = _furnaceTempHistory

    private val mqttClient = MQTTClient(
        serverURI = "192.168.1.148",
        port = 1883,
        clientID = "AndroidClient_${FirebaseInstallations.getInstance().id}"
    )

    /*private fun appendToHistory(currentList: MutableLiveData<List<Float>>, newValue: String, historySize: Int = 200) {
        try {
            val floatValue = newValue.toFloat()
            val currentHistory = currentList.value ?: emptyList()
            if(currentHistory.size > historySize){
                currentList.postValue(listOf())
                return
            }

            val timestampedValue = floatValue

            val updatedHistory = currentHistory + timestampedValue
            currentList.postValue(updatedHistory)

        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }*/

    fun connectToBroker(deviceToken: String, criticalFuelLevel: Int) {
        mqttClient.connect(
            onSuccess = {
                try {

                    val registrationObj = DeviceRegistrationPayload(
                        token = deviceToken,
                        fuelCriticalPoint =  criticalFuelLevel,
                        timestamp = Utils.getCurrentDateTimeString()
                    )
                    mqttClient.publish("smart-heat/register-device", Json.encodeToString(registrationObj), MqttQos.EXACTLY_ONCE,
                        onError = {
                        it.printStackTrace()
                        _isConnected.postValue(false)
                    },
                        onSuccess = {
                            _isConnected.postValue(true)
                            mqttClient.subscribe(
                                topic = "smart-heat/furnace-temp",
                                qos = MqttQos.AT_LEAST_ONCE,
                                onMessage = { _, payload ->
                                    _furnaceTemp.postValue(payload)
                                   //appendToHistory(_furnaceTempHistory, payload, 1000)
                                }
                            )

                            mqttClient.subscribe(
                                topic = "smart-heat/room-temp",
                                qos = MqttQos.AT_LEAST_ONCE,
                                onMessage = { _, payload ->
                                    _roomTemp.postValue(payload)
                                    //appendToHistory(_roomTempHistory, payload, 1000)
                                }
                            )

                            mqttClient.subscribe(
                                topic = "smart-heat/room-humidity",
                                qos = MqttQos.AT_LEAST_ONCE,
                                onMessage = { _, payload ->
                                    _humidity.postValue(payload)
                                    //appendToHistory(_humidityHistory, payload, 1000)
                                }
                            )

                            mqttClient.subscribe(
                                topic = "smart-heat/distance",
                                qos = MqttQos.AT_LEAST_ONCE,
                                onMessage = { _, payload ->
                                    _distance.postValue(Utils.calculateRemainingFuel(payload.toInt(), criticalFuelLevel).toString() )
                                   // appendToHistory(_distanceHistory, payload, 1000)
                                }
                            )
                        }
                    )

                }catch (e: Exception){
                    e.printStackTrace()
                }
            },
            onError = { it.printStackTrace() }
        )
    }

    fun fetchHistory(){

        val call = api.getHistory()

        call.enqueue(object : Callback<HistoryResponseData>{
            override fun onResponse(
                call: Call<HistoryResponseData?>,
                response: Response<HistoryResponseData?>
            ) {
                if(response.isSuccessful){

                   val data = response.body()

                    if(data != null ){
                        _furnaceTempHistory.postValue(data.furnace_temp)
                        _humidityHistory.postValue(data.room_humidity)
                        _fuelPercentageHistory.postValue(data.fuel_percentage)
                        _roomTempHistory.postValue(data.room_temp)
                    }
                }
            }

            override fun onFailure(
                call: Call<HistoryResponseData?>,
                t: Throwable
            ) {
                t.printStackTrace()
            }

        })
    }

    fun disconnectFromBroker(){
        mqttClient.disconnect (onError = {
            Log.d("MQTT", "Could not disconnect")

        }, onComplete = {
            _isConnected.postValue(false)
            Log.d("MQTT", "Disconnected successfuly")
        })
    }


    // Optional: Add method to clear history if needed
    fun clearHistory() {
        _fuelPercentageHistory.postValue(emptyList())
        _humidityHistory.postValue(emptyList())
        _roomTempHistory.postValue(emptyList())
        _furnaceTempHistory.postValue(emptyList())
    }
}