package com.example.aidhub.fragments.mainNavFragments

import android.Manifest
import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.managers.LocationManager
import com.example.aidhub.R
import com.example.aidhub.managers.SettingsManager
import com.example.data.dataStractures.DialogType
import com.example.aidhub.fragments.base.BaseFragment
import com.example.data.dataStractures.Request
import com.example.aidhub.databinding.FragmentMapBinding
import com.example.aidhub.utilities.Constants
import com.example.aidhub.utilities.DialogHelper
import com.example.aidhub.viewModels.RequestViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : BaseFragment<FragmentMapBinding>(), OnMapReadyCallback {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMapBinding.inflate(inflater, container, false)

    override fun setupTopBar() {}

    private lateinit var mMap: GoogleMap
    private val requestViewModel: RequestViewModel by activityViewModels()
    private val locationManager = LocationManager.getInstance()
    private val settingsManager = SettingsManager.getInstance()
    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    private var normalIcon: BitmapDescriptor? = null
    private var urgentIcon: BitmapDescriptor? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableMyLocation()
            refreshRequests(500.0)
        } else {
            DialogHelper.showAlertDialog(
                requireContext(),
                DialogType.PERMISSION_LOCATION
            ) { buttonId, _ ->
                if (buttonId == Constants.BUTTON_POSITIVE_KEY) {
                    settingsManager.openAppSettings(requireActivity())
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        normalIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker)
        urgentIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker_urgent)
        observeRequests()
        radiusListener()
    }

    override fun onResume() {
        super.onResume()
        binding.toggleRadius.check(R.id.btn500m)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.setOnMarkerClickListener { marker ->
            val request = marker.tag as? Request
            request?.let {
                requestViewModel.setRequest(request)
                findNavController().navigate(R.id.requestPreviewFragment)
            }
            true
        }
        setDarkModeMap(googleMap)
        checkLocationPermission()
    }

    private fun observeRequests() {
        requestViewModel.requests.observe(viewLifecycleOwner) { requests ->
            displayRequestsOnMap(requests)
        }
    }

    private fun radiusListener() {
        binding.toggleRadius.check(R.id.btn500m)
        binding.toggleRadius.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val radius = when (checkedId) {
                    R.id.btn500m -> 500.0
                    R.id.btn1km -> 1000.0
                    R.id.btn2km -> 2000.0
                    else -> 500.0
                }
                refreshRequests(radius)
            }
        }
    }

    private fun setDarkModeMap(googleMap: GoogleMap) {
        val styleRes = if (settingsManager.isDarkModeEnabled()) R.raw.map_style_dark else null
        styleRes?.let {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), it))
        } ?: googleMap.setMapStyle(null)
    }

    private fun checkLocationPermission() {
        if (settingsManager.hasLocationPermission()) {
            enableMyLocation()
            refreshRequests(500.0)
        } else {
            requestPermissionLauncher.launch(locationPermission)
        }
    }


    private fun refreshRequests(radius: Double) {
        locationManager.getCurrentLocation({ lat, lng, _ ->
            requestViewModel.fetchNearbyRequests(
                AuthManager.getUid() ?: "", lat, lng, radius
            )
        }, {})
    }

    private fun enableMyLocation() {
        try {
            mMap.isMyLocationEnabled = true
            locationManager.getCurrentLocation(
                { lat, lng, _ ->
                    val currentLatLng = LatLng(lat, lng)
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
                    )
                },
                {
                    // Error handling without re-requesting permissions here
                })
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun displayRequestsOnMap(requests: List<Request?>) {
        if (!::mMap.isInitialized) return
        mMap.clear()
        for (request in requests) {
            if (request == null) continue
            val position = LatLng(request.latitude, request.longitude)
            val markerOptions = MarkerOptions()
                .position(position)
                .title(request.title)
                .snippet(request.tag)
                .icon(if (request.urgent) urgentIcon else normalIcon)

            val marker = mMap.addMarker(markerOptions)
            marker?.tag = request
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return try {
            val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
            val canvas = Canvas(bitmap)
            drawable.draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bitmap)
        } catch (e: Exception) {
            null
        }
    }
}
