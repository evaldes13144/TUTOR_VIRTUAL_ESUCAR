package com.example.esucar_app.model

enum class PerfilUsuario(
    val nombreVisible: String,
    val descripcion: String
) {
    ESTUDIANTE(
        nombreVisible = "Estudiante",
        descripcion = "Consulta la guía metodológica, completa checklists, registra tus hitos y consulta el banco FAQ."
    ),
    PROFESOR_GUIA(
        nombreVisible = "Profesor Guía",
        descripcion = "Supervisa avances, aprueba hitos académicos, edita rúbricas y administra respuestas oficiales FAQ."
    )
}
