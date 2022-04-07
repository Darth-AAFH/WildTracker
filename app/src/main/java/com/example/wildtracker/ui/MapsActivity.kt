package com.example.wildtracker.ui

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.wildtracker.LoginActivity
import com.example.wildtracker.MainActivity
import com.example.wildtracker.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawer: DrawerLayout
    private lateinit var map: GoogleMap
    var mActivity = MainActivity()

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
        initToolbar()
        initNavigationView()
    }
    private fun initToolbar() {
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawerlayout)
        val toggle = ActionBarDrawerToggle(this,drawer,toolbar,R.string.bar_title,
            R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }
    private fun initNavigationView() {

        var navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        var headerView: View = LayoutInflater.from(this)
            .inflate(R.layout.nav_header_main,navigationView,false)
        //Header para datos del usuario
        navigationView.removeHeaderView(headerView)
        //para actualizar los datos del header
        navigationView.addHeaderView(headerView)

        var tvUser: TextView = headerView.findViewById(R.id.tvUser)
        tvUser.text = LoginActivity.useremail

    }

    private fun createMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragggment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_maps -> callMapsActivity()
            R.id.logOut-> signOut()
        }

        drawer.closeDrawer(GravityCompat.START) // cerrar menu

        return true
    }

    private fun callMapsActivity() {
        val intent = Intent(this,MapsActivity::class.java)
        startActivity(intent)
    }


    private fun signOut() {

        LoginActivity.useremail = ""
        FirebaseAuth.getInstance().signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(this.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()


        //Cierra sesion y manda devuelta al login


        startActivity(Intent(this, LoginActivity::class.java))
    }
}



