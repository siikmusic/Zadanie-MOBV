package com.example.zadaniemobv

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.Manifest
import android.util.Log
import android.widget.Switch
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import com.example.zadaniemobv.viewModel.ProfileViewModel

class ProfileFragment: Fragment(R.layout.fragment_profile) {
    private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    private lateinit var model: ProfileViewModel

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted){
            PreferenceData.getInstance().putSharing(requireContext(),true)
        }else{
            PreferenceData.getInstance().putSharing(requireContext(),false)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
    }
    // returns if Permissions are accepted
    fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.sharingLocation.postValue(PreferenceData.getInstance().getSharing(requireContext()))

        val switch: SwitchCompat = view.findViewById(R.id.switch1)

        model.sharingLocation.observe(viewLifecycleOwner) { sharing ->
            if (sharing == true) {
                PreferenceData.getInstance().putSharing(requireContext(),sharing)
                Log.d("ewf","" + sharing)
            } else {
                Log.d("ewf","" + sharing)

            }
        }


    }
}