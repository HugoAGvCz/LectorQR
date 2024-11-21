package com.example.lectorqr

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.example.lectorqr.databinding.ActivityLeerQrBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator

class LeerQR : AppCompatActivity() {
    private lateinit var binding: ActivityLeerQrBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeerQrBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        var toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val imageView = findViewById<ImageView>(R.id.imgMuestraQR)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> imageView.setImageResource(R.drawable.logo_lector_dark)
            Configuration.UI_MODE_NIGHT_NO -> imageView.setImageResource(R.drawable.logo_lector_light)
        }


        binding.btnLeerQR.setOnClickListener {
            initScanner()
        }

        setSupportActionBar(toolbar)
    }

    private fun initScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Escanea el código QR de tu boleto electrónico")
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                // Si se cancela la lectura
            } else {
                // Aquí se puede hacer algo con el resultado
                val codigoQR = result.contents
                val db = FirebaseFirestore.getInstance()
                val codigosQRRef = db.collection("codigosQR")
                val query = codigosQRRef.whereEqualTo("codigo", codigoQR)
                query.get().addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // No se encontró el código QR en la base de datos
                        Toast.makeText(this, "El QR no es válido", Toast.LENGTH_LONG).show()
                    } else {
                        for (document in documents) {
                            val status = document.getString("status")
                            if (status == "Generado") {
                                document.reference.update("status", "Utilizado")
                                Toast.makeText(this, "Buen viaje!", Toast.LENGTH_LONG).show()
                            } else if (status == "Utilizado") {
                                Toast.makeText(this, "QR utilizado, genera un nuevo QR para ingresar.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                auth.signOut()
                startActivity(Intent(this, Login::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}