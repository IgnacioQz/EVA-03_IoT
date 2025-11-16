package com.example.eva_02_ignacioquiero

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DetalleNoticiaActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var imagenNoticia: ImageView
    private lateinit var fechaTextView: TextView
    private lateinit var tituloTextView: TextView
    private lateinit var bajadaTextView: TextView
    private lateinit var cuerpoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_noticia)

        supportActionBar?.hide()

        initializeViews()
        setupListeners()
        loadNoticiaData()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        imagenNoticia = findViewById(R.id.imagenNoticia)
        fechaTextView = findViewById(R.id.fechaTextView)
        tituloTextView = findViewById(R.id.tituloTextView)
        bajadaTextView = findViewById(R.id.bajadaTextView)
        cuerpoTextView = findViewById(R.id.cuerpoTextView)
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish() // Volver a MainActivity
        }
    }

    private fun loadNoticiaData() {
        // Obtener datos pasados desde MainActivity
        val titulo = intent.getStringExtra("TITULO") ?: ""
        val bajada = intent.getStringExtra("BAJADA") ?: ""
        val cuerpo = intent.getStringExtra("CUERPO") ?: ""
        val fecha = intent.getStringExtra("FECHA") ?: ""
        val imagenUrl = intent.getStringExtra("IMAGEN_URL") ?: ""

        // Validar que se recibieron datos
        if (titulo.isEmpty()) {
            Toast.makeText(this, "Error: No se recibieron datos de la noticia", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Asignar datos a las vistas
        tituloTextView.text = titulo
        bajadaTextView.text = bajada
        cuerpoTextView.text = cuerpo
        fechaTextView.text = fecha


    }

    // Método para manejar el botón "atrás" del sistema
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}