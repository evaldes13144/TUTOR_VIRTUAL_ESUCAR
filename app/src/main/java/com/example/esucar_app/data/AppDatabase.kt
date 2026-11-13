package com.example.esucar_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.esucar_app.model.HitoTesis
import com.example.esucar_app.model.PreguntaFrecuente

@Database(
    entities = [HitoTesis::class, PreguntaFrecuente::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tutorDao(): TutorDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obtenerBaseDatos(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esucar_tutor_db"
                ).build()
                INSTANCIA = instance
                instance
            }
        }

        suspend fun poblarDatosIniciales(dao: TutorDao) {
            if (dao.contarHitos() == 0) {
                val hitosIniciales = listOf(
                    HitoTesis(
                        etapa = "Trabajo de Título I",
                        titulo = "Cap. 1: Planteamiento del Problema",
                        descripcion = "Definir problemática operativa en seguridad pública, pregunta de investigación, objetivo general y tres objetivos específicos.",
                        formatoRequerido = "PDF - Norma APA 7",
                        fechaLimite = "25/11/2026",
                        estado = "Aprobado",
                        criteriosRubrica = "• Pertinencia institucional en Carabineros (40%)\n• Claridad de objetivos verbo en infinitivo (30%)\n• Justificación y viabilidad operativa (30%)"
                    ),
                    HitoTesis(
                        etapa = "Trabajo de Título I",
                        titulo = "Cap. 2: Marco Teórico y Normativo",
                        descripcion = "Revisión de literatura sobre gestión policial, leyes vigentes y reglamentos institucionales aplicables al caso.",
                        formatoRequerido = "DOCX / PDF - APA 7",
                        fechaLimite = "05/12/2026",
                        estado = "En desarrollo",
                        criteriosRubrica = "• Mínimo 10 fuentes académicas o legales (40%)\n• Uso correcto de citas textuales y paráfrasis (30%)\n• Coherencia con los objetivos (30%)"
                    ),
                    HitoTesis(
                        etapa = "Trabajo de Título II",
                        titulo = "Cap. 3: Marco Metodológico",
                        descripcion = "Definir enfoque (cualitativo, cuantitativo o mixto), alcance del estudio, población/muestra de la unidad policial e instrumentos.",
                        formatoRequerido = "PDF - Norma APA 7",
                        fechaLimite = "15/12/2026",
                        estado = "Por iniciar",
                        criteriosRubrica = "• Coherencia del diseño metodológico (40%)\n• Definición correcta de la muestra (30%)\n• Validación del instrumento de recogida (30%)"
                    ),
                    HitoTesis(
                        etapa = "Trabajo de Título III",
                        titulo = "Cap. 4: Análisis de Resultados y Propuesta",
                        descripcion = "Procesamiento de datos levantados en terreno, gráficos estadísticos y conclusiones de mejora para la unidad.",
                        formatoRequerido = "PDF - Informe Final",
                        fechaLimite = "28/12/2026",
                        estado = "Por iniciar",
                        criteriosRubrica = "• Rigurosidad en el análisis de datos (40%)\n• Discusión contrastada con el Marco Teórico (30%)\n• Viabilidad de las conclusiones (30%)"
                    )
                )
                hitosIniciales.forEach { dao.insertarHito(it) }
            }

            if (dao.contarPreguntas() == 0) {
                val faqsIniciales = listOf(
                    PreguntaFrecuente(
                        pregunta = "¿Cómo cito una Ley o Reglamento Institucional de Carabineros en APA 7?",
                        categoria = "Citación APA",
                        respuesta = "En el texto se indica el nombre de la ley o número y el año: (Ley N° 18.961, 1990). En la lista de referencias: Nombre de la ley, Número, Fecha de publicación en el Diario Oficial."
                    ),
                    PreguntaFrecuente(
                        pregunta = "¿Cuál es el formato exigido para los márgenes, fuente e interlineado?",
                        categoria = "Formato",
                        respuesta = "Márgenes de 2,54 cm en todos los lados. Fuente Times New Roman de 12 pt o Arial de 11 pt, interlineado doble (2.0) y alineación a la izquierda sin justificar."
                    ),
                    PreguntaFrecuente(
                        pregunta = "¿Cuántos objetivos específicos debe tener el Trabajo de Título?",
                        categoria = "Metodología",
                        respuesta = "Se exigen entre 3 y 4 objetivos específicos redactados con verbos en infinitivo (ej: Diagnosticar, Analizar, Proponer) que desglosen de forma lógica el objetivo general."
                    ),
                    PreguntaFrecuente(
                        pregunta = "¿Puedo utilizar datos confidenciales de partes policiales?",
                        categoria = "Entregas",
                        respuesta = "No. Todo dato debe estar debidamente anonimizado y depurado sin incluir RUT, nombres de funcionarios ni direcciones exactas, cumpliendo la normativa de seguridad institucional."
                    )
                )
                faqsIniciales.forEach { dao.insertarPregunta(it) }
            }
        }
    }
}