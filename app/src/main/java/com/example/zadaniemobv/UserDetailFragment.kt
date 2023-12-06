package com.example.zadaniemobv

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.zadaniemobv.model.DataUser
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
import java.lang.Math.PI
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
    private fun randomPointWithinRadius(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double
    ): Point {
        // Convert radius from meters to degrees (approximately)
        val radiusInDegrees = radiusMeters * 2 / 111320

        // Generate random distance and angle
        val distance = Random.nextDouble() * radiusInDegrees
        val angle = Random.nextDouble() * 2 * PI

        // Calculate the coordinates
        val deltaLat = distance * kotlin.math.cos(angle)
        val deltaLon = distance * kotlin.math.sin(angle) / kotlin.math.cos(Math.toRadians(latitude))

        // New coordinates
        val newLat = latitude + deltaLat
        val newLon = longitude + deltaLon
        return Point.fromLngLat(newLon, newLat)
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
            } else{
                photoUrl = "https://www.amaraventures.in/assets/uploads/testimonial/user.png"
            }
            val mapView = view.findViewById<MapView>(R.id.mapView_profile)
            mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(point).zoom(14.0).build())
            Picasso.get()
                .load(photoUrl)
                .into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                        val resizedBitmap = resizeBitmap(bitmap, 70, 70)
                        val circularBitmap = getCircularBitmap(resizedBitmap)
                        val resizedBitmap2 = resizeBitmap(bitmap, 150, 150)
                        val circularBitmap2 = getCircularBitmap(resizedBitmap2)
                        // Image loaded, now add it to the map style
                        userImageView.setImageBitmap(circularBitmap2)
                        mapView.getMapboxMap().loadStyleUri(Style.DARK) { style ->
                            // Add custom icon to the style
                            style.addImage("custom-icon", circularBitmap)
                            val circleManager = mapView.annotations.createCircleAnnotationManager()
                            val circleOptions = CircleAnnotationOptions()
                                .withPoint(point)
                                .withCircleRadius(radius)
                                .withCircleOpacity(0.2)
                                .withCircleColor("#cfabff")
                                .withCircleStrokeWidth(2.0)
                                .withCircleStrokeColor("#ffffff")
                            circleManager.create(circleOptions)

                            // Add point (marker) annotation
                            val annotationManager = mapView.annotations.createPointAnnotationManager()
                            val pointAnnotationOptions = PointAnnotationOptions()
                                .withPoint(point)
                                .withIconImage("custom-icon") // Reference icon
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