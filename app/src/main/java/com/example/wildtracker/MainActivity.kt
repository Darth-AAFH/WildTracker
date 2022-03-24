package com.example.wildtracker
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.example.wildtracker.LoginActivity.Companion.useremail

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(this, "Bienvenido $useremail", Toast.LENGTH_SHORT).show()

    }

    fun callSignOut(view: View){
        signOut()
    }
    private fun signOut(){
        useremail = ""
        FirebaseAuth.getInstance().signOut()
        //Cierra sesion y manda devuelta al login
        startActivity (Intent(this, LoginActivity::class.java))
    }
}