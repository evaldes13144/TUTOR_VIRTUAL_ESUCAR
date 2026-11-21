package com.example.esucar_app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.esucar_app.data.AppDatabase
import com.example.esucar_app.data.TutorDao
import com.example.esucar_app.model.FormularioFaqState
import com.example.esucar_app.model.FormularioHitoState
import com.example.esucar_app.model.HitoTesis
import com.example.esucar_app.model.PerfilUsuario
import com.example.esucar_app.model.PreguntaFrecuente
import com.example.esucar_app.repository.TutorRepository
import com.example.esucar_app.util.ImagenHelper
import com.example.esucar_app.util.TextoNormalizer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.time.temporal.ChronoUnit

class TutorViewModel(
    private val repository: TutorRepository,
    poblarIniciales: Boolean = true,
    perfilInicial: PerfilUsuario = PerfilUsuario.ESTUDIANTE,
    private val onDatosPoblados: () -> Unit = {},
    private val onPerfilGuardado: (PerfilUsuario) -> Unit = {}
) : ViewModel() {

    constructor(
        dao: TutorDao,
        poblarIniciales: Boolean = true,
        perfilInicial: PerfilUsuario = PerfilUsuario.ESTUDIANTE,
        onDatosPoblados: () -> Unit = {},
        onPerfilGuardado: (PerfilUsuario) -> Unit = {}
    ) : this(
        repository = TutorRepository(dao),
        poblarIniciales = poblarIniciales,
        perfilInicial = perfilInicial,
        onDatosPoblados = onDatosPoblados,
        onPerfilGuardado = onPerfilGuardado
    )

    // Perfil de usuario activo (Estudiante o Profesor Guía)
    private val _perfilActual = MutableStateFlow(perfilInicial)
    val perfilActual: StateFlow<PerfilUsuario> = _perfilActual.asStateFlow()

    // Listas reactivas desde Room vía Repository
    val listaHitos: StateFlow<List<HitoTesis>> = repository.obtenerHitos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listaPreguntas: StateFlow<List<PreguntaFrecuente>> = repository.obtenerPreguntas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checklistCompletados: StateFlow<Set<String>> = repository.obtenerChecklistCompletados()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Filtro de asignatura en Mis Hitos
    private val _etapaFiltroHitos = MutableStateFlow("Todas")
    val etapaFiltroHitos: StateFlow<String> = _etapaFiltroHitos.asStateFlow()

    val hitosFiltrados: StateFlow<List<HitoTesis>> = combine(
        listaHitos,
        _etapaFiltroHitos
    ) { hitos, etapa ->
        val filtrados = if (etapa == "Todas") hitos else hitos.filter { it.etapa == etapa }
        filtrados.sortedWith(
            compareBy<HitoTesis> { parsearFecha(it.fechaLimite) ?: LocalDate.MAX }
                .thenBy { it.id }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado del formulario de Hito (creación y edición)
    private val _formHito = MutableStateFlow(FormularioHitoState())
    val formHito: StateFlow<FormularioHitoState> = _formHito.asStateFlow()

    // Estado del formulario de consulta FAQ (creación y edición)
    private val _formFaq = MutableStateFlow(FormularioFaqState())
    val formFaq: StateFlow<FormularioFaqState> = _formFaq.asStateFlow()

    // Filtros de búsqueda en FAQ (insensible a tildes y mayúsculas)
    private val _busquedaFaq = MutableStateFlow("")
    val busquedaFaq: StateFlow<String> = _busquedaFaq.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow("Todas")
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada.asStateFlow()

    val preguntasFiltradas: StateFlow<List<PreguntaFrecuente>> = combine(
        listaPreguntas,
        _busquedaFaq,
        _categoriaSeleccionada
    ) { preguntas, busqueda, categoria ->
        val terminoNormalizado = TextoNormalizer.normalizar(busqueda)
        preguntas.filter { item ->
            val coincideCategoria = categoria == "Todas" || item.categoria == categoria
            val coincideTexto = terminoNormalizado.isEmpty() ||
                    TextoNormalizer.normalizar(item.pregunta).contains(terminoNormalizado) ||
                    TextoNormalizer.normalizar(item.respuesta).contains(terminoNormalizado) ||
                    TextoNormalizer.normalizar(item.categoria).contains(terminoNormalizado)
            coincideCategoria && coincideTexto
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (poblarIniciales) {
            viewModelScope.launch {
                repository.poblarDatosInicialesSiVacio()
                onDatosPoblados()
            }
        }
    }

    // --- Perfil de usuario ---

    fun cambiarPerfil(perfil: PerfilUsuario) {
        _perfilActual.value = perfil
        onPerfilGuardado(perfil)
    }

    // --- Guía Metodológica y Checklist ---

    fun alternarItemChecklist(itemId: String) {
        val estaCompletado = itemId in checklistCompletados.value
        viewModelScope.launch {
            repository.guardarEstadoChecklist(itemId, !estaCompletado)
        }
    }

    // --- Filtros y CRUD de Hitos ---

    fun seleccionarEtapaHitos(etapa: String) {
        _etapaFiltroHitos.value = etapa
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
        val limpia = valor.trim()
        val error = when {
            limpia.isBlank() -> "La fecha límite es obligatoria"
            !REGEX_FECHA.matches(limpia) -> "Usa el formato DD/MM/AAAA (ej: 30/11/2026)"
            !esFechaCalendarioValida(limpia) -> "Ingresa una fecha de calendario válida (ej: 30/11/2026)"
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

    fun onFotoRutaCapturada(rutaArchivo: String?) {
        _formHito.update { it.copy(rutaFotoEvidencia = rutaArchivo, guardadoExitoso = false) }
    }

    fun eliminarFotoFormulario() {
        _formHito.update { it.copy(rutaFotoEvidencia = null, guardadoExitoso = false) }
    }

    fun cargarHitoParaEdicion(hito: HitoTesis) {
        _formHito.value = FormularioHitoState(
            idEdicion = hito.id,
            estadoOriginal = hito.estado,
            etapa = hito.etapa,
            titulo = hito.titulo,
            descripcion = hito.descripcion,
            formatoRequerido = hito.formatoRequerido,
            fechaLimite = hito.fechaLimite,
            criteriosRubrica = hito.criteriosRubrica,
            rutaFotoEvidencia = hito.rutaFotoEvidencia
        )
    }

    fun cancelarEdicionHito() {
        _formHito.value = FormularioHitoState()
    }

    fun validarYGuardarHito(onGuardado: (HitoTesis) -> Unit = {}): Boolean {
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
                val hitoBase = HitoTesis(
                    id = validado.idEdicion ?: 0,
                    etapa = validado.etapa,
                    titulo = validado.titulo.trim(),
                    descripcion = validado.descripcion.trim(),
                    formatoRequerido = validado.formatoRequerido.trim(),
                    fechaLimite = validado.fechaLimite.trim(),
                    estado = validado.estadoOriginal ?: "En desarrollo",
                    criteriosRubrica = validado.criteriosRubrica.trim(),
                    tieneEvidenciaFoto = !validado.rutaFotoEvidencia.isNullOrBlank(),
                    rutaFotoEvidencia = validado.rutaFotoEvidencia
                )
                val hitoPersistido = if (validado.estaEditando) {
                    repository.actualizarHito(hitoBase)
                    hitoBase
                } else {
                    val nuevoId = repository.insertarHito(hitoBase).toInt()
                    hitoBase.copy(id = nuevoId)
                }
                _formHito.value = FormularioHitoState(guardadoExitoso = true)
                onGuardado(hitoPersistido)
            }
        }
        return esValido
    }

    /**
     * Avanza el estado de un hito según el perfil activo:
     * - Estudiante: Por iniciar -> En desarrollo -> Enviado (no puede auto-aprobarse ni revertir un hito ya Aprobado).
     * - Profesor Guía: ciclo completo incluyendo Aprobado.
     * Retorna false si la transición está restringida por perfil.
     */
    fun cambiarEstadoHito(hito: HitoTesis): Boolean {
        val perfil = _perfilActual.value
        if (perfil == PerfilUsuario.ESTUDIANTE && hito.estado == "Aprobado") {
            return false
        }

        val ordenEstados = if (perfil == PerfilUsuario.PROFESOR_GUIA) {
            listOf("Por iniciar", "En desarrollo", "Enviado", "Aprobado")
        } else {
            listOf("Por iniciar", "En desarrollo", "Enviado")
        }
        val indiceActual = ordenEstados.indexOf(hito.estado)
        val siguienteEstado = ordenEstados[(indiceActual + 1) % ordenEstados.size]

        viewModelScope.launch {
            repository.actualizarHito(hito.copy(estado = siguienteEstado))
        }
        return true
    }

    fun aprobarHito(hito: HitoTesis): Boolean {
        if (_perfilActual.value != PerfilUsuario.PROFESOR_GUIA) return false
        viewModelScope.launch {
            repository.actualizarHito(hito.copy(estado = "Aprobado"))
        }
        return true
    }

    fun eliminarHito(hito: HitoTesis) {
        viewModelScope.launch {
            ImagenHelper.eliminarArchivoSiExiste(hito.rutaFotoEvidencia)
            repository.eliminarHito(hito)
            if (_formHito.value.idEdicion == hito.id) {
                _formHito.value = FormularioHitoState()
            }
        }
    }

    // --- Buscador y CRUD de Preguntas Frecuentes (FAQ) ---

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

    fun cargarFaqParaEdicion(faq: PreguntaFrecuente) {
        _formFaq.value = FormularioFaqState(
            idEdicion = faq.id,
            pregunta = faq.pregunta,
            categoria = faq.categoria,
            respuesta = faq.respuesta
        )
    }

    fun limpiarFormularioFaq() {
        _formFaq.value = FormularioFaqState()
    }

    fun guardarPreguntaFaq(): Boolean {
        val actual = _formFaq.value
        onPreguntaFaqChange(actual.pregunta)
        onRespuestaFaqChange(actual.respuesta)

        val postValidacion = _formFaq.value
        val esValido = postValidacion.errorPregunta == null && postValidacion.errorRespuesta == null

        if (esValido) {
            viewModelScope.launch {
                val entidad = PreguntaFrecuente(
                    id = postValidacion.idEdicion ?: 0,
                    pregunta = postValidacion.pregunta.trim(),
                    categoria = postValidacion.categoria,
                    respuesta = postValidacion.respuesta.trim()
                )
                if (postValidacion.estaEditando) {
                    repository.actualizarPregunta(entidad)
                } else {
                    repository.insertarPregunta(entidad)
                }
                _formFaq.value = FormularioFaqState()
            }
        }
        return esValido
    }

    fun eliminarPreguntaFaq(faq: PreguntaFrecuente) {
        viewModelScope.launch {
            repository.eliminarPregunta(faq)
        }
    }

    companion object {
        private const val PREFS_NAME = "esucar_tutor_prefs"
        private const val KEY_SEED_COMPLETADO = "datos_iniciales_poblados"
        private const val KEY_PERFIL_ACTIVO = "perfil_usuario_activo"

        private val REGEX_FECHA = Regex("""^\d{2}/\d{2}/\d{4}$""")
        private val FORMATO_FECHA_ESTRICTO: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

        fun parsearFecha(fecha: String): LocalDate? {
            return try {
                val parsed = LocalDate.parse(fecha.trim(), FORMATO_FECHA_ESTRICTO)
                if (parsed.year in 1900..2100) parsed else null
            } catch (_: DateTimeParseException) {
                null
            }
        }

        internal fun esFechaCalendarioValida(fecha: String): Boolean = parsearFecha(fecha) != null

        fun calcularDiasRestantes(fechaLimite: String, hoy: LocalDate = LocalDate.now()): Long? {
            val fechaEntrega = parsearFecha(fechaLimite) ?: return null
            return ChronoUnit.DAYS.between(hoy, fechaEntrega)
        }

        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = AppDatabase.obtenerBaseDatos(appContext).tutorDao()
                    val repository = TutorRepository(dao)
                    val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val yaPoblado = prefs.getBoolean(KEY_SEED_COMPLETADO, false)
                    val perfilGuardado = runCatching {
                        PerfilUsuario.valueOf(
                            prefs.getString(KEY_PERFIL_ACTIVO, PerfilUsuario.ESTUDIANTE.name)
                                ?: PerfilUsuario.ESTUDIANTE.name
                        )
                    }.getOrDefault(PerfilUsuario.ESTUDIANTE)

                    return TutorViewModel(
                        repository = repository,
                        poblarIniciales = !yaPoblado,
                        perfilInicial = perfilGuardado,
                        onDatosPoblados = {
                            prefs.edit().putBoolean(KEY_SEED_COMPLETADO, true).apply()
                        },
                        onPerfilGuardado = { nuevoPerfil ->
                            prefs.edit().putString(KEY_PERFIL_ACTIVO, nuevoPerfil.name).apply()
                        }
                    ) as T
                }
            }
        }
    }
}