package com.feri.smartheat.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.feri.smartheat.classes.DeviceRegistrationPayload
import com.feri.smartheat.classes.MQTTClient
import com.feri.smartheat.classes.Utils
import com.feri.smartheat.services.FirebaseMessagingService
import com.google.firebase.installations.FirebaseInstallations
import com.hivemq.client.mqtt.datatypes.MqttQos
import kotlinx.serialization.json.Json
import java.sql.Date
import java.time.LocalDateTime

// Data class to hold timestamp and value
data class TimestampedValue(
    val timestamp: Long,
    val value: Float
)

class SharedViewModel : ViewModel() {

    private val _distance = MutableLiveData<String>()
    private val _distanceHistory = MutableLiveData<List<TimestampedValue>>(emptyList())

    val distance: LiveData<String> = _distance
    val distanceHistory: LiveData<List<TimestampedValue>> = _distanceHistory

    // Humidity
    private val _humidity = MutableLiveData<String>()
    private val _humidityHistory = MutableLiveData<List<TimestampedValue>>(emptyList())

    val humidity: LiveData<String> = _humidity
    val humidityHistory: LiveData<List<TimestampedValue>> = _humidityHistory

    // Room Temperature
    private val _roomTemp = MutableLiveData<String>()
    private val _roomTempHistory = MutableLiveData<List<TimestampedValue>>(emptyList())

    val roomTemp: LiveData<String> = _roomTemp
    val roomTempHistory: LiveData<List<TimestampedValue>> = _roomTempHistory

    // Furnace Temperature
    private val _furnaceTemp = MutableLiveData<String>()
    private val _furnaceTempHistory = MutableLiveData<List<TimestampedValue>>(emptyList())

    val furnaceTemp: LiveData<String> = _furnaceTemp
    val furnaceTempHistory: LiveData<List<TimestampedValue>> = _furnaceTempHistory

    private val mqttClient = MQTTClient(
        serverURI = "192.168.1.148",
        port = 1883,
        clientID = "AndroidClient_${FirebaseInstallations.getInstance().id}"
    )

    private fun appendToHistory(currentList: MutableLiveData<List<TimestampedValue>>, newValue: String, historySize: Int = 200) {
        try {
            val floatValue = newValue.toFloat()
            val currentHistory = currentList.value ?: emptyList()
            if(currentHistory.size > historySize){
                currentList.postValue(listOf())
                return
            }

            val timestampedValue = TimestampedValue(
                timestamp = System.currentTimeMillis(),
                value = floatValue
            )
            val updatedHistory = currentHistory + timestampedValue
            currentList.postValue(updatedHistory)

        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }

    fun connectToBroker(deviceToken: String, criticalFuelLevel: Int) {
        mqttClient.connect(
            onSuccess = {
                try {

                    val registrationObj = DeviceRegistrationPayload(
                        token = deviceToken,
                        fuelCriticalPoint =  criticalFuelLevel,
                        timestamp = Utils.getCurrentDateTimeString()
                    )
                    mqttClient.publish("smart-heat/register-device", Json.encodeToString(registrationObj), MqttQos.EXACTLY_ONCE, onError = {
                        it.printStackTrace()
                    },
                        onSuccess = {

                            mqttClient.subscribe(
                                topic = "smart-heat/furnace-temp",
                                qos = MqttQos.AT_LEAST_ONCE,
                                onMessage = { _, payload ->
                                    _furnaceTemp.postValue(payload)
                                    appendToHistory(_furnaceTempHistory, payload, 1000)
                                }
                            )

                            mqttClient.subscribe(
                                topic = "smart-heat/room-temp",
                                qos = MqttQos.AT_LEAST_ONCE,
                                onMessage = { _, payload ->
                                    _roomTemp.postValue(payload)
                                    appendToHistory(_roomTempHistory, payload, 1000)
                                }
                            )

                            mqttClient.subscribe(
                                topic = "smart-heat/room-humidity",
                                qos = MqttQos.AT_LEAST_ONCE,
                                onMessage = { _, payload ->
                                    _humidity.postValue(payload)
                                    appendToHistory(_humidityHistory, payload, 1000)
                                }
                            )

                            mqttClient.subscribe(
                                topic = "smart-heat/distance",
                                qos = MqttQos.AT_LEAST_ONCE,
                                onMessage = { _, payload ->
                                    _distance.postValue(Utils.calculateRemainingFuel(payload.toInt(), criticalFuelLevel).toString() )
                                    appendToHistory(_distanceHistory, payload, 1000)
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


    // Optional: Add method to clear history if needed
    fun clearHistory() {
        _distanceHistory.postValue(emptyList())
        _humidityHistory.postValue(emptyList())
        _roomTempHistory.postValue(emptyList())
        _furnaceTempHistory.postValue(emptyList())
    }
}