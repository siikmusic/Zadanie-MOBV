package com.example.zadaniemobv

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.zadaniemobv.model.DataUser
import com.example.zadaniemobv.model.User
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.sin
import java.net.URL
import kotlin.random.Random

class UserDetailFragment : Fragment(R.layout.fragment_user) {
    private var photoPrefix = "https://upload.mcomputing.eu/";
    // Utility function to resize bitmap
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
    private fun randomPointWithinRadius(latitude: Double, longitude: Double, radiusMeters: Double): Point {
        // Convert radius from meters to degrees (approximately)
        val radiusInDegrees = radiusMeters*2 / 111320

        // Generate random distance and angle
        val distance = Random.nextDouble() * radiusInDegrees
        val angle = Random.nextDouble() * 2 * PI

        // Calculate the coordinates
        val deltaLat = distance * cos(angle)
        val deltaLon = distance * sin(angle) / cos(Math.toRadians(latitude))

        // New coordinates
        val newLat = latitude + deltaLat
        val newLon = longitude + deltaLon
        val point = Point.fromLngLat(newLon,newLat)
        return point
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        val userDto = arguments?.getSerializable("user") as? DataUser

        if (userDto != null) {
            // Use the userDto data to populate your UI elements in the fragment
            val usernameTextView = view.findViewById<TextView>(R.id.username_profile)
            val userImageView = view.findViewById<ImageView>(R.id.imageView4)

            usernameTextView.text = userDto.username
            val lat = userDto.lat
            val lon = userDto.lon
            val radius = userDto.radius
            var photoUrl = ""
            var point = randomPointWithinRadius(lat,lon,radius)

            if(userDto.photo != ""){
                photoUrl = photoPrefix + userDto.photo
                Picasso.get()
                    .load(photoUrl)
                    .into(userImageView)
            } else{
                photoUrl = "https://www.amaraventures.in/assets/uploads/testimonial/user.png"
            }
            val mapView = view.findViewById<MapView>(R.id.mapView_profile)
            mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(point).zoom(14.0).build())
            Picasso.get()
                .load(photoUrl)
                .into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                        val resizedBitmap = resizeBitmap(bitmap, 70, 70) // Set your desired width and height
                        val circularBitmap = getCircularBitmap(resizedBitmap)
                        // Image loaded, now add it to the map style
                        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
                            // Add custom icon to the style
                            style.addImage("my-custom-icon", circularBitmap)
                            val circleManager = mapView.annotations.createCircleAnnotationManager()
                            val circleOptions = CircleAnnotationOptions()
                                .withPoint(point)
                                .withCircleRadius(radius)
                                .withCircleOpacity(0.2)
                                .withCircleColor("#000")
                                .withCircleStrokeWidth(2.0)
                                .withCircleStrokeColor("#ffffff")
                            circleManager.create(circleOptions)

                            // Add point (marker) annotation
                            val annotationManager = mapView.annotations.createPointAnnotationManager()
                            val pointAnnotationOptions = PointAnnotationOptions()
                                .withPoint(point)
                                .withIconImage("my-custom-icon") // Reference your custom icon
                            annotationManager.create(pointAnnotationOptions)

                        }
                    }

                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                        // Handle the failure
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        // Handle the prepare load
                    }
                })



        } else {
            // Handle the case where userDto is null or data is missing
        }

        return view
    }
}