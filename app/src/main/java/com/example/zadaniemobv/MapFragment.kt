package com.example.zadaniemobv

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.zadaniemobv.databinding.FragmentMapBinding
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import com.example.zadaniemobv.api.DataRepository
import com.example.zadaniemobv.entities.UserEntity
import com.example.zadaniemobv.model.DataUser
import com.example.zadaniemobv.viewModel.AuthViewModel
import com.example.zadaniemobv.viewModel.FeedViewModel
import com.example.zadaniemobv.viewModel.LocationViewModel
import com.example.zadaniemobv.viewModel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonPrimitive
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.squareup.picasso.Picasso
import kotlin.random.Random

class MapFragment : Fragment(R.layout.fragment_map) {
    private lateinit var binding: FragmentMapBinding
    private var selectedPoint: CircleAnnotation? = null
    private var lastLocation: Point? = null
    private lateinit var annotationManager: CircleAnnotationManager
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var sharedViewModel: LocationViewModel
    private lateinit var viewModel: FeedViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private var lastRefreshTime: Long = 0

    private lateinit var authViewModel: AuthViewModel

    private lateinit var currentUsers: List<UserEntity>
    private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    private var photoPrefix = "https://upload.mcomputing.eu/";
    private val handler = Handler(Looper.getMainLooper())
    private var delayedRunnable: Runnable? = null
    private var lastPoint: Point? = null
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                initLocationComponent()
                addLocationListeners()
            }
        }

    fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
        if (profileViewModel.sharingLocation.value != null && !profileViewModel.sharingLocation.value!!) return false;
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
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

    fun randomPointWithinRadius(latitude: Double, longitude: Double, radiusMeters: Double): Point {
        // Convert radius from meters to degrees (approximately)
        val radiusInDegrees = radiusMeters / 111320

        // Generate a biased random distance and angle
        // Bias the distance towards the edge of the circle
        val distance = radiusInDegrees * (0.75 + Random.nextDouble() * 0.25)
        val angle = Random.nextDouble() * 2 * Math.PI

        // Calculate the coordinates
        val deltaLat = distance * kotlin.math.cos(angle)
        val deltaLon = distance * kotlin.math.sin(angle) / kotlin.math.cos(Math.toRadians(latitude))

        // New coordinates
        val newLat = latitude + deltaLat
        val newLon = longitude + deltaLon

        return Point.fromLngLat(newLon, newLat)
    }

    fun displayUsersOnMap(users: List<UserEntity>, baseLat: Double, baseLon: Double) {
        val userIdToUserData = mutableMapOf<String, UserEntity>()
        for (user in users) {
            userIdToUserData[user.uid] = user
        }
        annotationManager.deleteAll()
        addMarker(Point.fromLngLat(baseLon, baseLat))

        pointAnnotationManager.deleteAll()

        binding.mapView.getMapboxMap().loadStyleUri(Style.DARK) { style ->

            users.forEach { user ->
                var photoUrl = if (user.photo != "") photoPrefix + user.photo
                else "https://www.amaraventures.in/assets/uploads/testimonial/user.png"

                var point = randomPointWithinRadius(baseLat, baseLon, user.radius)

                Picasso.get()
                    .load(photoUrl)
                    .into(object : com.squareup.picasso.Target {
                        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                            val resizedBitmap =
                                resizeBitmap(bitmap, 70, 70) // Set your desired width and height
                            val circularBitmap = getCircularBitmap(resizedBitmap)

                            // Use a unique ID for each user's custom icon
                            val iconId = "custom-icon-${user.uid}"

                            // Add custom icon to the style
                            style.addImage(iconId, circularBitmap)

                            // Add point (marker) annotation
                            val pointAnnotationOptions = PointAnnotationOptions()
                                .withPoint(point)
                                .withIconImage(iconId)
                                .withData(JsonPrimitive(user.uid)) // Reference the unique custom icon
                            val annotation = pointAnnotationManager.create(pointAnnotationOptions)

                        }

                        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                            // Handle the failure
                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                            // Handle the prepare load
                        }
                    })
            }
            pointAnnotationManager.apply {
                addClickListener(
                    OnPointAnnotationClickListener { pointAnnotation ->
                        if (userIdToUserData.isNotEmpty()) {
                            val id = pointAnnotation.id
                            val uid = pointAnnotation.getData().toString()
                                .substring(1, pointAnnotation.getData().toString().length - 1)
                            val userData = userIdToUserData[uid]
                            Log.d("DISPLAYUSERS", "" + id + uid + userIdToUserData)
                            //Log.d("DISPLAYUSERS", "$userData")
                            val bundle = Bundle()
                            val dataUser = userData?.let {
                                DataUser(
                                    it.name,
                                    it.photo,
                                    baseLat,
                                    baseLon,
                                    it.radius
                                )
                            }
                            bundle.putSerializable("user", dataUser)
                            findNavController().navigate(
                                R.id.action_mapFragment_to_profileUserFragment,
                                bundle
                            )
                        } else {
                            Snackbar.make(
                                binding.mapView,
                                "Cannot retrieve user data, wait a second",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        true // Return true to consume the event
                    }
                )
            }
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection available", Toast.LENGTH_SHORT).show()
        }
        binding = FragmentMapBinding.inflate(inflater, container, false)
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
        sharedViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[ProfileViewModel::class.java]

        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[FeedViewModel::class.java]
        viewModel.feed_items.observe(viewLifecycleOwner, Observer { users ->

            if (users != null) {
                val filteredUsers = users.filter { user ->
                    user.name != authViewModel.username.value
                }

                currentUsers = filteredUsers
            }
        })
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }.also { bnd ->

            annotationManager = bnd.mapView.annotations.createCircleAnnotationManager()
            pointAnnotationManager = bnd.mapView.annotations.createPointAnnotationManager()

            val hasPermission = hasPermissions(requireContext())
            if (!hasPermission) {
                Toast.makeText(requireContext(), "Location permission is required to show users on map", Toast.LENGTH_SHORT).show()
            }
            onMapReady(hasPermission)

            bnd.myLocation.setOnClickListener {
                if (!hasPermissions(requireContext())) {

                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )

                } else {
                    lastLocation?.let {
                        refreshLocation(it)
                    }
                    addLocationListeners()
                }
            }
        }

    }

    private fun onMapReady(enabled: Boolean) {
        binding.mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(14.3539484, 49.8001304))
                .zoom(2.0)
                .build()
        )
        binding.mapView.getMapboxMap().loadStyleUri(
            Style.DARK
        ) {
            if (enabled) {
                initLocationComponent()
                addLocationListeners()
            }
        }

        binding.mapView.getMapboxMap().addOnMapClickListener {
            if (hasPermissions(requireContext())) {
                onCameraTrackingDismissed()
            }
            true
        }
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = binding.mapView.location
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
        binding.mapView.location.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        binding.mapView.gestures.addOnMoveListener(onMoveListener)

    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        Log.d("Location Changed","LOCATION CHANGED")
        refreshLocation(it)
    }

    private fun refreshLocation(point: Point) {
        binding.mapView.getMapboxMap()
            .setCamera(CameraOptions.Builder().center(point).zoom(15.0).build())
        binding.mapView.gestures.focalPoint =
            binding.mapView.getMapboxMap().pixelForCoordinate(point)
        lastLocation = point
        val currentTime = System.currentTimeMillis()
        // Check if 10 seconds have passed since the last refresh
        if (currentTime - lastRefreshTime < 5000) {
            return // If not, simply return
        }
        Log.d("MapFragmentRefresh", "poloha je $point")


        if (hasPermissions(requireContext())) {
            if (::currentUsers.isInitialized) {
                displayUsersOnMap(currentUsers, point.latitude(), point.longitude())
            }
        }
        lastRefreshTime = currentTime
    }

    private fun scheduleDelayedRefresh(point: Point) {
        // Update the last point
        lastPoint = point

        // Create or update the runnable
        delayedRunnable = Runnable {
            lastPoint?.let { refreshLocation(it) }
        }

        // Remove any existing callbacks to avoid multiple executions
        handler.removeCallbacks(delayedRunnable!!)

        // Schedule the new execution
        handler.postDelayed(delayedRunnable!!, 10000)
    }

    private fun addMarker(point: Point) {

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
        binding.mapView.apply {
            location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            gestures.removeOnMoveListener(onMoveListener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.apply {
            location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            gestures.removeOnMoveListener(onMoveListener)
        }
        //runnable?.let { handler.removeCallbacks(it) }
        delayedRunnable?.let { handler.removeCallbacks(it) }

    }
}