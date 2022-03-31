package com.example.wildtracker.ui

import android.content.ContentValues.TAG
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.example.wildtracker.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private var markers: MutableList<Marker> = mutableListOf<Marker>()
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setOnInfoWindowClickListener { markerToDelete ->
            Log.i(TAG, "OnWindowClickDelete")
            markers.remove(markerToDelete)
            markerToDelete.remove()

        }


        map.setOnMapLongClickListener { latLng ->
            showAlertDialog(latLng)


        }


        createMarker()
    }

    private fun showAlertDialog(latLng: LatLng) {
        var dialog = AlertDialog.Builder(this).setTitle("Crear un marcador").setMessage("Hola")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Ok") { dialog, id ->
                // User clicked OK button

            }
        val marker = map.addMarker(
            MarkerOptions().position(latLng).title("NewMarcador").snippet("A cool place")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.location))
        )
        markers.add(marker!!)


    }

    private fun createMarker() {
        val favoritePlace = LatLng(20.702609, -103.389246)
        val home = LatLng(20.669607, -103.388950)
        map.addMarker(MarkerOptions().position(favoritePlace).title("Here we are"))
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(favoritePlace, 18f),
            1000,
            null
        )

        map.addMarker(
            MarkerOptions()
                .position(home)
                .title("Marcador en mi casa")
                .snippet("Population: 776733")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.location))
        )


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        createMapFragment()
    }

    private fun createMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragggment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


}



