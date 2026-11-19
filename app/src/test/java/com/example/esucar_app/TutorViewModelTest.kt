package com.example.esucar_app

import com.example.esucar_app.data.AppDatabase
import com.example.esucar_app.model.HitoTesis
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
    fun precargaInicial_insertaCuatroHitosYCuatroFaqsYEjecutaCallback() = runTest {
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
        assertEquals(4, vm.listaHitos.value.size)
        assertEquals(4, vm.listaPreguntas.value.size)
    }

    @Test
    fun poblarDatosIniciales_esIdempotenteSiYaExistenRegistros() = runTest {
        AppDatabase.poblarDatosIniciales(fakeDao)
        AppDatabase.poblarDatosIniciales(fakeDao)

        assertEquals(4, fakeDao.contarHitos())
        assertEquals(4, fakeDao.contarPreguntas())
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

        // Fechas inexistentes que antes pasaban el regex
        listOf("31/02/2026", "31/04/2026", "29/02/2025", "00/01/2026", "01/01/0000").forEach { fechaInvalida ->
            vm.onFechaChange(fechaInvalida)
            assertNotNull("Debió rechazar la fecha inválida $fechaInvalida", vm.formHito.value.errorFecha)
        }

        // Año bisiesto y fecha válida
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
    fun validarYGuardarHito_guardaLimpioConRutaDeFotoYReiniciaFormulario() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.listaHitos.collect() }

        vm.onEtapaChange("Trabajo de Título II")
        vm.onTituloChange("  Cap. 3: Diseño Metodológico  ")
        vm.onDescripcionChange("  Definición de muestra, instrumentos y validación en terreno.  ")
        vm.onFormatoChange("  PDF - APA 7  ")
        vm.onFechaChange("  15/12/2026  ")
        vm.onRubricaChange("  Instrumento validado (100%)  ")
        vm.onFotoCapturada(bitmap = null, rutaArchivo = "/data/evidencias/foto_1.jpg")

        val guardado = vm.validarYGuardarHito()
        advanceUntilIdle()

        assertTrue(guardado)
        assertTrue(vm.formHito.value.guardadoExitoso)
        assertEquals("", vm.formHito.value.titulo)
        assertEquals(1, vm.listaHitos.value.size)

        val hito = vm.listaHitos.value.first()
        assertEquals("Trabajo de Título II", hito.etapa)
        assertEquals("Cap. 3: Diseño Metodológico", hito.titulo)
        assertEquals("PDF - APA 7", hito.formatoRequerido)
        assertEquals("15/12/2026", hito.fechaLimite)
        assertEquals("En desarrollo", hito.estado)
        assertTrue(hito.tieneEvidenciaFoto)
        assertEquals("/data/evidencias/foto_1.jpg", hito.rutaFotoEvidencia)
    }

    // --- Transiciones de estado y eliminación de Hito ---

    @Test
    fun cambiarEstadoHito_recorreCicloCompletoYRecuperaEstadoDesconocido() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.listaHitos.collect() }

        val base = HitoTesis(
            id = 1,
            etapa = "Trabajo de Título I",
            titulo = "Cap. 1: Problema",
            descripcion = "Descripción metodológica de prueba.",
            formatoRequerido = "PDF",
            fechaLimite = "20/11/2026",
            estado = "Por iniciar",
            criteriosRubrica = "Criterio general (100%)"
        )
        fakeDao.insertarHito(base)
        advanceUntilIdle()

        val esperados = listOf("En desarrollo", "Enviado", "Aprobado", "Por iniciar")
        for (estadoEsperado in esperados) {
            vm.cambiarEstadoHito(vm.listaHitos.value.first())
            advanceUntilIdle()
            assertEquals(estadoEsperado, vm.listaHitos.value.first().estado)
        }

        // Estado inesperado debe recuperarse a "Por iniciar" sin lanzar excepción
        val desconocido = vm.listaHitos.value.first().copy(estado = "EstadoInvalido")
        fakeDao.actualizarHito(desconocido)
        advanceUntilIdle()
        vm.cambiarEstadoHito(vm.listaHitos.value.first())
        advanceUntilIdle()
        assertEquals("Por iniciar", vm.listaHitos.value.first().estado)
    }

    @Test
    fun eliminarHito_remueveElementoDeListaReactiva() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = true)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.listaHitos.collect() }
        advanceUntilIdle()

        val primero = vm.listaHitos.value.first()
        vm.eliminarHito(primero)
        advanceUntilIdle()

        assertEquals(3, vm.listaHitos.value.size)
        assertFalse(vm.listaHitos.value.any { it.id == primero.id })
    }

    // --- FAQ: creación, categoría, limpieza y filtrado ---

    @Test
    fun guardarPreguntaFaq_validaYRespetaCategoriaSeleccionada() = runTest {
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
        assertEquals("¿Cómo citar un decreto supremo en APA 7?", guardada.pregunta)
        assertEquals("", vm.formFaq.value.pregunta)
    }

    @Test
    fun limpiarFormularioFaq_borraErroresYTextoResidual() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = false)

        vm.onPreguntaFaqChange("corta")
        vm.onRespuestaFaqChange("corta")
        assertNotNull(vm.formFaq.value.errorPregunta)

        vm.limpiarFormularioFaq()
        assertEquals("", vm.formFaq.value.pregunta)
        assertEquals("", vm.formFaq.value.respuesta)
        assertNull(vm.formFaq.value.errorPregunta)
        assertNull(vm.formFaq.value.errorRespuesta)
    }

    @Test
    fun preguntasFiltradas_filtraPorCategoriaYTextoEnPreguntaORespuesta() = runTest {
        val vm = TutorViewModel(dao = fakeDao, poblarIniciales = true)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.preguntasFiltradas.collect() }
        advanceUntilIdle()

        // Sin filtro: todas las 4 iniciales
        assertEquals(4, vm.preguntasFiltradas.value.size)

        // Filtro por categoría
        vm.seleccionarCategoriaFaq("Formato")
        advanceUntilIdle()
        assertEquals(1, vm.preguntasFiltradas.value.size)
        assertEquals("Formato", vm.preguntasFiltradas.value.first().categoria)

        // Filtro por palabra presente solo en la respuesta (case-insensitive)
        vm.seleccionarCategoriaFaq("Todas")
        vm.actualizarBusquedaFaq("anonimizado")
        advanceUntilIdle()
        assertEquals(1, vm.preguntasFiltradas.value.size)
        assertEquals("Entregas", vm.preguntasFiltradas.value.first().categoria)

        // Combinación sin coincidencias
        vm.seleccionarCategoriaFaq("Metodología")
        advanceUntilIdle()
        assertTrue(vm.preguntasFiltradas.value.isEmpty())
    }
}
