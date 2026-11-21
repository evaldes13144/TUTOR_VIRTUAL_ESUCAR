package com.example.esucar_app.data

import androidx.room.*
import com.example.esucar_app.model.ChecklistGuiaEstado
import com.example.esucar_app.model.HitoTesis
import com.example.esucar_app.model.PreguntaFrecuente
import kotlinx.coroutines.flow.Flow

@Dao
interface TutorDao {

    @Query("SELECT * FROM hitos_tesis ORDER BY id ASC")
    fun obtenerHitos(): Flow<List<HitoTesis>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarHito(hito: HitoTesis): Long

    @Update
    suspend fun actualizarHito(hito: HitoTesis)

    @Delete
    suspend fun eliminarHito(hito: HitoTesis)

    @Query("SELECT COUNT(*) FROM hitos_tesis")
    suspend fun contarHitos(): Int

    @Query("SELECT * FROM preguntas_frecuentes ORDER BY id DESC")
    fun obtenerPreguntas(): Flow<List<PreguntaFrecuente>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPregunta(pregunta: PreguntaFrecuente): Long

    @Update
    suspend fun actualizarPregunta(pregunta: PreguntaFrecuente)

    @Delete
    suspend fun eliminarPregunta(pregunta: PreguntaFrecuente)

    @Query("SELECT COUNT(*) FROM preguntas_frecuentes")
    suspend fun contarPreguntas(): Int

    @Query("SELECT itemId FROM checklist_guia WHERE completado = 1")
    fun obtenerChecklistCompletados(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarEstadoChecklist(estado: ChecklistGuiaEstado)
}