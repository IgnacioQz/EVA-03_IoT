package com.example.eva_02_ignacioquiero

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.eva_02_ignacioquiero.firebase.FirebaseHelper
import com.google.android.material.textfield.TextInputEditText
import kotlin.random.Random

class RecoverPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var sendButton: Button
    private lateinit var backToLoginTextView: TextView

    private val firebaseHelper = FirebaseHelper()

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
                // Verificar si el usuario existe y generar nueva contraseña
                verifyUserAndResetPassword(email)
            }
        }
    }

    private fun verifyUserAndResetPassword(email: String) {
        // Mostrar loading
        setLoading(true)

        // Verificar si el usuario existe intentando obtener información
        firebaseHelper.checkUserExists(
            email = email,
            onExists = { exists ->
                if (exists) {
                    // Usuario existe - generar nueva contraseña
                    val newPassword = generateRandomPassword()

                    // Actualizar la contraseña en Firebase
                    firebaseHelper.resetPasswordForUser(
                        email = email,
                        newPassword = newPassword,
                        onSuccess = {
                            setLoading(false)
                            showPasswordDialog(email, newPassword)
                        },
                        onFailure = { errorMessage ->
                            setLoading(false)
                            showAlert("Error", errorMessage)
                        }
                    )
                } else {
                    setLoading(false)
                    showAlert(
                        "Error",
                        "No existe una cuenta registrada con el correo:\n\n$email\n\n" +
                                "Por favor verifica el correo o regístrate."
                    )
                }
            },
            onFailure = { errorMessage ->
                setLoading(false)
                showAlert("Error", errorMessage)
            }
        )
    }

    /**
     * Genera una contraseña aleatoria de 8 caracteres
     * Formato: 2 mayúsculas + 4 minúsculas + 2 números
     */
    private fun generateRandomPassword(): String {
        val upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowerCase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"

        val password = StringBuilder()

        // 2 mayúsculas
        repeat(2) {
            password.append(upperCase[Random.nextInt(upperCase.length)])
        }

        // 4 minúsculas
        repeat(4) {
            password.append(lowerCase[Random.nextInt(lowerCase.length)])
        }

        // 2 números
        repeat(2) {
            password.append(numbers[Random.nextInt(numbers.length)])
        }

        // Mezclar los caracteres para que no sigan un patrón
        return password.toString().toList().shuffled().joinToString("")
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            sendButton.isEnabled = false
            sendButton.text = "Verificando..."
            emailEditText.isEnabled = false
        } else {
            sendButton.isEnabled = true
            sendButton.text = getString(R.string.send_button)
            emailEditText.isEnabled = true
        }
    }

    private fun showPasswordDialog(email: String, newPassword: String) {
        AlertDialog.Builder(this)
            .setTitle("✅ Contraseña Restablecida")
            .setMessage(
                "Se ha generado una nueva contraseña para:\n\n" +
                        "📧 Correo: $email\n\n" +
                        "🔐 Nueva contraseña:\n" +
                        "$newPassword\n\n" +
                        "⚠️ IMPORTANTE: Guarda esta contraseña en un lugar seguro. " +
                        "No podrás verla nuevamente."
            )
            .setPositiveButton("Copiar y Cerrar") { dialog, _ ->
                // Copiar al portapapeles
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Nueva Contraseña", newPassword)
                clipboard.setPrimaryClip(clip)

                android.widget.Toast.makeText(
                    this,
                    "Contraseña copiada al portapapeles",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
                finish() // Volver al login
            }
            .setNegativeButton("Cerrar") { dialog, _ ->
                dialog.dismiss()
                finish()
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