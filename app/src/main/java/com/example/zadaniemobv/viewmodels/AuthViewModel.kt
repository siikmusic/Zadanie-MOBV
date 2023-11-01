package com.example.zadaniemobv.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zadaniemobv.data.api.DataRepository
import com.example.zadaniemobv.data.api.model.User
import kotlinx.coroutines.launch

class AuthViewModel(private val dataRepository: DataRepository) : ViewModel() {
    private val _registrationResult = MutableLiveData<Pair<String, User?>>()
    private val _loginResult = MutableLiveData<Pair<String, User?>>()
    val username = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val repeat_password = MutableLiveData<String>()
    val registrationResult: LiveData<Pair<String, User?>> get() = _registrationResult
    val loginResult: LiveData<Pair<String, User?>> get() = _registrationResult

    fun registerUser() {
        viewModelScope.launch {
            val result = dataRepository.apiRegisterUser(username.toString()?:"", email.toString()?:"",password.toString()?:"",repeat_password.toString()?:"")

            _registrationResult.postValue(Pair(result.first ?: "", null))
        }
    }
    fun loginUser() {
        viewModelScope.launch {
            val result = dataRepository.apiLoginUser(username.toString()?:"", email.toString()?:"",password.toString()?:"")
            _loginResult.postValue(Pair(result.first ?: "", null))
            // _userResult.postValue(result.second)
        }
    }
}