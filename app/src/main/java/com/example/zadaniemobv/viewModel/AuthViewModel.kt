package com.example.zadaniemobv.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zadaniemobv.PreferenceData
import com.example.zadaniemobv.api.DataRepository
import com.example.zadaniemobv.model.User
import kotlinx.coroutines.launch

class AuthViewModel(private val dataRepository: DataRepository) : ViewModel() {
    private val _registrationResult = MutableLiveData<String?>()
    val registrationResult: MutableLiveData<String?> get() = _registrationResult
    val resetResult: MutableLiveData<String?> get() = _resetResult
    val changeResult: MutableLiveData<String?> get() = _changeResult

    private var _loginResult = MutableLiveData<String?>()
    private val _resetResult = MutableLiveData<String?>()
    private val _changeResult = MutableLiveData<String?>()

    val loginResult: MutableLiveData<String?> get() = _loginResult

    private val _userResult = MutableLiveData<User?>()
    val userResult: LiveData<User?> get() = _userResult

    var username = MutableLiveData<String?>()
    var email = MutableLiveData<String?>()
    var password = MutableLiveData<String?>()
    val changePassword = MutableLiveData<String?>()
    val repeat_password = MutableLiveData<String?>()
    val newPassword = MutableLiveData<String?>()

    fun registerUser() {
        viewModelScope.launch {
            Log.d("debug", username.value + " " + password.value + " " + email.value);

            val result = dataRepository.apiRegisterUser(
                username.value ?: "",
                email.value ?: "",
                password.value ?: ""
            )
            result.second?.let { Log.d("debug", it.username) };
            _registrationResult.postValue(result.first ?: "")
            _userResult.postValue(result.second)
        }
    }

    fun loginUser() {
        viewModelScope.launch {
            val result = dataRepository.apiLoginUser(username.value ?: "", password.value ?: "")
            _loginResult.postValue(result.first ?: "")
            _userResult.postValue(result.second)
        }
    }

    fun resetPassword() {
        viewModelScope.launch {
            val result = dataRepository.apiResetPassword(email.value?: "")
            _resetResult.postValue(result.first?: "")
        }
    }
    fun changePassword() {
        viewModelScope.launch {
            val result = dataRepository.apiChangePassword(changePassword.value?: "",newPassword.value?: "", repeat_password.value?:"")
            _changeResult.postValue(result.first?: "")
        }
    }
    fun logOutUser(){
        username.value = null
        password.value = null
        email.value = null
        changePassword.value = null
        newPassword.value = null
        repeat_password.value = null
        _loginResult.value = null
        _registrationResult.value = null
        _resetResult.value = null
        _changeResult.value = null
        _userResult.value = null
        viewModelScope.launch {
            dataRepository.logout()
        }
    }
}