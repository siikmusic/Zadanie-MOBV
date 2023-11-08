package com.example.zadaniemobv.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    val sharingLocation = MutableLiveData<Boolean?>(null)

}