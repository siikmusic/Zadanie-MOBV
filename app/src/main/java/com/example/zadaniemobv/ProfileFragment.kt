package com.example.zadaniemobv

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Switch
import androidx.appcompat.widget.SwitchCompat
import androidx.work.Constraints
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.zadaniemobv.api.DataRepository
import com.example.zadaniemobv.broadcast.GeofenceBroadcastReceiver
import com.example.zadaniemobv.databinding.FragmentFeedBinding
import com.example.zadaniemobv.databinding.FragmentProfileBinding
import com.example.zadaniemobv.viewModel.ProfileViewModel
import com.example.zadaniemobv.workers.MyWorker
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.TimeUnit

class ProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var binding: FragmentProfileBinding

    private val PERMISSIONS_REQUIRED = when {
        Build.VERSION.SDK_INT >= 33 -> { // android 13
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        Build.VERSION.SDK_INT >= 29 -> { // android 10
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

        else -> {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {

        }

    fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[ProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewModel
        }.also { bnd ->

            /*bnd.loadProfileBtn.setOnClickListener {
                val user = PreferenceData.getInstance().getUser(requireContext())
                user?.let {
                    viewModel.loadUser(it.id)
                }
            }

            bnd.logoutBtn.setOnClickListener {
                PreferenceData.getInstance().clearData(requireContext())
                it.findNavController().navigate(R.id.action_profile_intro)
            }

            viewModel.profileResult.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    Snackbar.make(
                        bnd.loadProfileBtn,
                        it,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }*/

            bnd.switch1.isChecked = PreferenceData.getInstance().getSharing(requireContext())
            bnd.switch1.setOnCheckedChangeListener { _, checked ->
                Log.d("ProfileFragment", "sharing je $checked")
                if (checked) {
                    turnOnSharing()
                } else {
                    turnOffSharing()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun turnOnSharing() {
        Log.d("ProfileFragment", "turnOnSharing")
        if (!hasPermissions(requireContext())) {
            binding.switch1.isChecked = false
            for (p in PERMISSIONS_REQUIRED) {
                requestPermissionLauncher.launch(p)
            }
            return
        }
        PreferenceData.getInstance().putSharing(requireContext(), true)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) {
            // Logika pre prácu s poslednou polohou
            Log.d("ProfileFragment", "poloha posledna ${it ?: "-"}")
            if (it == null) {
                Log.e("ProfileFragment", "poloha neznama geofence nevytvoreny")
            } else {
                setupGeofence(it)
            }
        }

    }

    private fun turnOffSharing() {
        Log.d("ProfileFragment", "turnOffSharing")
        PreferenceData.getInstance().putSharing(requireContext(), false)
        removeGeofence()
    }

    @SuppressLint("MissingPermission")
    private fun setupGeofence(location: Location) {

        val geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        val geofence = Geofence.Builder()
            .setRequestId("my-geofence")
            .setCircularRegion(location.latitude, location.longitude, 100f) // 100m polomer
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        val geofencePendingIntent =
            PendingIntent.getBroadcast(
                requireActivity(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                // Geofences boli úspešne pridané
                Log.d("ProfileFragment", "geofence vytvoreny")
                viewModel.updateGeofence(location.latitude, location.longitude, 100.0)
                runWorker()
            }
            addOnFailureListener {
                // Chyba pri pridaní geofences
                it.printStackTrace()
                binding.switch1.isChecked = false
                PreferenceData.getInstance().putSharing(requireContext(), false)
            }
        }

    }

    private fun removeGeofence() {
        Log.d("ProfileFragment", "geofence zruseny")
        val geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        geofencingClient.removeGeofences(listOf("my-geofence"))
        viewModel.removeGeofence()
        cancelWorker()
    }

    private fun runWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<MyWorker>(
            15, TimeUnit.MINUTES, // repeatInterval
            5, TimeUnit.MINUTES // flexInterval
        )
            .setConstraints(constraints)
            .addTag("myworker-tag")
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "myworker",
            ExistingPeriodicWorkPolicy.KEEP, // or REPLACE
            repeatingRequest
        )
    }

    private fun cancelWorker() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork("myworker")
    }
}
