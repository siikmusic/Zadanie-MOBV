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
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.work.Constraints
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.zadaniemobv.api.DataRepository
import com.example.zadaniemobv.broadcast.GeofenceBroadcastReceiver
import com.example.zadaniemobv.databinding.FragmentFeedBinding
import com.example.zadaniemobv.databinding.FragmentProfileBinding
import com.example.zadaniemobv.viewModel.AuthViewModel
import com.example.zadaniemobv.viewModel.LocationViewModel
import com.example.zadaniemobv.viewModel.ProfileViewModel
import com.example.zadaniemobv.workers.MyWorker
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.squareup.picasso.Picasso
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class ProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var binding: FragmentProfileBinding
    private lateinit var sharedViewModel: LocationViewModel
    private lateinit var authViewModel: AuthViewModel

    private var lastLocation: Point? = null
    private var selectedPoint: CircleAnnotation? = null
    private lateinit var annotationManager: CircleAnnotationManager
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
        if (!isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection available", Toast.LENGTH_SHORT).show()
        }
        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[ProfileViewModel::class.java]
        authViewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[AuthViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = PreferenceData.getInstance().getUser(requireContext())
        sharedViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]
        user?.let {
            viewModel.loadUser(user.uid)
        }
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewModel
        }.also { bnd ->
            bnd.switch1.isChecked = PreferenceData.getInstance().getSharing(requireContext())
            Log.d("IS CHECKED V BIND",""+bnd.switch1.isChecked)
            annotationManager = bnd.mapViewProfile.annotations.createCircleAnnotationManager()

            bnd.imageView4.setOnClickListener {
                showImageOptions()
            }
            var photoUrl = "https://www.amaraventures.in/assets/uploads/testimonial/user.png"
            val profileImage = bnd.imageView4

            // Observe the userResult LiveData
            viewModel.userResult.observe(viewLifecycleOwner) { user ->
                user?.let {
                    val photoPrefix = "https://upload.mcomputing.eu/"
                    if(user.photo!=""){
                        photoUrl = photoPrefix + user.photo
                    }
                    Picasso.get()
                        .load(photoUrl)
                        .into(object : com.squareup.picasso.Target {
                            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                                val resizedBitmap = resizeBitmap(bitmap, 200, 200)
                                val circularBitmap = getCircularBitmap(resizedBitmap)
                                profileImage.setImageBitmap(circularBitmap)

                            }

                            override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                                // Handle the failure
                            }

                            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                // Handle the prepare load
                            }
                        })
                }
            }
            val hasPermission = hasPermissions(requireContext())
            onMapReady(hasPermission)

            viewModel.sharingLocation.observe(viewLifecycleOwner){sharing ->
                if(sharing!=null){
                    if(sharing){
                        turnOnSharing()
                    } else{
                        turnOffSharing()
                    }
                }
            }

            bnd.button.setOnClickListener {
                view.findNavController().navigate(R.id.action_profileFragment_to_recoverPassword2Fragment)
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
        var geofencePendingIntent: PendingIntent? = null
        geofencePendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                requireActivity(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                requireActivity(),
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
            )
        }
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d("ProfileFragment", "geofence vytvoreny"+location.latitude+location.longitude)
                viewModel.updateGeofence(location.latitude, location.longitude, 100.0)
                runWorker()
            }
            addOnFailureListener {
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
    private fun showImageOptions() {
        val options = arrayOf("Upload New Image", "Remove Image")
        AlertDialog.Builder(context)
            .setTitle("Change Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGalleryForImage()
                    1 -> removeProfileImage()
                }
            }.show()
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun removeProfileImage() {
        viewModel.deleteProfileImage().observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.isSuccessful) {
                    val photoUrl = "https://www.amaraventures.in/assets/uploads/testimonial/user.png"

                    Picasso.get()
                        .load(photoUrl)
                        .into(object : com.squareup.picasso.Target {
                            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                                val resizedBitmap = resizeBitmap(bitmap, 200, 200)
                                val circularBitmap = getCircularBitmap(resizedBitmap)
                                binding.imageView4.setImageBitmap(circularBitmap)

                            }
                            override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                                // Handle the failure
                            }
                            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                // Handle the prepare load
                            }
                        })
                    Toast.makeText(requireContext(), "Successfully removed image", Toast.LENGTH_SHORT).show()

                } else{
                    Toast.makeText(requireContext(), "Could not remove image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            Log.d("IMAGE", selectedImageUri.toString());
            if (selectedImageUri != null) {
                uploadImage(selectedImageUri)
            }
        }
    }
    private fun uploadImage(imageUri: Uri) {
        val context = requireContext()

        // Convert Uri to File
        val file = FileUtil.from(context, imageUri)

        // Create RequestBody
        val requestFile = file.asRequestBody("image/jpg".toMediaTypeOrNull())

        // MultipartBody.Part is used to send also the actual file name
        val body = MultipartBody.Part.createFormData("image", file.name+"."+"jpg", requestFile)

        viewModel.uploadProfileImage(body).observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.isSuccessful) {
                    Log.d("IIMAGED",imageUri.toString())
                    Picasso.get()
                        .load(imageUri)
                        .into(object : com.squareup.picasso.Target {
                            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                                val resizedBitmap = resizeBitmap(bitmap, 200, 200)
                                val circularBitmap = getCircularBitmap(resizedBitmap)
                                binding.imageView4.setImageBitmap(circularBitmap)

                            }
                            override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                                // Handle the failure
                            }
                            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                // Handle the prepare load
                            }
                        })
                    Toast.makeText(requireContext(), "Successfully uploaded image", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(requireContext(), "Could not upload image", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    object FileUtil {
        fun from(context: Context, uri: Uri): File {
            // Create an input stream from the URI and a temporary file to write to
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_image")
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            return file
        }
    }
    companion object {
        private const val GALLERY_REQUEST_CODE = 1001
    }
    fun resizeBitmap(source: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(source, width, height, false)
    }

    // Utility function to make bitmap circular
    fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = Color.WHITE
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawOval(rectF, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }
    private fun onMapReady(enabled: Boolean) {
        binding.mapViewProfile.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(14.3539484, 49.8001304))
                .zoom(2.0)
                .build()
        )
        binding.mapViewProfile.getMapboxMap().loadStyleUri(
            Style.DARK
        ) {
            if (enabled) {
                initLocationComponent()
                addLocationListeners()
            }
        }

        binding.mapViewProfile.getMapboxMap().addOnMapClickListener {
            if (hasPermissions(requireContext())) {
                onCameraTrackingDismissed()
            }
            true
        }
    }
    private fun initLocationComponent() {
        val locationComponentPlugin = binding.mapViewProfile.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.pulsingEnabled = true
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener {
            Log.d("sharedViewModel", "poloha je $it")
            sharedViewModel.updateLocation(it.latitude(), it.longitude())
        }
    }

    private fun addLocationListeners() {
        binding.mapViewProfile.location.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        binding.mapViewProfile.gestures.addOnMoveListener(onMoveListener)

    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        refreshLocation(it)
    }

    private fun refreshLocation(point: Point) {
        Log.d("MapFragmentRefresh", "poloha je $point")

        binding.mapViewProfile.getMapboxMap()
            .setCamera(CameraOptions.Builder().center(point).zoom(15.0).build())
        binding.mapViewProfile.gestures.focalPoint =
            binding.mapViewProfile.getMapboxMap().pixelForCoordinate(point)
        lastLocation = point
        addMarker(point)
    }

    private fun addMarker(point: Point) {

        if (selectedPoint == null) {
            annotationManager.deleteAll()
            val pointAnnotationOptions = CircleAnnotationOptions()
                .withPoint(point)
                .withCircleRadius(100.0)
                .withCircleOpacity(0.2)
                .withCircleColor("#cfabff")
                .withCircleStrokeWidth(2.0)
                .withCircleStrokeColor("#ffffff")
            selectedPoint = annotationManager.create(pointAnnotationOptions)
        } else {
            selectedPoint?.let {
                it.point = point
                annotationManager.update(it)
            }
        }
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }


    private fun onCameraTrackingDismissed() {
        binding.mapViewProfile.apply {
            location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            gestures.removeOnMoveListener(onMoveListener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapViewProfile.apply {
            location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            gestures.removeOnMoveListener(onMoveListener)
        }
    }
}
