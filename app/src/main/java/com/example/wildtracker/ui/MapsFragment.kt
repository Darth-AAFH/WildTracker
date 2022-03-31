package com.example.wildtracker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.wildtracker.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.common.collect.Maps
import kotlinx.coroutines.currentCoroutineContext

class MapsFragment : Fragment() {
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available. This callback is
         * triggered when the map is ready to be used. This is where we
         * can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia. If
         * Google Play services is not installed on the device, the user
         * will be prompted to install it inside the SupportMapFragment.
         * This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */

        val home = LatLng(20.669607, -103.388950)
        val park = LatLng(20.670489, -103.385834)

        googleMap.addMarker(
            MarkerOptions()
                .position(home)
                .title("Marcador en mi casa")

                .snippet("Population: 776733")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.location))
        )
        googleMap.addMarker(
            MarkerOptions()
                .position(park)
                .title("Parque cercano")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.parque))
        )

        val zoomLevel = 16.0f //This goes up to 21
        // googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(park, zoomLevel))


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }


}