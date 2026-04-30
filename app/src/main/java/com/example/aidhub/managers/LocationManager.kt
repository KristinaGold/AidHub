package com.example.aidhub.managers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.core.app.ActivityCompat
import com.example.aidhub.utilities.Constants
import com.google.android.gms.location.LocationServices
import java.util.Locale

class LocationManager(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context, Locale.getDefault())
    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: LocationManager? = null

        fun init(context: Context): LocationManager {
            return instance ?: synchronized(this) {
                instance ?: LocationManager(context).also { instance = it }
            }
        }

        fun getInstance(): LocationManager {
            return instance ?: throw IllegalStateException(
                "LocationManager must be initialized by calling init(context) before use.")
        }
    }

    fun getCurrentLocation(onSuccess: (Double, Double, String) -> Unit, onError: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                locationPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onError()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val address = getAddressFromCoords(it.latitude, it.longitude)
                onSuccess(it.latitude, it.longitude, address)
            } ?: onError()
        }.addOnFailureListener { onError() }
    }

    fun getCoordsFromAddress(address: String): Pair<Double, Double>? {
        return try {
            val addresses = geocoder.getFromLocationName(address, 1)
            if (!addresses.isNullOrEmpty()) {
                Pair(addresses[0].latitude, addresses[0].longitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun getAddressFromCoords(lat: Double, lng: Double): String {
        return try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            addresses?.get(0)?.getAddressLine(0) ?: Constants.UNKNOWN_LOCATION_KEY
        } catch (e: Exception) {
            Constants.UNKNOWN_LOCATION_KEY
        }
    }

    private fun formatDistance(meters: Float): String {
        return if (meters < 1000) {
            "${meters.toInt()} meters away"
        } else {
            val km = meters / 1000
            String.format("%.1f km away", km)
        }
    }


    fun calculateDistanceToUser(lat: Double, lng: Double, onSuccess: (String) -> Unit) {
        val targetLocation = Location("target").apply {
            latitude = lat
            longitude = lng
        }
        getCurrentLocation({ myLat, myLng, _ ->
            val myLocation = Location("me").apply {
                latitude = myLat
                longitude = myLng
            }
            val distanceInMeters = myLocation.distanceTo(targetLocation)
            onSuccess(formatDistance(distanceInMeters))

        }, {
        })
    }


    fun navigateToLocation(lat: Double, lng: Double) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setPackage("com.google.android.apps.maps")
        }

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$lat,$lng")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(genericIntent)
        }
    }
}