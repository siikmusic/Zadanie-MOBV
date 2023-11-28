package com.example.zadaniemobv.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zadaniemobv.api.DataRepository
import com.example.zadaniemobv.entities.GeofenceEntity
import com.example.zadaniemobv.model.User
import kotlinx.coroutines.launch

class ProfileViewModel(private val dataRepository: DataRepository) : ViewModel() {

    val sharingLocation = MutableLiveData<Boolean?>(null)
    private val _profileResult = MutableLiveData<String>()
    val profileResult: LiveData<String> get() = _profileResult

    private val _userResult = MutableLiveData<User?>()
    val userResult: LiveData<User?> get() = _userResult

    fun loadUser(uid: String) {
        viewModelScope.launch {
            val result = dataRepository.apiGetUser(uid)
            _profileResult.postValue(result.first ?: "")
            _userResult.postValue(result.second)
            Log.d("debugload",result.first.toString() + " " + result.second.toString())

        }
    }

    fun updateGeofence(lat: Double, lon: Double, radius: Double) {
        viewModelScope.launch {
            dataRepository.insertGeofence(GeofenceEntity(lat, lon, radius))
        }
    }

    fun removeGeofence() {
        viewModelScope.launch {
            dataRepository.removeGeofence()
        }
    }
}