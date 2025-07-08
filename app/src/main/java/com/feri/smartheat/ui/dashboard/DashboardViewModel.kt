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
    val roomTempHistory: LiveData<List<Float>> = _roomTempHistory

    private val tempHistory = mutableListOf<Float>()
    private val maxHistorySize = 20 // Keep last 20 readings

    fun addTemperatureReading(temperature: Float) {
        tempHistory.add(temperature)

        // Remove oldest reading if we exceed max size
        if (tempHistory.size > maxHistorySize) {
            tempHistory.removeAt(0)
        }

        // Update LiveData with new list
        _roomTempHistory.value = tempHistory.toList()
    }

    fun clearTemperatureHistory() {
        tempHistory.clear()
        _roomTempHistory.value = emptyList()
    }
}