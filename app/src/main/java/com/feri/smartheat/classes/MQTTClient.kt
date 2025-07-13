package com.feri.smartheat.classes

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5RetainHandling

class MQTTClient(
    serverURI: String = "broker.mqtt.cool",
    port: Int = 1883,
    clientID: String = "AndroidClient_${System.currentTimeMillis()}"
) {

    private val client: Mqtt5AsyncClient = MqttClient.builder()
        .useMqttVersion5()
        .serverHost(serverURI)
        .serverPort(port)
        .identifier(clientID)
        .buildAsync()

    fun connect(
        onConnecting: () -> Unit = {},
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        onConnecting();
        client.connect()
            .whenComplete { _, ex -> if (ex != null) onError(ex) else onSuccess() }
    }

    fun disconnect(
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        client.disconnect()
            .whenComplete { _, ex -> if (ex != null) onError(ex) else onComplete() }
    }

    fun publish(
        topic: String,
        message: String,
        qos: MqttQos = MqttQos.AT_LEAST_ONCE,
        retain: Boolean = false,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        client.publishWith()
            .topic(topic)
            .payload(message.toByteArray())
            .qos(qos)
            .send()
            .whenComplete { _, ex -> if (ex != null) onError(ex) else onSuccess() }
    }

    fun subscribe(
        topic: String,
        qos: MqttQos = MqttQos.AT_LEAST_ONCE,
        onMessage: (topic: String, payload: String) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
            client.subscribeWith()
                .topicFilter(topic)
                .noLocal(true)
                .retainHandling(Mqtt5RetainHandling.DO_NOT_SEND)    // do not send retained messages
                .qos(qos)
                .callback { publish ->
                    val topicStr = publish.topic.toString()
                    val payload = publish.payloadAsBytes

                    onMessage(topicStr, String(payload))
                }
                .send()
                .whenComplete { _, ex -> if (ex != null) onError(ex) }
        }

    fun unsubscribe(
        topic: String,
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        client.unsubscribeWith()
            .topicFilter(topic)
            .send()
            .whenComplete { _, ex -> if (ex != null) onError(ex) else onComplete() }
    }
    }



