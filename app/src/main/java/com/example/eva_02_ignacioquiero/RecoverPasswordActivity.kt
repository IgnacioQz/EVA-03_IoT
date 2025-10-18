package com.example.eva_02_ignacioquiero

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class RecoverPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var sendButton: Button
    private lateinit var backToLoginTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        supportActionBar?.hide()

        initializeViews()

        setupListeners()
    }


    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        sendButton = findViewById(R.id.sendButton)
        backToLoginTextView = findViewById(R.id.backToLoginTextView)
    }

    private fun setupListeners() {
        sendButton.setOnClickListener {
            handleRecoverPassword()
        }

        backToLoginTextView.setOnClickListener {
            finish()
        }
    }


    private fun handleRecoverPassword() {
        val email = emailEditText.text.toString().trim()

        when {
            email.isEmpty() -> {
                showAlert("Error", "Por favor ingresa tu correo electrónico")
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showAlert("Error", "Por favor ingresa un correo válido")
            }
            else -> {
                showAlert(
                    "Correo Enviado",
                    "Se ha enviado un enlace de recuperación a:\n\n$email\n\nPor favor revisa tu bandeja de entrada."
                )
            }
        }
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()

                if (title == "Correo Enviado") {
                    finish()
                }
            }
            .setCancelable(false)
            .show()
    }
}