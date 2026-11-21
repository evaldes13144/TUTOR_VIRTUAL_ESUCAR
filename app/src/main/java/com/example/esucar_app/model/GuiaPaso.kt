package com.example.esucar_app.model

data class ItemChecklistGuia(
    val id: String,
    val texto: String
)

data class GuiaPaso(
    val id: String,
    val numero: Int,
    val etapa: String,
    val titulo: String,
    val resumen: String,
    val pasosClave: List<String>,
    val ejemploEsucar: String,
    val erroresFrecuentes: List<String>,
    val checklist: List<ItemChecklistGuia>
)

object GuiaMetodologicaEsucar {
    val pasos: List<GuiaPaso> = listOf(
        GuiaPaso(
            id = "paso_1",
            numero = 1,
            etapa = "Trabajo de Título I",
            titulo = "Planteamiento del Problema y Objetivos",
            resumen = "Define con precisión una problemática real de gestión o seguridad pública en una unidad policial y establece objetivos medibles.",
            pasosClave = listOf(
                "Describe la situación actual con antecedentes concretos (sin exponer datos personales ni reservados).",
                "Formula una pregunta de investigación clara, delimitada en espacio y tiempo.",
                "Redacta 1 Objetivo General con verbo en infinitivo (ej: Analizar, Evaluar, Proponer).",
                "Desglosa entre 3 y 4 Objetivos Específicos secuenciales que permitan cumplir el objetivo general.",
                "Justifica la relevancia institucional y la viabilidad operativa del estudio."
            ),
            ejemploEsucar = "Objetivo General: Proponer un protocolo de optimización en la asignación de patrullajes preventivos en la 1ra. Comisaría durante el segundo semestre, mediante análisis de demanda operativa.",
            erroresFrecuentes = listOf(
                "Usar objetivos demasiado amplios o no verificables (ej: 'Mejorar toda la seguridad del país').",
                "Confundir objetivos específicos con actividades administrativas sueltas.",
                "Incluir nombres de funcionarios, RUT o direcciones exactas de partes policiales."
            ),
            checklist = listOf(
                ItemChecklistGuia("p1_c1", "Problema delimitado en ámbito operativo y temporal"),
                ItemChecklistGuia("p1_c2", "Pregunta de investigación coherente con el objetivo general"),
                ItemChecklistGuia("p1_c3", "Entre 3 y 4 objetivos específicos con verbos en infinitivo"),
                ItemChecklistGuia("p1_c4", "Justificación institucional y viabilidad redactadas en tercera persona")
            )
        ),
        GuiaPaso(
            id = "paso_2",
            numero = 2,
            etapa = "Trabajo de Título I",
            titulo = "Marco Teórico y Normativo",
            resumen = "Sustenta tu investigación con literatura académica actualizada, legislación vigente y reglamentación institucional de Carabineros.",
            pasosClave = listOf(
                "Identifica las variables o categorías clave de tus objetivos para organizar los subtítulos.",
                "Selecciona al menos 10 fuentes académicas, técnicas o normativas confiables (últimos 5–10 años).",
                "Incluye normativa vigente pertinente (ej: Ley Orgánica Constitucional N° 18.961, reglamentos y manuales públicos).",
                "Aplica citación APA 7 en todas las citas textuales y paráfrasis, y valida correspondencia con Referencias."
            ),
            ejemploEsucar = "Cita normativa en APA 7: De acuerdo con la Ley Orgánica Constitucional de Carabineros de Chile (Ley N° 18.961, 1990), la función preventiva constituye un eje estructural de la gestión territorial.",
            erroresFrecuentes = listOf(
                "Copiar definiciones de sitios no académicos (blogs, Wikipedia) sin contraste técnico.",
                "Citar autores en el texto que luego no aparecen en la lista de Referencias.",
                "Acumular teoría desconectada de la problemática definida en el Capítulo 1."
            ),
            checklist = listOf(
                ItemChecklistGuia("p2_c1", "Mínimo 10 fuentes académicas o normativas verificadas"),
                ItemChecklistGuia("p2_c2", "Marco legal e institucional vigente citado correctamente"),
                ItemChecklistGuia("p2_c3", "Todas las citas en texto coinciden con la sección de Referencias APA 7"),
                ItemChecklistGuia("p2_c4", "Formato de márgenes 2,54 cm e interlineado 2.0 aplicado")
            )
        ),
        GuiaPaso(
            id = "paso_3",
            numero = 3,
            etapa = "Trabajo de Título II",
            titulo = "Marco Metodológico",
            resumen = "Define cómo investigarás el problema: enfoque, alcance, diseño, población/muestra e instrumentos de levantamiento.",
            pasosClave = listOf(
                "Elige y justifica el enfoque: cuantitativo (datos medibles), cualitativo (percepciones/procesos) o mixto.",
                "Determina el alcance del estudio (exploratorio, descriptivo o correlacional) y el diseño.",
                "Define población, criterios de inclusión y tamaño de la muestra de la unidad o servicio.",
                "Diseña los instrumentos (encuesta, pauta de entrevista, lista de cotejo) alineados a cada objetivo específico.",
                "Solicita validación del instrumento por juicio de expertos o profesor guía antes de aplicarlo."
            ),
            ejemploEsucar = "Enfoque mixto con alcance descriptivo, aplicando encuesta estructurada con escala Likert a una muestra censal de 45 funcionarios operativos y entrevista semiestructurada a 3 jefes de turno.",
            erroresFrecuentes = listOf(
                "Elegir un enfoque que no permite responder la pregunta de investigación.",
                "Aplicar encuestas sin revisar su coherencia con los objetivos específicos.",
                "No definir cómo se protegerá el anonimato de los participantes."
            ),
            checklist = listOf(
                ItemChecklistGuia("p3_c1", "Enfoque, alcance y diseño metodológico justificados"),
                ItemChecklistGuia("p3_c2", "Población, muestra y criterios de selección definidos"),
                ItemChecklistGuia("p3_c3", "Instrumentos vinculados a cada objetivo específico"),
                ItemChecklistGuia("p3_c4", "Instrumento revisado/validado por el profesor guía")
            )
        ),
        GuiaPaso(
            id = "paso_4",
            numero = 4,
            etapa = "Trabajo de Título II",
            titulo = "Recogida de Información en Terreno",
            resumen = "Ejecuta el levantamiento de datos de forma ordenada, ética y segura cumpliendo los protocolos institucionales.",
            pasosClave = listOf(
                "Coordina la autorización formal en la unidad correspondiente antes de levantar datos.",
                "Aplica los instrumentos asegurando consentimiento informado y anonimato de los encuestados.",
                "Depura los registros eliminando RUT, nombres, patentes y direcciones exactas.",
                "Documenta el avance con respaldo fotográfico de instrumentos en blanco o bitácoras (sin datos confidenciales)."
            ),
            ejemploEsucar = "Registro anonimizado mediante códigos internos (F-01 a F-45) y consolidación en planilla cifrada para tabulación estadística de tiempos de respuesta por cuadrante.",
            erroresFrecuentes = listOf(
                "Fotografiar o almacenar partes policiales originales con información reservada.",
                "Recolectar menos respuestas que la muestra comprometida sin justificarlo metodológicamente.",
                "Perder trazabilidad de fechas y condiciones en que se aplicaron los instrumentos."
            ),
            checklist = listOf(
                ItemChecklistGuia("p4_c1", "Autorización y coordinación previa con la unidad completadas"),
                ItemChecklistGuia("p4_c2", "Datos depurados y 100% anonimizados (sin RUT ni nombres)"),
                ItemChecklistGuia("p4_c3", "Base de datos o transcripciones ordenadas para análisis")
            )
        ),
        GuiaPaso(
            id = "paso_5",
            numero = 5,
            etapa = "Trabajo de Título III",
            titulo = "Análisis de Resultados, Propuesta y Defensa",
            resumen = "Procesa los hallazgos, contrástalos con el Marco Teórico, diseña una propuesta viable y prepara tu defensa final.",
            pasosClave = listOf(
                "Organiza los resultados en tablas y gráficos claros numerados según norma APA 7.",
                "Interpreta los hallazgos contrastándolos con la teoría y normativa del Capítulo 2.",
                "Formula una propuesta de mejora concreta (objetivo, responsables, plazos, recursos e indicadores).",
                "Redacta conclusiones respondiendo uno a uno a todos los objetivos específicos.",
                "Prepara la presentación de defensa oral enfocada en problema, método, hallazgos y propuesta."
            ),
            ejemploEsucar = "Propuesta de tablero semanal de control de turnos con 3 indicadores KPI (cobertura preventiva, tiempo medio de despacho y cumplimiento de rondas), evaluable en 90 días sin costo adicional.",
            erroresFrecuentes = listOf(
                "Presentar gráficos sin análisis interpretativo ni contraste teórico.",
                "Proponer soluciones inviables presupuestaria o reglamentariamente.",
                "Concluir temas que nunca fueron investigados en los objetivos."
            ),
            checklist = listOf(
                ItemChecklistGuia("p5_c1", "Tablas y figuras formateadas según APA 7 con análisis escrito"),
                ItemChecklistGuia("p5_c2", "Discusión contrastada con el Marco Teórico y Normativo"),
                ItemChecklistGuia("p5_c3", "Propuesta de mejora con plan de acción e indicadores viables"),
                ItemChecklistGuia("p5_c4", "Conclusiones responden directamente a cada objetivo y defensa ensayada")
            )
        )
    )

    val totalItemsChecklist: Int = pasos.sumOf { it.checklist.size }
}
