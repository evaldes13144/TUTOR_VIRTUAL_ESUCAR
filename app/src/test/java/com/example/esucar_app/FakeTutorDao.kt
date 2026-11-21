package com.example.esucar_app

import com.example.esucar_app.data.TutorDao
import com.example.esucar_app.model.ChecklistGuiaEstado
import com.example.esucar_app.model.HitoTesis
import com.example.esucar_app.model.PreguntaFrecuente
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTutorDao : TutorDao {

    private var nextHitoId = 1
    private var nextPreguntaId = 1

    private val hitosFlow = MutableStateFlow<List<HitoTesis>>(emptyList())
    private val preguntasFlow = MutableStateFlow<List<PreguntaFrecuente>>(emptyList())
    private val checklistFlow = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    override fun obtenerHitos(): Flow<List<HitoTesis>> =
        hitosFlow.map { lista -> lista.sortedBy { it.id } }

    override suspend fun insertarHito(hito: HitoTesis): Long {
        val conId = if (hito.id == 0) hito.copy(id = nextHitoId++) else hito
        hitosFlow.update { actual ->
            actual.filterNot { it.id == conId.id } + conId
        }
        return conId.id.toLong()
    }

    override suspend fun actualizarHito(hito: HitoTesis) {
        hitosFlow.update { actual ->
            actual.map { if (it.id == hito.id) hito else it }
        }
    }

    override suspend fun eliminarHito(hito: HitoTesis) {
        hitosFlow.update { actual ->
            actual.filterNot { it.id == hito.id }
        }
    }

    override suspend fun contarHitos(): Int = hitosFlow.value.size

    override fun obtenerPreguntas(): Flow<List<PreguntaFrecuente>> =
        preguntasFlow.map { lista -> lista.sortedByDescending { it.id } }

    override suspend fun insertarPregunta(pregunta: PreguntaFrecuente): Long {
        val conId = if (pregunta.id == 0) pregunta.copy(id = nextPreguntaId++) else pregunta
        preguntasFlow.update { actual ->
            actual.filterNot { it.id == conId.id } + conId
        }
        return conId.id.toLong()
    }

    override suspend fun actualizarPregunta(pregunta: PreguntaFrecuente) {
        preguntasFlow.update { actual ->
            actual.map { if (it.id == pregunta.id) pregunta else it }
        }
    }

    override suspend fun eliminarPregunta(pregunta: PreguntaFrecuente) {
        preguntasFlow.update { actual ->
            actual.filterNot { it.id == pregunta.id }
        }
    }

    override suspend fun contarPreguntas(): Int = preguntasFlow.value.size

    override fun obtenerChecklistCompletados(): Flow<List<String>> =
        checklistFlow.map { mapa -> mapa.filterValues { it }.keys.toList() }

    override suspend fun guardarEstadoChecklist(estado: ChecklistGuiaEstado) {
        checklistFlow.update { actual ->
            actual + (estado.itemId to estado.completado)
        }
    }
}
