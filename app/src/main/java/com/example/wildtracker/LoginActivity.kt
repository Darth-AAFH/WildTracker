@file:Suppress("DEPRECATION")

package com.example.wildtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.viewbinding.ViewBinding
import com.example.wildtracker.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.example.wildtracker.databinding.ActivityMainBinding


import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class LoginActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth


    companion object {
        var useremail: String = " "
        lateinit var providerSession: String
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    //private var ConfirmPassword by Delegates.notNull<String>()

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    //  private lateinit var etconfirmPassword: EditText
    private lateinit var lyTerms: LinearLayout

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_login)
        setContentView(binding.root)

        lyTerms = findViewById(R.id.lyTerms)
        lyTerms.visibility = View.INVISIBLE

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        //  etconfirmPassword = findViewById(R.id.etconfirmPassword)
        mAuth = FirebaseAuth.getInstance()
        //  etconfirmPassword.visibility = View.INVISIBLE

        manageButtonLogin()
        etEmail.doOnTextChanged { text, start, before, count -> manageButtonLogin() }
        etPassword.doOnTextChanged { text, start, before, count -> manageButtonLogin() }
        // etconfirmPassword.doOnTextChanged { text, start, before, count ->  manageButtonLogin() }


        //Configure google sign in
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("727481893022-adct709pnvj5tlihh532i6gjgm26thh6.apps.googleusercontent.com")

            //No pasa nada si esta en rojo
            .requestEmail() //Solo necesitamos el correo de la cuenta
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //Google Sign In Button
        binding.btSignGoogle.setOnClickListener {
            Log.d(TAG, "onCreate: begin Google SignIn")
            val intent = googleSignInClient.signInIntent
            startActivityIfNeeded(intent, RC_SIGN_IN)
        }


    }

    private fun checkUser() {
        //chack if user is logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            //user is LoggedIn
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            Log.d(TAG, "onActivityResult: Google SignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            } catch (e: Exception) {
                Log.d(TAG, "onActivityResult:${e.message}")

            }


        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin fireabase auth with google account")
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                //loggin success
                Log.d(TAG, "firebaseAuthWithGoogleAccount: LoggedIn")
                //Get logged in user
                val firebaseUser = firebaseAuth.currentUser
                //get user information
                useremail = firebaseUser?.email.toString()
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Email: ${useremail}")
                //check if user is new or existing
                if (authResult.additionalUserInfo!!.isNewUser) {
                    //user is new -- create account
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Account created...\n $useremail")
                    Toast.makeText(
                        this@LoginActivity,
                        "Account created...\n $useremail",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // existing user -LoggedIn
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing User...\n $useremail")
                    Toast.makeText(
                        this@LoginActivity,
                        "LoggedIn...\n $useremail",
                        Toast.LENGTH_SHORT
                    ).show()

                }
                register(true)
                //Start profile activity
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                //logginfailed()
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Loggin Failed due to ${e.message}")
                Toast.makeText(
                    this@LoginActivity,
                    "Loggin Failed due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()


            }

    }


    public override fun onStart() {
        super.onStart()
        //Comprueba si hay usuario con sesión iniciada
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) goHome(currentUser.email.toString(), currentUser.providerId)

    }

    override fun onBackPressed() {
        //Cuando pulse por atrasque se vea solo la pantalla de inicio de aplicación, no vuelva al inicio de sesion
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }


    private fun manageButtonLogin(){

        var tvLogin = findViewById<TextView>(R.id.tvLogin) //administrar el btn login
        email = etEmail.text.toString()
        password = etPassword.text.toString()
        //   ConfirmPassword = etconfirmPassword.text.toString()

        //validar datos del login
        if (TextUtils.isEmpty(password) || !ValidateEmail.isEmail(email)) {

            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            tvLogin.isEnabled = false
            //    etconfirmPassword.visibility = View.VISIBLE

        } else {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            tvLogin.isEnabled = true

        }

    }

    /*  private fun confirmPass(): Boolean {
           var etPassword: EditText = findViewById(R.id.etPassword)
           var etconfirmPassword: EditText = findViewById(R.id.etconfirmPassword)
          password = etPassword.text.toString()
          ConfirmPassword = etconfirmPassword.text.toString()
          return ConfirmPassword.equals(password)
      }

     */

    fun login(view: View) {
        loginUser()
    }

    private fun loginUser() {
        email = etEmail.text.toString()
        password = etPassword.text.toString()
        //  ConfirmPassword = etconfirmPassword.text.toString()
        var tvLogin = findViewById<TextView>(R.id.tvLogin) //administrar el btn login
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful)  goHome(email, "email")
                else {
                    if (lyTerms.visibility == View.INVISIBLE) {
                        lyTerms.visibility = View.VISIBLE
                    } else {
                        var cbAcept = findViewById<CheckBox>(R.id.cbAcept)
                        if (cbAcept.isChecked) register(false)
                    }
                }
            }

    }

    private fun goHome(email: String, provider: String) {

        useremail = email
        providerSession = provider

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun register(Google: Boolean) {
        if (Google) {
            val firebaseUser = firebaseAuth.currentUser
            email = firebaseUser!!.email.toString()
            password = "0000"
        } else {
            email = etEmail.text.toString()
            password = etPassword.text.toString()
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    var dateRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())
                    var dbRegister = FirebaseFirestore.getInstance()
                    dbRegister.collection("users").document(email).set(
                        hashMapOf(
                            "user" to email,
                            "dateRegister" to dateRegister
                        )
                    )

                    goHome(email, "email")
                } else Toast.makeText(this, "Error, algo ha ido mal :(", Toast.LENGTH_SHORT).show()
            }
    }

    fun goTerms(v: View){
        val intent = Intent(this, TermsActivity::class.java)
        startActivity(intent)
    }

    fun forgotPassword(view: View) {
        //startActivity(Intent(this, ForgotPasswordActivity::class.java))
        resetPassword()
    }

    private fun resetPassword(){
        var e = etEmail.text.toString()
        if (!TextUtils.isEmpty(e)) {
            mAuth.sendPasswordResetEmail(e)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) Toast.makeText(
                        this,
                        "Email Enviado a $e",
                        Toast.LENGTH_SHORT
                    ).show()
                    else Toast.makeText(
                        this,
                        "No se encontró el usuario con este correo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else Toast.makeText(this, "Indica un email", Toast.LENGTH_SHORT).show()
    }
}