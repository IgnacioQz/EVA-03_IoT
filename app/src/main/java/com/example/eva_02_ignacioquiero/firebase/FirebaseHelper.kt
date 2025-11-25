package com.example.eva_02_ignacioquiero.firebase

import com.example.eva_02_ignacioquiero.models.Noticia
import com.example.eva_02_ignacioquiero.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirebaseHelper {

    // Instancias de Firebase
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Colección de noticias en Firestore
    private val noticiasCollection = firestore.collection("noticias")
    private val usuariosCollection = firestore.collection("usuarios")
    private val recuperacionesCollection = firestore.collection("recuperaciones")

    // ==================== AUTHENTICATION ====================

    /**
     * Registrar un nuevo usuario
     */
    fun registerUser(
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                authResult.user?.let { user ->
                    onSuccess(user)
                } ?: onFailure("Error: Usuario nulo")
            }
            .addOnFailureListener { exception ->
                onFailure(getErrorMessage(exception))
            }
    }

    /**
     * Guardar datos del usuario en Firestore
     */
    fun saveUserData(
        usuario: Usuario,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        usuariosCollection.document(usuario.id)
            .set(usuario)
            .addOnSuccessListener {
                android.util.Log.d("FirebaseHelper", "Usuario guardado: ${usuario.email}")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(getErrorMessage(exception))
            }
    }

    /**
     * Iniciar sesión
     */
    fun loginUser(
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                authResult.user?.let { user ->
                    onSuccess(user)
                } ?: onFailure("Error: Usuario nulo")
            }
            .addOnFailureListener { exception ->
                onFailure(getErrorMessage(exception))
            }
    }

    /**
     * Verificar si un usuario existe en Firestore (colección usuarios)
     * Esta es más confiable que fetchSignInMethodsForEmail en plan free
     */
    fun checkUserExists(
        email: String,
        onExists: (Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {
        android.util.Log.d("FirebaseHelper", "Verificando si existe en Firestore: $email")

        // Buscar en la colección "usuarios" por email
        usuariosCollection
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val exists = !querySnapshot.isEmpty
                android.util.Log.d("FirebaseHelper", "✓ Email encontrado en Firestore: $exists")
                onExists(exists)
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("FirebaseHelper", "Error verificando usuario: ${exception.message}")
                onFailure(getErrorMessage(exception))
            }
    }

    /**
     * Recuperar contraseña sin Cloud Functions (Plan Free)
     * Solo valida que el email existe y genera una contraseña temporal
     */
    fun resetPasswordFree(
        email: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Paso 1: Verificar si el usuario existe
        checkUserExists(
            email = email,
            onExists = { exists ->
                if (exists) {
                    // Paso 2: Guardar la contraseña temporal en Firestore
                    saveTemporaryPassword(email, newPassword, onSuccess, onFailure)
                } else {
                    onFailure("❌ Usuario no registrado\n\nNo existe una cuenta con este correo electrónico.\n\n¿Deseas registrarte?")
                }
            },
            onFailure = { error ->
                onFailure(error)
            }
        )
    }

    /**
     * Guardar contraseña temporal en Firestore en la colección "recuperaciones"
     * El usuario usará esta contraseña para iniciar sesión
     */
    private fun saveTemporaryPassword(
        email: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val recoveryData = hashMapOf(
            "email" to email,
            "temporaryPassword" to newPassword,
            "timestamp" to System.currentTimeMillis(),
            "used" to false
        )

        // Guardar con un ID basado en el email
        val recoveryId = email.replace("@", "_").replace(".", "_")

        recuperacionesCollection.document(recoveryId)
            .set(recoveryData)
            .addOnSuccessListener {
                android.util.Log.d("FirebaseHelper", "Contraseña temporal guardada")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("FirebaseHelper", "Error guardando contraseña temporal", exception)
                onFailure("❌ Error al procesar la solicitud: ${exception.message}")
            }
    }

    /**
     * Obtener la contraseña temporal guardada
     */
    fun getTemporaryPassword(
        email: String,
        onSuccess: (String?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val recoveryId = email.replace("@", "_").replace(".", "_")

        recuperacionesCollection.document(recoveryId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val tempPassword = document.getString("temporaryPassword")
                    onSuccess(tempPassword)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(getErrorMessage(exception))
            }
    }

    /**
     * Marcar la contraseña temporal como usada
     */
    fun markPasswordAsUsed(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val recoveryId = email.replace("@", "_").replace(".", "_")

        recuperacionesCollection.document(recoveryId)
            .update("used", true)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(getErrorMessage(exception))
            }
    }

    /**
     * Cerrar sesión
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Obtener usuario actual
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Verificar si hay un usuario logueado
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // ==================== FIRESTORE - NOTICIAS ====================

    /**
     * Guardar una noticia en Firestore
     */
    fun saveNoticia(
        noticia: Noticia,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Crear un documento con ID automático si no tiene
        val documentId = noticia.id.ifEmpty { noticiasCollection.document().id }
        val noticiaConId = noticia.copy(id = documentId)

        noticiasCollection.document(documentId)
            .set(noticiaConId)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Error al guardar noticia")
            }
    }

    /**
     * Obtener todas las noticias
     */
    fun getNoticias(
        onSuccess: (List<Noticia>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        noticiasCollection
            .get()
            .addOnSuccessListener { querySnapshot ->
                val noticias = querySnapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(Noticia::class.java)
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseHelper", "Error parsing noticia", e)
                        null
                    }
                }
                android.util.Log.d("FirebaseHelper", "Noticias obtenidas: ${noticias.size}")
                onSuccess(noticias)
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("FirebaseHelper", "Error obteniendo noticias", exception)
                onFailure(exception.message ?: "Error al obtener noticias")
            }
    }

    /**
     * Obtener una noticia específica por ID
     */
    fun getNoticiaById(
        id: String,
        onSuccess: (Noticia?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        noticiasCollection.document(id)
            .get()
            .addOnSuccessListener { document ->
                val noticia = document.toObject(Noticia::class.java)
                onSuccess(noticia)
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Error al obtener noticia")
            }
    }

    /**
     * Eliminar una noticia
     */
    fun deleteNoticia(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        noticiasCollection.document(id)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Error al eliminar noticia")
            }
    }

    /**
     * Actualizar una noticia existente
     */
    fun updateNoticia(
        noticia: Noticia,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        noticiasCollection.document(noticia.id)
            .set(noticia)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Error al actualizar noticia")
            }
    }

    // ==================== UTILIDADES ====================

    /**
     * Traducir mensajes de error de Firebase al español
     */
    private fun getErrorMessage(exception: Exception): String {
        val message = exception.message?.lowercase() ?: ""

        return when {
            // Errores de autenticación - Credenciales incorrectas o usuario no encontrado
            message.contains("supplied auth credential is incorrect") ||
                    message.contains("credential is incorrect") ||
                    message.contains("malformed") ||
                    message.contains("has expired") ->
                "❌ Usuario no registrado o contraseña incorrecta\n\nVerifica que:\n• El correo esté correctamente escrito\n• La contraseña sea la correcta\n• Tu cuenta esté registrada\n\n¿Necesitas registrarte o recuperar tu contraseña?"

            // Errores de autenticación - Usuario no encontrado
            message.contains("no user record") ||
                    message.contains("user not found") ||
                    message.contains("user-not-found") ||
                    message.contains("there is no user") ->
                "❌ Usuario no registrado\n\nNo existe una cuenta con este correo electrónico.\n\n¿Deseas registrarte?"

            // Errores de autenticación - Contraseña incorrecta específicamente
            message.contains("wrong-password") ||
                    message.contains("invalid-password") ||
                    message.contains("password is invalid") ->
                "❌ Contraseña incorrecta\n\nLa contraseña ingresada no es válida.\n\n¿Olvidaste tu contraseña?"

            // Error general de credenciales inválidas
            message.contains("invalid-credential") ->
                "❌ Credenciales inválidas\n\nEl correo o la contraseña son incorrectos.\n\nVerifica tus datos e intenta nuevamente."

            // Email inválido
            message.contains("invalid-email") ||
                    message.contains("badly formatted") ->
                "❌ Correo electrónico inválido\n\nPor favor ingresa un correo válido.\n\nEjemplo: usuario@correo.com"

            // Email ya en uso
            message.contains("email-already-in-use") ||
                    message.contains("email already in use") ->
                "❌ Correo ya registrado\n\nYa existe una cuenta con este correo electrónico.\n\n¿Deseas iniciar sesión?"

            // Contraseña débil
            message.contains("weak-password") ||
                    message.contains("password should be at least") ->
                "❌ Contraseña muy débil\n\nLa contraseña debe tener al menos 6 caracteres.\n\nUsa letras, números y símbolos."

            // Errores de red
            message.contains("network") ||
                    message.contains("timeout") ||
                    message.contains("unable to resolve host") ||
                    message.contains("failed to connect") ->
                "❌ Error de conexión\n\nNo se pudo conectar con el servidor.\n\n• Verifica tu conexión a internet\n• Intenta nuevamente en unos momentos"

            // Demasiados intentos
            message.contains("too-many-requests") ||
                    message.contains("too many attempts") ->
                "❌ Demasiados intentos\n\nSe han bloqueado temporalmente los intentos de inicio de sesión.\n\nPor favor intenta más tarde (espera 5-10 minutos)."

            // Usuario deshabilitado
            message.contains("user-disabled") ||
                    message.contains("disabled") ->
                "❌ Cuenta deshabilitada\n\nEsta cuenta ha sido deshabilitada.\n\nContacta al soporte para más información."

            // Operación no permitida
            message.contains("operation-not-allowed") ->
                "❌ Operación no permitida\n\nEste método de autenticación no está habilitado.\n\nContacta al administrador."

            // Errores de Firestore
            message.contains("permission-denied") ||
                    message.contains("missing or insufficient permissions") ->
                "❌ Permisos insuficientes\n\nNo tienes permisos para realizar esta operación.\n\nAsegúrate de haber iniciado sesión."

            message.contains("not-found") ->
                "❌ Documento no encontrado\n\nEl documento solicitado no existe en la base de datos."

            message.contains("already-exists") ->
                "❌ Ya existe\n\nEl documento que intentas crear ya existe."

            // Error genérico con el mensaje original
            else -> "❌ Error\n\n${exception.message ?: "Error desconocido al procesar la solicitud."}\n\nSi el problema persiste, contacta al soporte."
        }
    }
}