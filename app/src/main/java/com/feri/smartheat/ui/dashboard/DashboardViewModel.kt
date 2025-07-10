package com.feri.smartheat.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    // Temperature history management
    private val _roomTempHistory = MutableLiveData<List<Float>>()
    private val _roomHumidityHistory = MutableLiveData<List<Float>>()
    private val _furnaceTempHistory = MutableLiveData<List<Float>>()
    private val _fuelLevelHistory = MutableLiveData<List<Float>>()

    val roomTempHistory: LiveData<List<Float>> = _roomTempHistory
    val roomHumidityHistory: LiveData<List<Float>> = _roomHumidityHistory
    val furnaceTempHistory: LiveData<List<Float>> = _furnaceTempHistory
    val fuelLevelHistory: LiveData<List<Float>> = _fuelLevelHistory

    private val tempHistory = mutableListOf<Float>()
    private val humHist = mutableListOf<Float>()
    private val fuelLvlHist = mutableListOf<Float>()
    private val furnaceTmpHist = mutableListOf<Float>()
    private val maxHistorySize = 200 // Keep last 20 readings

    fun addTemperatureReading(temperature: Float) {
        tempHistory.add(temperature)

        // Remove oldest reading if we exceed max size
        if (tempHistory.size > maxHistorySize) {
            tempHistory.removeAt(0)
        }

        // Update LiveData with new list
        _roomTempHistory.value = tempHistory.toList()
    }
    fun addHumidtyReading(temperature: Float) {
        tempHistory.add(temperature)

        // Remove oldest reading if we exceed max size
        if (tempHistory.size > maxHistorySize) {
            tempHistory.removeAt(0)
        }

        // Update LiveData with new list
        _roomTempHistory.value = tempHistory.toList()
    }
    fun addFurnaceTempReading(temperature: Float) {
        tempHistory.add(temperature)

        // Remove oldest reading if we exceed max size
        if (tempHistory.size > maxHistorySize) {
            tempHistory.removeAt(0)
        }

        // Update LiveData with new list
        _roomTempHistory.value = tempHistory.toList()
    }
    fun addFuelLevelReading(fuelLevel: Float) {
        fuelLvlHist.add(fuelLevel)
        _roomTempHistory.value = tempHistory.toList()
    }

}