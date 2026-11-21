package com.example.esucar_app

import com.example.esucar_app.data.AppDatabase
import com.example.esucar_app.model.HitoTesis
import com.example.esucar_app.model.PerfilUsuario
import com.example.esucar_app.util.RecordatorioScheduler
import com.example.esucar_app.util.TextoNormalizer
import com.example.esucar_app.viewmodel.TutorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class TutorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeTutorDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeTutorDao()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Precarga inicial e idempotencia ---

    @Test
    fun precargaInicial_insertaCincoHitosYDiezFaqsYEjecutaCallback() = runTest {
        var callbackEjecutado = false
        val vm = TutorViewModel(
            dao = fakeDao,
            poblarIniciales = true,
            onDatosPoblados = { callbackEjecutado = true }
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.listaHitos.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.listaPreguntas.collect() }

        advanceUntilIdle()

        assertTrue(callbackEjecutado)
        assertEquals(5, vm.listaHitos.value.size)
        assertEquals(10, vm.listaPreguntas.value.size)
    }

    @Test
    fun poblarDatosIniciales_esIdempotenteSiYaExistenRegistros() = runTest {
        AppDatabase.poblarDatosIniciales(fakeDao)
        AppDatabase.poblarDatosIniciales(fakeDao)

        assertEquals(5, fakeDao.contarHitos())
        assertEquals(10, fakeDao.contarPreguntas())
    }

    @Test
    fun sinPoblarIniciales_noResucitaHitosCuandoBaseEstaVacia() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.listaHitos.collect() }

        advanceUntilIdle()

        assertTrue(vm.listaHitos.value.isEmpty())
        assertEquals(0, fakeDao.contarHitos())
    }

    // --- Validaciones de formulario de Hito ---

    @Test
    fun validacionTitulo_rechazaVacioYMenorASeisCaracteres() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)

        vm.onTituloChange("   ")
        assertEquals("El título del hito es obligatorio", vm.formHito.value.errorTitulo)

        vm.onTituloChange("  Cap1 ")
        assertEquals("Debe tener al menos 6 caracteres", vm.formHito.value.errorTitulo)

        vm.onTituloChange("Cap. 1: Introducción")
        assertNull(vm.formHito.value.errorTitulo)
    }

    @Test
    fun validacionDescripcion_rechazaVacioYMenorAQuinceCaracteres() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)

        vm.onDescripcionChange("")
        assertEquals("La descripción metodológica es obligatoria", vm.formHito.value.errorDescripcion)

        vm.onDescripcionChange("Muy corta")
        assertEquals("Detalla al menos 15 caracteres para guiar el capítulo", vm.formHito.value.errorDescripcion)

        vm.onDescripcionChange("Descripción completa de los objetivos y alcance del capítulo.")
        assertNull(vm.formHito.value.errorDescripcion)
    }

    @Test
    fun validacionFormato_aceptaPdfDocxOApaSinDistinguirMayusculas() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)

        vm.onFormatoChange("")
        assertNotNull(vm.formHito.value.errorFormato)

        vm.onFormatoChange("Presentación PPTX")
        assertEquals("Debe especificar formato académico (PDF, DOCX o APA)", vm.formHito.value.errorFormato)

        vm.onFormatoChange("Informe en pdf")
        assertNull(vm.formHito.value.errorFormato)

        vm.onFormatoChange("Documento docx")
        assertNull(vm.formHito.value.errorFormato)

        vm.onFormatoChange("Norma apa 7")
        assertNull(vm.formHito.value.errorFormato)
    }

    @Test
    fun validacionFecha_rechazaFormatoInvalidoYFechasInexistentesDeCalendario() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)

        vm.onFechaChange("")
        assertEquals("La fecha límite es obligatoria", vm.formHito.value.errorFecha)

        vm.onFechaChange("2026-11-30")
        assertEquals("Usa el formato DD/MM/AAAA (ej: 30/11/2026)", vm.formHito.value.errorFecha)

        listOf("31/02/2026", "31/04/2026", "29/02/2025", "00/01/2026", "01/01/0000").forEach { fechaInvalida ->
            vm.onFechaChange(fechaInvalida)
            assertNotNull("Debió rechazar la fecha inválida $fechaInvalida", vm.formHito.value.errorFecha)
        }

        vm.onFechaChange("29/02/2024")
        assertNull(vm.formHito.value.errorFecha)

        vm.onFechaChange("30/11/2026")
        assertNull(vm.formHito.value.errorFecha)
    }

    @Test
    fun validacionRubrica_rechazaVacioYMenorADiezCaracteres() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)

        vm.onRubricaChange(" ")
        assertEquals("Ingresa al menos un criterio de evaluación", vm.formHito.value.errorRubrica)

        vm.onRubricaChange("Corto")
        assertEquals("El criterio debe tener mínimo 10 caracteres", vm.formHito.value.errorRubrica)

        vm.onRubricaChange("Coherencia metodológica (50%) y APA 7 (50%)")
        assertNull(vm.formHito.value.errorRubrica)
    }

    @Test
    fun validarYGuardarHito_fallaConFormularioVacioYNoInserta() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)

        val guardado = vm.validarYGuardarHito()
        advanceUntilIdle()

        assertFalse(guardado)
        assertNotNull(vm.formHito.value.errorTitulo)
        assertNotNull(vm.formHito.value.errorDescripcion)
        assertNotNull(vm.formHito.value.errorFormato)
        assertNotNull(vm.formHito.value.errorFecha)
        assertNotNull(vm.formHito.value.errorRubrica)
        assertEquals(0, fakeDao.contarHitos())
    }

    @Test
    fun validarYGuardarHito_creaYEditaHitoConRutaDeFoto() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.listaHitos.collect() }

        vm.onEtapaChange("Trabajo de Título II")
        vm.onTituloChange("  Cap. 3: Diseño Metodológico  ")
        vm.onDescripcionChange("  Definición de muestra, instrumentos y validación en terreno.  ")
        vm.onFormatoChange("  PDF - APA 7  ")
        vm.onFechaChange("  15/12/2026  ")
        vm.onRubricaChange("  Instrumento validado (100%)  ")
        vm.onFotoRutaCapturada("/data/evidencias/foto_1.jpg")

        var callbackHito: HitoTesis? = null
        val guardado = vm.validarYGuardarHito { callbackHito = it }
        advanceUntilIdle()

        assertTrue(guardado)
        assertNotNull(callbackHito)
        assertEquals(1, callbackHito!!.id)
        assertEquals(1, vm.listaHitos.value.size)

        // Edición del hito existente
        vm.cargarHitoParaEdicion(vm.listaHitos.value.first())
        assertTrue(vm.formHito.value.estaEditando)
        vm.onTituloChange("Cap. 3: Marco Metodológico Actualizado")
        vm.onFechaChange("20/12/2026")
        assertTrue(vm.validarYGuardarHito())
        advanceUntilIdle()

        assertEquals(1, vm.listaHitos.value.size)
        val editado = vm.listaHitos.value.first()
        assertEquals("Cap. 3: Marco Metodológico Actualizado", editado.titulo)
        assertEquals("20/12/2026", editado.fechaLimite)
        assertTrue(editado.tieneEvidenciaFoto)
        assertEquals("/data/evidencias/foto_1.jpg", editado.rutaFotoEvidencia)
    }

    // --- Perfiles: Estudiante vs Profesor Guía ---

    @Test
    fun cambiarEstadoHito_respetaRestriccionesDePerfilEstudianteYProfesor() = runTest {
        val vm = TutorViewModel(
            dao = fakeDao,
            poblarIniciales = false,
            perfilInicial = PerfilUsuario.ESTUDIANTE
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.listaHitos.collect() }

        fakeDao.insertarHito(
            HitoTesis(
                id = 1,
                etapa = "Trabajo de Título I",
                titulo = "Cap. 1: Problema",
                descripcion = "Descripción metodológica de prueba.",
                formatoRequerido = "PDF",
                fechaLimite = "20/11/2026",
                estado = "Por iniciar",
                criteriosRubrica = "Criterio general (100%)"
            )
        )
        advanceUntilIdle()

        // Estudiante: Por iniciar -> En desarrollo -> Enviado -> Por iniciar (sin auto-aprobarse)
        assertTrue(vm.cambiarEstadoHito(vm.listaHitos.value.first()))
        advanceUntilIdle()
        assertEquals("En desarrollo", vm.listaHitos.value.first().estado)

        assertTrue(vm.cambiarEstadoHito(vm.listaHitos.value.first()))
        advanceUntilIdle()
        assertEquals("Enviado", vm.listaHitos.value.first().estado)

        assertFalse(vm.aprobarHito(vm.listaHitos.value.first()))

        // Cambiar a Profesor Guía -> puede aprobar
        vm.cambiarPerfil(PerfilUsuario.PROFESOR_GUIA)
        assertTrue(vm.aprobarHito(vm.listaHitos.value.first()))
        advanceUntilIdle()
        assertEquals("Aprobado", vm.listaHitos.value.first().estado)

        // Volver a Estudiante -> no puede modificar un hito ya Aprobado
        vm.cambiarPerfil(PerfilUsuario.ESTUDIANTE)
        assertFalse(vm.cambiarEstadoHito(vm.listaHitos.value.first()))
    }

    @Test
    fun hitosFiltrados_filtraPorAsignaturaYOrdenaCronologicamente() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.hitosFiltrados.collect() }

        fakeDao.insertarHito(
            HitoTesis(
                etapa = "Trabajo de Título II",
                titulo = "Hito Posterior",
                descripcion = "Descripción de prueba metodológica.",
                formatoRequerido = "PDF",
                fechaLimite = "28/12/2026",
                estado = "Por iniciar",
                criteriosRubrica = "Criterio 100%"
            )
        )
        fakeDao.insertarHito(
            HitoTesis(
                etapa = "Trabajo de Título I",
                titulo = "Hito Más Próximo",
                descripcion = "Descripción de prueba metodológica.",
                formatoRequerido = "PDF",
                fechaLimite = "01/12/2026",
                estado = "En desarrollo",
                criteriosRubrica = "Criterio 100%"
            )
        )
        advanceUntilIdle()

        assertEquals(2, vm.hitosFiltrados.value.size)
        assertEquals("Hito Más Próximo", vm.hitosFiltrados.value.first().titulo)

        vm.seleccionarEtapaHitos("Trabajo de Título II")
        advanceUntilIdle()
        assertEquals(1, vm.hitosFiltrados.value.size)
        assertEquals("Hito Posterior", vm.hitosFiltrados.value.first().titulo)
    }

    @Test
    fun eliminarHito_remueveElementoDeListaReactiva() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = true)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.listaHitos.collect() }
        advanceUntilIdle()

        val primero = vm.listaHitos.value.first()
        vm.eliminarHito(primero)
        advanceUntilIdle()

        assertEquals(4, vm.listaHitos.value.size)
        assertFalse(vm.listaHitos.value.any { it.id == primero.id })
    }

    // --- Checklist de Guía Metodológica ---

    @Test
    fun alternarItemChecklist_persisteYRevierteEstadoEnRoom() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.checklistCompletados.collect() }

        vm.alternarItemChecklist("p1_c1")
        advanceUntilIdle()
        assertTrue("p1_c1" in vm.checklistCompletados.value)

        vm.alternarItemChecklist("p1_c1")
        advanceUntilIdle()
        assertFalse("p1_c1" in vm.checklistCompletados.value)
    }

    // --- FAQ: CRUD y búsqueda insensible a tildes ---

    @Test
    fun guardarYEditarYEliminarPreguntaFaq_funcionaCorrectamente() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.listaPreguntas.collect() }

        assertFalse(vm.guardarPreguntaFaq())
        assertNotNull(vm.formFaq.value.errorPregunta)
        assertNotNull(vm.formFaq.value.errorRespuesta)

        vm.onCategoriaFaqChange("Citación APA")
        vm.onPreguntaFaqChange("  ¿Cómo citar un decreto supremo en APA 7?  ")
        vm.onRespuestaFaqChange("  Se indica el número del decreto, el ministerio y el año de publicación.  ")

        assertTrue(vm.guardarPreguntaFaq())
        advanceUntilIdle()

        assertEquals(1, vm.listaPreguntas.value.size)
        val guardada = vm.listaPreguntas.value.first()
        assertEquals("Citación APA", guardada.categoria)

        // Editar
        vm.cargarFaqParaEdicion(guardada)
        assertTrue(vm.formFaq.value.estaEditando)
        vm.onCategoriaFaqChange("Formato")
        assertTrue(vm.guardarPreguntaFaq())
        advanceUntilIdle()
        assertEquals("Formato", vm.listaPreguntas.value.first().categoria)

        // Eliminar
        vm.eliminarPreguntaFaq(vm.listaPreguntas.value.first())
        advanceUntilIdle()
        assertTrue(vm.listaPreguntas.value.isEmpty())
    }

    @Test
    fun preguntasFiltradas_buscaSinSensibilidadATildesYPorCategoria() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = true)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.preguntasFiltradas.collect() }
        advanceUntilIdle()

        assertEquals(10, vm.preguntasFiltradas.value.size)

        // Búsqueda sin tildes debe encontrar "márgenes"
        vm.actualizarBusquedaFaq("margenes")
        advanceUntilIdle()
        assertEquals(1, vm.preguntasFiltradas.value.size)
        assertEquals("Formato", vm.preguntasFiltradas.value.first().categoria)

        // Búsqueda sin tildes en categoría "Citación APA"
        vm.actualizarBusquedaFaq("citacion")
        advanceUntilIdle()
        assertTrue(vm.preguntasFiltradas.value.isNotEmpty())

        // Combinación sin coincidencias
        vm.seleccionarCategoriaFaq("Entregas")
        vm.actualizarBusquedaFaq("interlineado")
        advanceUntilIdle()
        assertTrue(vm.preguntasFiltradas.value.isEmpty())
    }

    // --- Utilidades de fechas y programación de recordatorios ---

    @Test
    fun utilidadesFechaYRecordatorio_calculanCorrectamenteDiasYFechaObjetivo() {
        val hoy = LocalDate.of(2026, 11, 20)
        assertEquals(5L, TutorViewModel.calcularDiasRestantes("25/11/2026", hoy))
        assertEquals(0L, TutorViewModel.calcularDiasRestantes("20/11/2026", hoy))
        assertEquals(-2L, TutorViewModel.calcularDiasRestantes("18/11/2026", hoy))
        assertNull(TutorViewModel.calcularDiasRestantes("31/02/2026", hoy))

        val ahora = LocalDateTime.of(2026, 11, 20, 10, 0)
        val objetivoFuturo = RecordatorioScheduler.calcularFechaHoraRecordatorio("25/11/2026", ahora)
        assertEquals(LocalDateTime.of(2026, 11, 24, 9, 0), objetivoFuturo)

        val objetivoPasado = RecordatorioScheduler.calcularFechaHoraRecordatorio("19/11/2026", ahora)
        assertNull(objetivoPasado)

        assertEquals("citacion metodologica", TextoNormalizer.normalizar("  Citación Metodológica  "))
    }
}
