package com.example.eva_02_ignacioquiero

import android.content.Intent                // Para navegar entre pantallas
import android.os.Bundle                     // Para crear la actividad
import android.os.Handler                    // Para programar tareas
import android.os.Looper                     // Para el hilo principal
import androidx.appcompat.app.AppCompatActivity  // Clase base para actividades

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 2500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()

        }, SPLASH_DELAY)  // Esperamos 2500 milisegundos
    }
}