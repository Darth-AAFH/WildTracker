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


import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignIn


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




        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //Google Sign In Button
        binding.btSignGoogle.setOnClickListener {
            signInGoogle()
        }


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            Log.d(TAG, "onActivityResult: Google SignIn intent result")
            //llamar a la funcion principal

            try {
                val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = accountTask.getResult(ApiException::class.java)

                if (account != null) {
                    email = account.email.toString()
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    //usar el proveedor para las auth de firebaseUser
                    mAuth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) goHome(email, "Google")
                        else Toast.makeText(
                            this,
                            "Error en la conexión con Google",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }

            } catch (e: Exception) {
                Log.d(TAG, "onActivityResult:${e.message}")
                Toast.makeText(this, "Error en la conexión con Google", Toast.LENGTH_SHORT).show()

            }


        }
    }

    fun callSignInGoogle(view: View) {
        signInGoogle()
    }

    private fun signInGoogle() {
        //Configure google sign in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            //No pasa nada si esta en rojo, es el id del usuario
            .requestEmail() //Solo necesitamos el correo de la cuenta
            .build()
        var googleSignInClient = GoogleSignIn.getClient(this, gso)
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
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
                        if (cbAcept.isChecked) register()
                    }
                }
            }

    }

    fun goHome(email: String, provider: String) {

        useremail = email
        providerSession = provider

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun register() {

        email = etEmail.text.toString()
        password = etPassword.text.toString()

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