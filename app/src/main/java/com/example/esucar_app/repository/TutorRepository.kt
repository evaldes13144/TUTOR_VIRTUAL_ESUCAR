package com.example.esucar_app.repository

import com.example.esucar_app.data.AppDatabase
import com.example.esucar_app.data.TutorDao
import com.example.esucar_app.model.ChecklistGuiaEstado
import com.example.esucar_app.model.HitoTesis
import com.example.esucar_app.model.PreguntaFrecuente
import kotlinx.coroutines.flow.Flow

class TutorRepository(private val dao: TutorDao) {

    fun obtenerHitos(): Flow<List<HitoTesis>> = dao.obtenerHitos()

    suspend fun insertarHito(hito: HitoTesis): Long = dao.insertarHito(hito)

    suspend fun actualizarHito(hito: HitoTesis) = dao.actualizarHito(hito)

    suspend fun eliminarHito(hito: HitoTesis) = dao.eliminarHito(hito)

    fun obtenerPreguntas(): Flow<List<PreguntaFrecuente>> = dao.obtenerPreguntas()

    suspend fun insertarPregunta(pregunta: PreguntaFrecuente): Long = dao.insertarPregunta(pregunta)

    suspend fun actualizarPregunta(pregunta: PreguntaFrecuente) = dao.actualizarPregunta(pregunta)

    suspend fun eliminarPregunta(pregunta: PreguntaFrecuente) = dao.eliminarPregunta(pregunta)

    fun obtenerChecklistCompletados(): Flow<List<String>> = dao.obtenerChecklistCompletados()

    suspend fun guardarEstadoChecklist(itemId: String, completado: Boolean) {
        dao.guardarEstadoChecklist(ChecklistGuiaEstado(itemId = itemId, completado = completado))
    }

    suspend fun poblarDatosInicialesSiVacio() {
        AppDatabase.poblarDatosIniciales(dao)
    }
}
