package com.example.esucar_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist_guia")
data class ChecklistGuiaEstado(
    @PrimaryKey
    val itemId: String,
    val completado: Boolean
)
