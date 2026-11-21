package com.example.esucar_app.model

data class FormularioFaqState(
    val idEdicion: Int? = null,
    val pregunta: String = "",
    val categoria: String = "Metodología",
    val respuesta: String = "",
    val errorPregunta: String? = null,
    val errorRespuesta: String? = null
) {
    val estaEditando: Boolean get() = idEdicion != null
}
