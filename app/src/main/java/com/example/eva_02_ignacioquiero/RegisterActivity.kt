package com.example.eva_02_ignacioquiero

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.eva_02_ignacioquiero.firebase.FirebaseHelper
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var backToLoginTextView: TextView
    private lateinit var progressBar: ProgressBar

    private val firebaseHelper = FirebaseHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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
        // progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        registerButton.setOnClickListener {
            handleRegister()
        }

        backToLoginTextView.setOnClickListener {
            finish()
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
                // Registrar usuario en Firebase
                registerUserInFirebase(name, email, password)
            }
        }
    }

    private fun registerUserInFirebase(name: String, email: String, password: String) {
        // Mostrar loading
        setLoading(true)

        firebaseHelper.registerUser(
            email = email,
            password = password,
            onSuccess = { user ->
                setLoading(false)

                // Registro exitoso
                showSuccessDialog(
                    "¡Cuenta creada exitosamente!",
                    "Bienvenido $name\n\nTu cuenta ha sido creada con el correo:\n$email"
                )
            },
            onFailure = { errorMessage ->
                setLoading(false)
                showAlert("Error al registrar", errorMessage)
            }
        )
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            registerButton.isEnabled = false
            registerButton.text = "Registrando..."
            // progressBar.visibility = View.VISIBLE
        } else {
            registerButton.isEnabled = true
            registerButton.text = getString(R.string.register_button)
            // progressBar.visibility = View.GONE
        }
    }

    private fun showSuccessDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
                finish() // Volver al login
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