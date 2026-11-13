package com.example.esucar_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preguntas_frecuentes")
data class PreguntaFrecuente(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val pregunta: String,
    val categoria: String, // Metodologia, Citacion APA, Formato, Entregas
    val respuesta: String
)