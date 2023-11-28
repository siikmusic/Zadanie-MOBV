package com.example.zadaniemobv.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zadaniemobv.api.DataRepository
import com.example.zadaniemobv.model.User
import kotlinx.coroutines.launch

class AuthViewModel(private val dataRepository: DataRepository) : ViewModel() {
    private val _registrationResult = MutableLiveData<String>()
    val registrationResult: LiveData<String> get() = _registrationResult
    val resetResult: LiveData<String> get() = _resetResult

    private val _loginResult = MutableLiveData<String>()
    private val _resetResult = MutableLiveData<String>()

    val loginResult: LiveData<String> get() = _loginResult

    private val _userResult = MutableLiveData<User?>()
    val userResult: LiveData<User?> get() = _userResult

    val username = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val repeat_password = MutableLiveData<String>()
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
            Log.d("debug", username.value + " " + password.value);

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
}