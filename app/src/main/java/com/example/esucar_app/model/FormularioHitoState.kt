package com.example.esucar_app.model

data class FormularioHitoState(
    val idEdicion: Int? = null,
    val estadoOriginal: String? = null,
    val etapa: String = "Trabajo de Título I",
    val titulo: String = "",
    val descripcion: String = "",
    val formatoRequerido: String = "",
    val fechaLimite: String = "",
    val criteriosRubrica: String = "",
    val rutaFotoEvidencia: String? = null,

    val errorTitulo: String? = null,
    val errorDescripcion: String? = null,
    val errorFormato: String? = null,
    val errorFecha: String? = null,
    val errorRubrica: String? = null,

    val guardadoExitoso: Boolean = false
) {
    val estaEditando: Boolean get() = idEdicion != null
}