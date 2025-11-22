package com.example.eva_02_ignacioquiero.models

data class Noticia(
    val id: String = "",
    val titulo: String = "",
    val bajada: String = "",
    val imagenUrl: String = "",
    val cuerpo: String = "",
    val fecha: String = ""
) {
    // Constructor vacío requerido por Firebase
    constructor() : this("", "", "", "", "", "")

    // Método para validar si la noticia está completa
    fun isValid(): Boolean {
        return id.isNotEmpty() &&
                titulo.isNotEmpty() &&
                bajada.isNotEmpty() &&
                cuerpo.isNotEmpty() &&
                fecha.isNotEmpty()
    }
}