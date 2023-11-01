package com.example.zadaniemobv.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zadaniemobv.api.DataRepository
import com.example.zadaniemobv.model.User
import kotlinx.coroutines.launch

class AuthViewModel(private val dataRepository: DataRepository) : ViewModel() {
    private val _registrationResult = MutableLiveData<Pair<String, User?>>()
    private val _loginResult = MutableLiveData<Pair<String, User?>>()

    val registrationResult: LiveData<Pair<String, User?>> get() = _registrationResult
    val loginResult: LiveData<Pair<String, User?>> get() = _loginResult

    val username = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val repeat_password = MutableLiveData<String>()
    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch {
            _registrationResult.postValue(dataRepository.apiRegisterUser(username, email, password))
        }
    }
    fun loginUser() {
        viewModelScope.launch {
            val result = dataRepository.apiLoginUser(username.value?:"", password.value?:"")
            _loginResult.postValue(result)
            //_userResult.postValue(result.second)
        }
    }
}