package com.example.eva_02_ignacioquiero

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.eva_02_ignacioquiero.firebase.FirebaseHelper
import com.example.eva_02_ignacioquiero.models.Noticia
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgregarNoticiaActivity : AppCompatActivity() {

    private lateinit var tituloEditText: TextInputEditText
    private lateinit var bajadaEditText: TextInputEditText
    private lateinit var imagenUrlEditText: TextInputEditText
    private lateinit var cuerpoEditText: TextInputEditText
    private lateinit var fechaEditText: TextInputEditText
    private lateinit var cancelarButton: Button
    private lateinit var guardarButton: Button

    private val calendar = Calendar.getInstance()
    private val firebaseHelper = FirebaseHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_noticia)

        initializeViews()
        setupListeners()

        // Establecer fecha actual por defecto
        updateFechaDisplay()
    }

    private fun initializeViews() {
        tituloEditText = findViewById(R.id.tituloEditText)
        bajadaEditText = findViewById(R.id.bajadaEditText)
        imagenUrlEditText = findViewById(R.id.imagenUrlEditText)
        cuerpoEditText = findViewById(R.id.cuerpoEditText)
        fechaEditText = findViewById(R.id.fechaEditText)
        cancelarButton = findViewById(R.id.cancelarButton)
        guardarButton = findViewById(R.id.guardarButton)
    }

    private fun setupListeners() {
        cancelarButton.setOnClickListener {
            showCancelDialog()
        }

        guardarButton.setOnClickListener {
            handleGuardarNoticia()
        }

        fechaEditText.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateFechaDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun updateFechaDisplay() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
        fechaEditText.setText(dateFormat.format(calendar.time))
    }

    private fun handleGuardarNoticia() {
        val titulo = tituloEditText.text.toString().trim()
        val bajada = bajadaEditText.text.toString().trim()
        val imagenUrl = imagenUrlEditText.text.toString().trim()
        val cuerpo = cuerpoEditText.text.toString().trim()
        val fecha = fechaEditText.text.toString().trim()

        // Validaciones
        when {
            titulo.isEmpty() -> {
                showAlert("Error", "Por favor ingresa un título")
                tituloEditText.requestFocus()
            }
            titulo.length < 10 -> {
                showAlert("Error", "El título debe tener al menos 10 caracteres")
                tituloEditText.requestFocus()
            }
            bajada.isEmpty() -> {
                showAlert("Error", "Por favor ingresa una bajada")
                bajadaEditText.requestFocus()
            }
            bajada.length < 20 -> {
                showAlert("Error", "La bajada debe tener al menos 20 caracteres")
                bajadaEditText.requestFocus()
            }
            cuerpo.isEmpty() -> {
                showAlert("Error", "Por favor ingresa el cuerpo de la noticia")
                cuerpoEditText.requestFocus()
            }
            cuerpo.length < 50 -> {
                showAlert("Error", "El cuerpo debe tener al menos 50 caracteres")
                cuerpoEditText.requestFocus()
            }
            fecha.isEmpty() -> {
                showAlert("Error", "Por favor selecciona una fecha")
            }
            else -> {
                // Guardar noticia en Firebase
                saveNoticiaToFirebase(titulo, bajada, imagenUrl, cuerpo, fecha)
            }
        }
    }

    private fun saveNoticiaToFirebase(
        titulo: String,
        bajada: String,
        imagenUrl: String,
        cuerpo: String,
        fecha: String
    ) {
        // Mostrar loading
        setLoading(true)

        // Crear objeto Noticia (el ID se generará automáticamente en FirebaseHelper)
        val noticia = Noticia(
            id = "", // Se generará automáticamente
            titulo = titulo,
            bajada = bajada,
            imagenUrl = imagenUrl,
            cuerpo = cuerpo,
            fecha = fecha
        )

        firebaseHelper.saveNoticia(
            noticia = noticia,
            onSuccess = {
                setLoading(false)

                showSuccessDialog(
                    "¡Noticia publicada!",
                    "La noticia \"$titulo\" ha sido publicada exitosamente."
                )
            },
            onFailure = { errorMessage ->
                setLoading(false)
                showAlert("Error al guardar", errorMessage)
            }
        )
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            guardarButton.isEnabled = false
            guardarButton.text = "Guardando..."
            cancelarButton.isEnabled = false
        } else {
            guardarButton.isEnabled = true
            guardarButton.text = "Guardar"
            cancelarButton.isEnabled = true
        }
    }

    private fun showCancelDialog() {
        AlertDialog.Builder(this)
            .setTitle("¿Cancelar?")
            .setMessage("¿Estás seguro de que deseas cancelar? Los cambios no se guardarán.")
            .setPositiveButton("Sí, cancelar") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setNegativeButton("No, continuar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSuccessDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
                finish() // Volver a MainActivity
            }
            .setCancelable(false)
            .show()
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}