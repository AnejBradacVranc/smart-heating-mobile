package com.feri.smartheat.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.feri.smartheat.classes.MQTTClient
import com.hivemq.client.mqtt.datatypes.MqttQos

class HomeViewModel : ViewModel() {

    private val _buttonText = MutableLiveData("Connect")
    val buttonText: LiveData<String> = _buttonText

}
