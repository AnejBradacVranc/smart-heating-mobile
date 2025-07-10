package com.feri.smartheat.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.feri.smartheat.classes.MQTTClient
import com.hivemq.client.mqtt.datatypes.MqttQos

class SharedViewModel : ViewModel() {
    private val _distance = MutableLiveData<String>()
    private val _distanceHistory = MutableLiveData<List<Float>>(emptyList())
    val distance: LiveData<String> = _distance
    val distanceHistory: LiveData<List<Float>> = _distanceHistory

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
        serverURI = "172.20.10.2",
        port = 1883,
        clientID = "AndroidClient_${System.currentTimeMillis()}"
    )

    private fun appendToHistory(currentList: MutableLiveData<List<Float>>, newValue: String) {
        try {
            val floatValue = newValue.toFloat()
            val currentHistory = currentList.value ?: emptyList()
            val updatedHistory = currentHistory + floatValue
            currentList.postValue(updatedHistory)
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
    }

    fun connectToBroker() {
        mqttClient.connect(
            onSuccess = {
                mqttClient.subscribe(
                    topic = "smart-heat/furnace-temp",
                    qos = MqttQos.AT_LEAST_ONCE,
                    onMessage = { _, payload ->
                        _furnaceTemp.postValue(payload)
                        appendToHistory(_furnaceTempHistory, payload)
                    }
                )

                mqttClient.subscribe(
                    topic = "smart-heat/room-temp",
                    qos = MqttQos.AT_LEAST_ONCE,
                    onMessage = { _, payload ->
                        _roomTemp.postValue(payload)
                        appendToHistory(_roomTempHistory, payload)
                    }
                )

                mqttClient.subscribe(
                    topic = "smart-heat/room-humidity",
                    qos = MqttQos.AT_LEAST_ONCE,
                    onMessage = { _, payload ->
                        _humidity.postValue(payload)
                        appendToHistory(_humidityHistory, payload)
                    }
                )

                mqttClient.subscribe(
                    topic = "smart-heat/distance",
                    qos = MqttQos.AT_LEAST_ONCE,
                    onMessage = { _, payload ->
                        _distance.postValue(payload)
                        appendToHistory(_distanceHistory, payload)
                    }
                )
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

    // Optional: Add method to limit history size
    fun limitHistorySize(maxSize: Int = 100) {
        _distanceHistory.value?.let { list ->
            if (list.size > maxSize) {
                _distanceHistory.postValue(list.takeLast(maxSize))
            }
        }
        _humidityHistory.value?.let { list ->
            if (list.size > maxSize) {
                _humidityHistory.postValue(list.takeLast(maxSize))
            }
        }
        _roomTempHistory.value?.let { list ->
            if (list.size > maxSize) {
                _roomTempHistory.postValue(list.takeLast(maxSize))
            }
        }
        _furnaceTempHistory.value?.let { list ->
            if (list.size > maxSize) {
                _furnaceTempHistory.postValue(list.takeLast(maxSize))
            }
        }
    }
}