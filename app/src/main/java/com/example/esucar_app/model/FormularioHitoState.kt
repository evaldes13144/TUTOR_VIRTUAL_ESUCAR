package com.example.esucar_app.model

import android.graphics.Bitmap

data class FormularioHitoState(
    val etapa: String = "Trabajo de Título I",
    val titulo: String = "",
    val descripcion: String = "",
    val formatoRequerido: String = "",
    val fechaLimite: String = "",
    val criteriosRubrica: String = "",
    val fotoEvidencia: Bitmap? = null,


    val errorTitulo: String? = null,
    val errorDescripcion: String? = null,
    val errorFormato: String? = null,
    val errorFecha: String? = null,
    val errorRubrica: String? = null,

    val guardadoExitoso: Boolean = false
)

data class FormularioFaqState(
    val pregunta: String = "",
    val categoria: String = "Metodología",
    val respuesta: String = "",
    val errorPregunta: String? = null,
    val errorRespuesta: String? = null
)