package com.example.lectorqr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.lectorqr.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ResetPassword : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityResetPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegresar.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        binding.btnReset.setOnClickListener {
            val email = binding.emailtoReset.text.toString()

            if (revisarCampo()) {
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(this, "Se ha enviado un correo para restablecer la contraseña", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Error desconocido: " + it.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
    }

    private fun revisarCampo() : Boolean {
        val email = binding.emailtoReset.text.toString()
        if (email == "") {
            Toast.makeText(this, "El campo correo es requerido", Toast.LENGTH_LONG).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "El correo no es válido", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

}