package com.example.zadaniemobv.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationViewModel : ViewModel() {
    private val _location = MutableLiveData<Pair<Double, Double>>()
    val location: LiveData<Pair<Double, Double>> = _location

    fun updateLocation(lat: Double, lon: Double) {
        _location.value = Pair(lat, lon)
    }
}
