package com.feri.smartheat.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.feri.smartheat.classes.MQTTClient
import com.hivemq.client.mqtt.datatypes.MqttQos

class SharedViewModel : ViewModel() {
    private val _distance = MutableLiveData<String>()
    val distance: LiveData<String> = _distance



    // Humidity
    private val _humidity = MutableLiveData<String>()
    val humidity: LiveData<String> = _humidity



    // Room Temperature
    private val _roomTemp = MutableLiveData<String>()
    val roomTemp: LiveData<String> = _roomTemp



    // Furnace Temperature
    private val _furnaceTemp = MutableLiveData<String>()
    val furnaceTemp: LiveData<String> = _furnaceTemp



    private val mqttClient = MQTTClient(
        serverURI = "172.20.10.2",
        port = 1883,
        clientID = "AndroidClient_${System.currentTimeMillis()}"
    )

    fun connectToBroker() {
        mqttClient.connect(
            onSuccess = {
                mqttClient.subscribe(
                    topic = "smart-heat/furnace-temp",
                    qos = MqttQos.AT_LEAST_ONCE,
                    onMessage = { _, payload -> _furnaceTemp.postValue(payload) }
                )

                mqttClient.subscribe(
                    topic = "smart-heat/room-temp",
                    qos = MqttQos.AT_LEAST_ONCE,
                    onMessage = { _, payload -> _roomTemp.postValue(payload) }
                )

                mqttClient.subscribe(
                    topic = "smart-heat/room-humidity",
                    qos = MqttQos.AT_LEAST_ONCE,
                    onMessage = { _, payload -> _humidity.postValue(payload) }
                )

                mqttClient.subscribe(
                    topic = "smart-heat/distance",
                    qos = MqttQos.AT_LEAST_ONCE,
                    onMessage = { _, payload -> _distance.postValue(payload) }
                )
            },
            onError = { it.printStackTrace() }
        )
    }
}
