package com.example.lectorqr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        var btnIniciarSesion = findViewById<Button>(R.id.btnLogin)
        var email = findViewById<EditText>(R.id.edtTxtEmail)
        var password = findViewById<EditText>(R.id.edtTxtPassword)
        var btnOlvideContrasena = findViewById<Button>(R.id.btnPassword)
        var btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)

        btnRegistrarse.setOnClickListener {
            startActivity(Intent(this, CrearCuenta::class.java))
        }
        btnOlvideContrasena.setOnClickListener {
            startActivity(Intent(this, ResetPassword::class.java))
        }
        btnIniciarSesion.setOnClickListener {
            if (revisarCampos(email.text.toString(), password.text.toString())){
                auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString()).addOnCompleteListener {
                        task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, LeerQR::class.java))
                    } else {
                        val exception = task.exception
                        val errorMessage: String

                        if (exception is FirebaseAuthInvalidUserException) {
                            errorMessage = when (exception.errorCode) {
                                "ERROR_INVALID_EMAIL" -> "El formato de correo electrónico es incorrecto"
                                "ERROR_USER_NOT_FOUND" -> "No se encontró un usuario con este correo electrónico"
                                else -> "Error desconocido: " + exception.message.toString()
                            }
                        } else if (exception is FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "La contraseña es incorrecta"
                        } else if (exception is FirebaseNetworkException) {
                            errorMessage = "Error de red. Por favor, verifica tu conexión a internet!"
                        } else if (exception is FirebaseAuthUserCollisionException) {
                            errorMessage = "Este correo ya fue registrado!"
                        } else if (exception is FirebaseAuthInvalidUserException) {
                            errorMessage = "Este usuario ha sido deshabilitado"
                        } else if (exception is FirebaseTooManyRequestsException) {
                            errorMessage = "Hemos bloqueado todas las solicitudes de este dispositivo debido a actividad inusual. Intenta más tarde."
                        } else {
                            errorMessage = "Error desconocido: " + exception!!.message.toString()
                        }

                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

    private fun revisarCampos(email: String, password: String) : Boolean {
        if (email == "") {
            Toast.makeText(this, "El campo correo es requerido", Toast.LENGTH_LONG).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "El correo no es válido", Toast.LENGTH_LONG).show()
            return false
        }

        if (password == "") {
            Toast.makeText(this, "El campo contraseña es requerido", Toast.LENGTH_LONG).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        val fromSignup = intent.getBooleanExtra("FROM_SIGNUP", false)
        if (currentUser != null && !fromSignup) {
            startActivity(Intent(this, LeerQR::class.java))
            finish()
        }
        intent.removeExtra("FROM_SIGNUP")
    }
}