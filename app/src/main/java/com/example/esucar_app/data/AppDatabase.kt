package com.example.esucar_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.esucar_app.model.ChecklistGuiaEstado
import com.example.esucar_app.model.HitoTesis
import com.example.esucar_app.model.PreguntaFrecuente

@Database(
    entities = [HitoTesis::class, PreguntaFrecuente::class, ChecklistGuiaEstado::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tutorDao(): TutorDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE hitos_tesis ADD COLUMN rutaFotoEvidencia TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS checklist_guia (
                        itemId TEXT NOT NULL,
                        completado INTEGER NOT NULL,
                        PRIMARY KEY(itemId)
                    )
                    """.trimIndent()
                )
            }
        }

        fun obtenerBaseDatos(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esucar_tutor_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
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
                        etapa = "Trabajo de Título II",
                        titulo = "Avance Terreno: Recogida de Información",
                        descripcion = "Aplicación de encuestas o entrevistas validadas en la unidad, consolidación y depuración anonimizada de los registros.",
                        formatoRequerido = "PDF - Evidencia APA 7",
                        fechaLimite = "21/12/2026",
                        estado = "Por iniciar",
                        criteriosRubrica = "• Cumplimiento de la muestra comprometida (40%)\n• Anonimización total de datos sensibles (35%)\n• Orden y trazabilidad de la matriz de datos (25%)"
                    ),
                    HitoTesis(
                        etapa = "Trabajo de Título III",
                        titulo = "Cap. 4: Análisis de Resultados y Propuesta",
                        descripcion = "Procesamiento de datos levantados en terreno, gráficos estadísticos, propuesta de mejora viable y preparación de defensa.",
                        formatoRequerido = "PDF - Informe Final APA 7",
                        fechaLimite = "28/12/2026",
                        estado = "Por iniciar",
                        criteriosRubrica = "• Rigurosidad en el análisis de datos (40%)\n• Discusión contrastada con el Marco Teórico (30%)\n• Viabilidad institucional de la propuesta (30%)"
                    )
                )
                hitosIniciales.forEach { dao.insertarHito(it) }
            }

            if (dao.contarPreguntas() == 0) {
                val faqsIniciales = listOf(
                    PreguntaFrecuente(
                        pregunta = "¿Cómo cito una Ley o Reglamento Institucional de Carabineros en APA 7?",
                        categoria = "Citación APA",
                        respuesta = "En el texto se indica el nombre o número de la ley y el año: (Ley N° 18.961, 1990). En la lista de referencias: Nombre de la ley, Número, Fecha de publicación en el Diario Oficial y enlace oficial si corresponde."
                    ),
                    PreguntaFrecuente(
                        pregunta = "¿Cómo se citan tablas y figuras elaboradas con datos propios en APA 7?",
                        categoria = "Citación APA",
                        respuesta = "Cada tabla o figura lleva número en negrita (Tabla 1), título en cursiva en la línea siguiente y al pie una nota aclaratoria: 'Nota. Elaboración propia a partir de datos levantados en la unidad (2026).'"
                    ),
                    PreguntaFrecuente(
                        pregunta = "¿Cuál es el formato exigido para los márgenes, fuente e interlineado?",
                        categoria = "Formato",
                        respuesta = "Márgenes de 2,54 cm en todos los lados. Fuente Times New Roman de 12 pt o Arial de 11 pt, interlineado doble (2.0), sangría de primera línea de 1,27 cm y alineación a la izquierda sin justificar."
                    ),
                    PreguntaFrecuente(
                        pregunta = "¿En qué persona gramatical se debe redactar el informe de Trabajo de Título?",
                        categoria = "Formato",
                        respuesta = "Siempre en tercera persona singular o forma impersonal (ej: 'Se analizó la dotación operativa' en lugar de 'Nosotros analizamos'), manteniendo lenguaje técnico, formal y objetivo."
                    ),
                    PreguntaFrecuente(
                        pregunta = "¿Cuál es la estructura oficial mínima del informe final de Trabajo de Título?",
                        categoria = "Formato",
                        respuesta = "Portada institucional ESUCAR, índice general y de tablas/figuras, resumen, introducción, Cap. 1 Planteamiento, Cap. 2 Marco Teórico y Normativo, Cap. 3 Metodología, Cap. 4 Resultados y Propuesta, Conclusiones, Referencias APA 7 y Anexos."
                    ),
                    PreguntaFrecuente(
                        pregunta = "¿Cuántos objetivos específicos debe tener el Trabajo de Título?",
                        categoria = "Metodología",
                        respuesta = "Se exigen entre 3 y 4 objetivos específicos redactados con verbos en infinitivo (ej: Diagnosticar, Analizar, Diseñar, Proponer) que desglosen de forma lógica y secuencial el objetivo general."
                    ),
                    PreguntaFrecuente(
                        pregunta = "¿Cuándo elegir un enfoque cualitativo, cuantitativo o mixto?",
                        categoria = "Metodología",
                        respuesta = "Usa enfoque cuantitativo si medirás indicadores numéricos (tiempos, tasas, encuestas cerradas), cualitativo si explorarás percepciones o procesos (entrevistas, grupos focales), y mixto si combinas ambos para mayor profundidad."
                    ),
                    PreguntaFrecuente(
                        pregunta = "¿Cómo valido un instrumento de encuesta o entrevista antes de aplicarlo?",
                        categoria = "Metodología",
                        respuesta = "Debes presentar el borrador del instrumento junto a una matriz de coherencia con tus objetivos al profesor guía y/o juicio de expertos del área operativa, incorporando sus observaciones antes del terreno."
                    ),
                    PreguntaFrecuente(
                        pregunta = "¿Puedo utilizar datos confidenciales de partes policiales?",
                        categoria = "Entregas",
                        respuesta = "No. Todo dato debe estar debidamente anonimizado y depurado sin incluir RUT, nombres de funcionarios o ciudadanos, patentes ni direcciones exactas, cumpliendo estrictamente la normativa de seguridad institucional."
                    ),
                    PreguntaFrecuente(
                        pregunta = "¿Cómo deben nombrarse y entregarse los archivos de avance al profesor guía?",
                        categoria = "Entregas",
                        respuesta = "Sube el documento en formato PDF (o DOCX si el docente solicita revisión con control de cambios) usando el formato: TT1_Apellido_Cap1_Fecha.pdf, respetando el calendario académico y la rúbrica de la etapa."
                    )
                )
                faqsIniciales.forEach { dao.insertarPregunta(it) }
            }
        }
    }
}