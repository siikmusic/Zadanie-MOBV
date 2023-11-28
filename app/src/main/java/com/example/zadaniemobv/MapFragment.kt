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
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.zadaniemobv.api.DataRepository
import com.example.zadaniemobv.entities.UserEntity
import com.example.zadaniemobv.viewModel.FeedViewModel
import com.example.zadaniemobv.viewModel.LocationViewModel
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.properties.generated.CirclePitchAlignment
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.annotation.annotations
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MapFragment : Fragment(R.layout.fragment_map) {
    private lateinit var binding: FragmentMapBinding
    private var selectedPoint: CircleAnnotation? = null
    private var lastLocation: Point? = null
    private lateinit var annotationManager: CircleAnnotationManager
    private lateinit var sharedViewModel: LocationViewModel
    private lateinit var viewModel: FeedViewModel
    private lateinit var currentUsers: List<UserEntity>
    private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    private var photoPrefix = "https://upload.mcomputing.eu/";

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
        val radiusInDegrees = radiusMeters * 2 / 111320

        // Generate random distance and angle
        val distance = Random.nextDouble() * radiusInDegrees
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
        // Load the map style only once
        binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
            // Create a single instance of the annotation manager
            val annotationManager = binding.mapView.annotations.createPointAnnotationManager()

            users.forEach { user ->
                var photoUrl = if (user.photo != "") photoPrefix + user.photo
                else "https://www.amaraventures.in/assets/uploads/testimonial/user.png"

                var point = randomPointWithinRadius(baseLat, baseLon, user.radius)

                Picasso.get()
                    .load(photoUrl)
                    .into(object : com.squareup.picasso.Target {
                        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                            val resizedBitmap = resizeBitmap(bitmap, 70, 70) // Set your desired width and height
                            val circularBitmap = getCircularBitmap(resizedBitmap)

                            // Use a unique ID for each user's custom icon
                            val iconId = "custom-icon-${user.uid}"

                            // Add custom icon to the style
                            style.addImage(iconId, circularBitmap)

                            // Add point (marker) annotation
                            val pointAnnotationOptions = PointAnnotationOptions()
                                .withPoint(point)
                                .withIconImage(iconId) // Reference the unique custom icon
                            annotationManager.create(pointAnnotationOptions)
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
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]
        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(DataRepository.getInstance(requireContext())) as T
            }
        })[FeedViewModel::class.java]
        viewModel.feed_items.observe(viewLifecycleOwner, Observer { users ->
            // This will be triggered whenever the feed_items data changes
            Log.d("USERSMap",""+users)
            if (users != null) {
                currentUsers = users
            }
        })
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }.also { bnd ->

            annotationManager = bnd.mapView.annotations.createCircleAnnotationManager()

            val hasPermission = hasPermissions(requireContext())
            onMapReady(hasPermission)

            bnd.myLocation.setOnClickListener {
                if (!hasPermissions(requireContext())) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                } else {
                    lastLocation?.let { refreshLocation(it) }
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
            Style.MAPBOX_STREETS
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
        Log.d("MapFragment", "poloha je $it")
        refreshLocation(it)
    }

    private fun refreshLocation(point: Point) {
        binding.mapView.getMapboxMap()
            .setCamera(CameraOptions.Builder().center(point).zoom(14.0).build())
        binding.mapView.gestures.focalPoint =
            binding.mapView.getMapboxMap().pixelForCoordinate(point)
        lastLocation = point
        addMarker(point)
        if (::currentUsers.isInitialized) {
            displayUsersOnMap(currentUsers, point.latitude(), point.longitude())
        }

    }
    fun createGeoJSONCircle(center: Point, radiusInKm: Double, points: Int = 64): FeatureCollection {
        val coords = center.latitude() to center.longitude()
        val km = radiusInKm

        val ret = mutableListOf<Point>()
        val distanceX = km / (111.320 * cos(coords.first * PI / 180))
        val distanceY = km / 110.574

        var theta: Double
        var x: Double
        var y: Double
        for (i in 0 until points) {
            theta = (i.toDouble() / points) * (2 * PI)
            x = distanceX * cos(theta)
            y = distanceY * sin(theta)
            ret.add(Point.fromLngLat(coords.second + x, coords.first + y))
        }
        ret.add(ret[0]) // Close the polygon

        // Create a GeoJSON FeatureCollection
        val polygon = Polygon.fromLngLats(listOf(ret))
        return FeatureCollection.fromFeatures(listOf(Feature.fromGeometry(polygon)))
    }

    private fun addMarker(point: Point) {
        val style = binding.mapView.getMapboxMap().getStyle() ?: return

        val sourceId = "circle-source"
        val featureCollection = createGeoJSONCircle(point, 1.0)

// Check if the source already exists
        val source = style.getSourceAs<GeoJsonSource>(sourceId)
        if (source != null) {
            // Update the source's feature collection
            source.featureCollection(featureCollection)
        } else {
            // Add the source to the map as it doesn't exist
            style.addSource(geoJsonSource(sourceId) {
                featureCollection(featureCollection)
            })

            // Create and add the layer to the map
            style.addLayer(fillLayer("circle-layer", sourceId) {
                fillColor("blue")
                fillOpacity(0.6)
            })
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
    }
}