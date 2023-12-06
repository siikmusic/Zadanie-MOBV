package com.example.zadaniemobv.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zadaniemobv.ProfileFragment
import com.example.zadaniemobv.api.DataRepository
import com.example.zadaniemobv.api.UploadApiService
import com.example.zadaniemobv.entities.GeofenceEntity
import com.example.zadaniemobv.model.User
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response

class ProfileViewModel(private val dataRepository: DataRepository) : ViewModel() {

    val sharingLocation = MutableLiveData<Boolean?>(false)
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
    fun uploadProfileImage(image: MultipartBody.Part): MutableLiveData<Response<UploadApiService.UploadImageResponse>?> {
        val liveData = MutableLiveData<Response<UploadApiService.UploadImageResponse>?>()
        Log.d("IIMAGED","uploading")
        viewModelScope.launch {
            val response = dataRepository.uploadProfileImage(image)
            liveData.postValue(response)
        }
        return liveData
    }
    fun deleteProfileImage(): MutableLiveData<Response<UploadApiService.UploadImageResponse>?> {
        val liveData = MutableLiveData<Response<UploadApiService.UploadImageResponse>?>()

        viewModelScope.launch {
            var response = dataRepository.deleteProfileImage()
            liveData.postValue(response)
        }
        return liveData
    }
}