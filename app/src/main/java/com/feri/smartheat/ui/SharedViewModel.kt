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
import com.feri.smartheat.classes.Api.retrofit
import com.feri.smartheat.classes.ApiService
import com.feri.smartheat.classes.HistoryResponseData
import com.feri.smartheat.classes.RegistrationResponseData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// Data class to hold timestamp and value


class SharedViewModel : ViewModel() {

    val api: ApiService = retrofit.create(ApiService::class.java)

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

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

        registerDevice(deviceToken, criticalFuelLevel, onSuccess = {
            _errorMessage.postValue(null)

            mqttClient.connect(
                onSuccess = {
                    _errorMessage.postValue(null)
                    try {

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
                                _distance.postValue(
                                    Utils.calculateRemainingFuel(
                                        payload.toInt(),
                                        criticalFuelLevel
                                    ).toString()
                                )
                                // appendToHistory(_distanceHistory, payload, 1000)
                            }
                        )
                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                },
                onError = {
                    it.printStackTrace()
                    _errorMessage.postValue(it.message)
                }
            )
        },
            onError = { error ->
                Log.d("ERROR", error)
                _errorMessage.postValue(error)
            })

    }

    fun fetchHistory(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val call = api.getHistory()

        call.enqueue(object : Callback<HistoryResponseData> {
            override fun onResponse(
                call: Call<HistoryResponseData>,
                response: Response<HistoryResponseData>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()

                    if (data != null) {
                        _furnaceTempHistory.postValue(data.furnace_temp)
                        _humidityHistory.postValue(data.room_humidity)
                        _fuelPercentageHistory.postValue(data.fuel_percentage)
                        _roomTempHistory.postValue(data.room_temp)

                        onSuccess() // ✅ explicitly call success
                    } else {
                        onError("Response body was null.")
                    }
                } else {
                    onError("Server returned error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<HistoryResponseData>, t: Throwable) {
                onError("Network failure: ${t.message}")
            }
        })
    }

    fun setErrorMessage(message: String){
        _errorMessage.postValue(message)
    }

    private fun registerDevice(
        token: String, fuelCriticalPoint: Int, onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {

        val call = api.registerDevice(
            DeviceRegistrationPayload(
                token = token,
                fuel_critical_point = fuelCriticalPoint,
                timestamp = Utils.getCurrentDateTimeString()
            )
        )

        call.enqueue(object : Callback<RegistrationResponseData> {
            override fun onResponse(
                call: Call<RegistrationResponseData?>,
                response: Response<RegistrationResponseData?>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("DATA", data.toString())

                    if (data != null) {
                        if (data.success)
                            onSuccess()
                        else {
                            onError("Error occured while registering new device: ${data.message}")
                        }
                    }
                    else
                        onError("No data in response")
                } else {
                    onError("Request to backend was not made successfully")
                }
            }

            override fun onFailure(
                call: Call<RegistrationResponseData?>,
                t: Throwable
            ) {
                onError("Error occurred when calling api endpoint. ${t.message}")
                t.printStackTrace()
            }

        })
    }

    fun disconnectFromBroker() {
        mqttClient.disconnect(onError = {
            Log.d("MQTT", "Could not disconnect")

        }, onComplete = {
            _isConnected.postValue(false)
            Log.d("MQTT", "Disconnected successfuly")
        })
    }

}