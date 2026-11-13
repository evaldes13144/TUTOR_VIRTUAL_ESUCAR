package com.example.esucar_app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esucar_app.data.AppDatabase
import com.example.esucar_app.model.FormularioFaqState
import com.example.esucar_app.model.FormularioHitoState
import com.example.esucar_app.model.HitoTesis
import com.example.esucar_app.model.PreguntaFrecuente
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TutorViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.obtenerBaseDatos(application).tutorDao()

    // Listas reactivas desde Room
    val listaHitos: StateFlow<List<HitoTesis>> = dao.obtenerHitos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listaPreguntas: StateFlow<List<PreguntaFrecuente>> = dao.obtenerPreguntas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado del formulario de Hito
    private val _formHito = MutableStateFlow(FormularioHitoState())
    val formHito: StateFlow<FormularioHitoState> = _formHito.asStateFlow()

    // Estado del formulario de nueva consulta FAQ
    private val _formFaq = MutableStateFlow(FormularioFaqState())
    val formFaq: StateFlow<FormularioFaqState> = _formFaq.asStateFlow()

    // Filtros de busqueda en FAQ
    private val _busquedaFaq = MutableStateFlow("")
    val busquedaFaq: StateFlow<String> = _busquedaFaq.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow("Todas")
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada.asStateFlow()

    init {
        viewModelScope.launch {
            AppDatabase.poblarDatosIniciales(dao)
        }
    }


    fun onEtapaChange(valor: String) {
        _formHito.update { it.copy(etapa = valor, guardadoExitoso = false) }
    }

    fun onTituloChange(valor: String) {
        val error = when {
            valor.isBlank() -> "El título del hito es obligatorio"
            valor.trim().length < 6 -> "Debe tener al menos 6 caracteres"
            else -> null
        }
        _formHito.update { it.copy(titulo = valor, errorTitulo = error, guardadoExitoso = false) }
    }

    fun onDescripcionChange(valor: String) {
        val error = when {
            valor.isBlank() -> "La descripción metodológica es obligatoria"
            valor.trim().length < 15 -> "Detalla al menos 15 caracteres para guiar el capítulo"
            else -> null
        }
        _formHito.update { it.copy(descripcion = valor, errorDescripcion = error, guardadoExitoso = false) }
    }

    fun onFormatoChange(valor: String) {
        val contieneFormatoValido = valor.contains("PDF", ignoreCase = true) ||
                valor.contains("DOCX", ignoreCase = true) ||
                valor.contains("APA", ignoreCase = true)
        val error = when {
            valor.isBlank() -> "Indica el formato exigido (ej: PDF - APA 7)"
            !contieneFormatoValido -> "Debe especificar formato académico (PDF, DOCX o APA)"
            else -> null
        }
        _formHito.update { it.copy(formatoRequerido = valor, errorFormato = error, guardadoExitoso = false) }
    }

    fun onFechaChange(valor: String) {
        val regexFecha = Regex("""^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\d{4}$""")
        val error = when {
            valor.isBlank() -> "La fecha límite es obligatoria"
            !regexFecha.matches(valor.trim()) -> "Usa el formato DD/MM/AAAA (ej: 30/11/2026)"
            else -> null
        }
        _formHito.update { it.copy(fechaLimite = valor, errorFecha = error, guardadoExitoso = false) }
    }

    fun onRubricaChange(valor: String) {
        val error = when {
            valor.isBlank() -> "Ingresa al menos un criterio de evaluación"
            valor.trim().length < 10 -> "El criterio debe tener mínimo 10 caracteres"
            else -> null
        }
        _formHito.update { it.copy(criteriosRubrica = valor, errorRubrica = error, guardadoExitoso = false) }
    }

    fun onFotoCapturada(bitmap: Bitmap?) {
        _formHito.update { it.copy(fotoEvidencia = bitmap) }
    }

    fun validarYGuardarHito(): Boolean {
        val actual = _formHito.value
        onTituloChange(actual.titulo)
        onDescripcionChange(actual.descripcion)
        onFormatoChange(actual.formatoRequerido)
        onFechaChange(actual.fechaLimite)
        onRubricaChange(actual.criteriosRubrica)

        val validado = _formHito.value
        val esValido = validado.errorTitulo == null &&
                validado.errorDescripcion == null &&
                validado.errorFormato == null &&
                validado.errorFecha == null &&
                validado.errorRubrica == null

        if (esValido) {
            viewModelScope.launch {
                val nuevoHito = HitoTesis(
                    etapa = validado.etapa,
                    titulo = validado.titulo.trim(),
                    descripcion = validado.descripcion.trim(),
                    formatoRequerido = validado.formatoRequerido.trim(),
                    fechaLimite = validado.fechaLimite.trim(),
                    estado = "En desarrollo",
                    criteriosRubrica = validado.criteriosRubrica.trim(),
                    tieneEvidenciaFoto = validado.fotoEvidencia != null
                )
                dao.insertarHito(nuevoHito)
                _formHito.value = FormularioHitoState(guardadoExitoso = true)
            }
        }
        return esValido
    }

    fun cambiarEstadoHito(hito: HitoTesis) {
        val ordenEstados = listOf("Por iniciar", "En desarrollo", "Enviado", "Aprobado")
        val indiceActual = ordenEstados.indexOf(hito.estado)
        val siguienteEstado = ordenEstados[(indiceActual + 1) % ordenEstados.size]
        viewModelScope.launch {
            dao.actualizarHito(hito.copy(estado = siguienteEstado))
        }
    }

    fun eliminarHito(hito: HitoTesis) {
        viewModelScope.launch {
            dao.eliminarHito(hito)
        }
    }

    // --- Buscador y Formulario de Preguntas Frecuentes (FAQ) ---

    fun actualizarBusquedaFaq(texto: String) {
        _busquedaFaq.value = texto
    }

    fun seleccionarCategoriaFaq(categoria: String) {
        _categoriaSeleccionada.value = categoria
    }

    fun onPreguntaFaqChange(valor: String) {
        val error = when {
            valor.isBlank() -> "Escribe la pregunta metodológica"
            valor.trim().length < 10 -> "La pregunta debe tener al menos 10 caracteres"
            else -> null
        }
        _formFaq.update { it.copy(pregunta = valor, errorPregunta = error) }
    }

    fun onCategoriaFaqChange(valor: String) {
        _formFaq.update { it.copy(categoria = valor) }
    }

    fun onRespuestaFaqChange(valor: String) {
        val error = when {
            valor.isBlank() -> "La respuesta u orientación es obligatoria"
            valor.trim().length < 15 -> "Escribe una respuesta clara (mínimo 15 caracteres)"
            else -> null
        }
        _formFaq.update { it.copy(respuesta = valor, errorRespuesta = error) }
    }

    fun guardarPreguntaFaq(): Boolean {
        val actual = _formFaq.value
        onPreguntaFaqChange(actual.pregunta)
        onRespuestaFaqChange(actual.respuesta)

        val postValidacion = _formFaq.value
        val esValido = postValidacion.errorPregunta == null && postValidacion.errorRespuesta == null

        if (esValido) {
            viewModelScope.launch {
                dao.insertarPregunta(
                    PreguntaFrecuente(
                        pregunta = postValidacion.pregunta.trim(),
                        categoria = postValidacion.categoria,
                        respuesta = postValidacion.respuesta.trim()
                    )
                )
                _formFaq.value = FormularioFaqState()
            }
        }
        return esValido
    }
}