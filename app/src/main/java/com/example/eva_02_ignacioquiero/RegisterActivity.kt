package com.example.eva_02_ignacioquiero

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var backToLoginTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.hide()

        initializeViews()

        setupListeners()
    }

    private fun initializeViews() {
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        registerButton = findViewById(R.id.registerButton)
        backToLoginTextView = findViewById(R.id.backToLoginTextView)
    }

    private fun setupListeners() {
        // Click en botón Registrarse
        registerButton.setOnClickListener {
            handleRegister()
        }


        backToLoginTextView.setOnClickListener {
            finish() // Cierra esta pantalla y vuelve al Login
        }
    }


    private fun handleRegister() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        // Validar datos
        when {
            name.isEmpty() -> {
                showAlert("Error", "Por favor ingresa tu nombre completo")
            }
            email.isEmpty() -> {
                showAlert("Error", "Por favor ingresa tu correo electrónico")
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showAlert("Error", "Por favor ingresa un correo válido")
            }
            password.isEmpty() -> {
                showAlert("Error", "Por favor ingresa una contraseña")
            }
            password.length < 6 -> {
                showAlert("Error", "La contraseña debe tener al menos 6 caracteres")
            }
            confirmPassword.isEmpty() -> {
                showAlert("Error", "Por favor confirma tu contraseña")
            }
            password != confirmPassword -> {
                showAlert("Error", "Las contraseñas no coinciden")
            }
            else -> {
                showAlert(
                    "Éxito",
                    "Cuenta creada exitosamente\n\nNombre: $name\nCorreo: $email\n\n¡Bienvenido!"
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
                // Si fue éxito, volver al login
                if (title == "Éxito") {
                    finish()
                }
            }
            .setCancelable(false)
            .show()
    }
}