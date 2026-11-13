package com.example.esucar_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hitos_tesis")
data class HitoTesis(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val etapa: String,           // Ej: Trabajo de Titulo I, II o III
    val titulo: String,          // Ej: Capitulo 1: Planteamiento del Problema
    val descripcion: String,
    val formatoRequerido: String,// Ej: PDF / Norma APA 7
    val fechaLimite: String,     // Formato DD/MM/AAAA
    val estado: String,          // Por iniciar, En desarrollo, Enviado, Aprobado
    val criteriosRubrica: String,
    val tieneEvidenciaFoto: Boolean = false
)