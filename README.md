# Tutor Virtual ESUCAR

Aplicación móvil Android desarrollada para la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)** — caso **Escuela de Suboficiales de Carabineros de Chile (ESUCAR)**.

## Equipo de Desarrollo
- **Integrante 1:** *(Completar Nombre y Apellido)*
- **Integrante 2:** *(Completar Nombre y Apellido)*
- **Equipo N°:** *(Completar número de equipo asignado)*
- **Tablero de Planificación (Trello):** *(Agregar enlace público al tablero Trello del equipo)*

## Descripción del Proyecto
**Tutor Virtual ESUCAR** es una aplicación móvil diseñada para acompañar a los estudiantes de ESUCAR en el desarrollo de su **Trabajo de Título I, II y III** sin depender de conexión permanente a internet. Centraliza una guía metodológica paso a paso con listas de chequeo verificables, un buscador de preguntas frecuentes (norma APA 7, formato, metodología y entregas), seguimiento de hitos con rúbricas simplificadas y alertas locales de fechas límite.

## Funcionalidades Implementadas
Construida en **Kotlin** y **Jetpack Compose** bajo arquitectura **MVVM + Repository + Room**:

- **Navegación Adaptativa (Teléfono y Tablet):**
  - Barra inferior (`NavigationBar`) en teléfonos y barra lateral (`NavigationRail`) + grilla de 2 columnas en tablets y pantallas anchas (`>= 600dp`).
  - Transiciones animadas entre vistas y soporte de tema claro/oscuro con alto contraste institucional.
- **Perfiles de Usuario (Estudiante y Profesor Guía):**
  - Selector de perfil desde la barra superior persistido localmente.
  - **Estudiante:** consulta la guía metodológica, marca checklists, crea/edita sus hitos y actualiza avance hasta *Enviado*.
  - **Profesor Guía:** supervisa entregas, aprueba hitos directamente y administra (edita/elimina) consultas oficiales del banco FAQ.
- **Guía Metodológica Paso a Paso:**
  - 5 etapas completas (*Planteamiento*, *Marco Teórico y Normativo*, *Marco Metodológico*, *Recogida en Terreno* y *Análisis y Propuesta*) con ejemplos aplicados a ESUCAR, errores frecuentes y checklist interactivo persistido en Room.
- **Seguimiento de Hitos y Formularios Validados:**
  - Creación y edición de hitos con selector visual de calendario (`DatePickerDialog`) y validación estricta de fechas, longitud y formatos académicos desacoplada en `TutorViewModel`.
  - Indicadores visuales estandarizados (ícono de error y validación) por campo, filtro por asignatura (*Título I, II y III*), orden cronológico y contador de días restantes.
- **Buscador FAQ Insensible a Tildes:**
  - Banco inicial con 10 consultas oficiales depuradas de ESUCAR, búsqueda normalizada sin sensibilidad a tildes ni mayúsculas, filtro por categoría y modal validado con selección de categoría.
- **Persistencia Local (Room v3):**
  - Base de datos SQLite con migraciones versionadas (`MIGRATION_1_2`, `MIGRATION_2_3`), tablas `hitos_tesis`, `preguntas_frecuentes` y `checklist_guia`, más precarga automática única.
- **Recursos Nativos del Dispositivo:**
  - **Cámara HD (`FileProvider` + `TakePicture`):** solicitud de permiso `CAMERA` en tiempo de ejecución, almacenamiento local persistente, visor ampliado y recordatorio de anonimización de datos sensibles.
  - **Notificaciones Locales Programadas (`WorkManager`):** solicitud de permiso `POST_NOTIFICATIONS` (Android 13+), programación de recordatorios para el día anterior a la fecha límite, cancelación automática al aprobar/eliminar y `PendingIntent` para abrir la app al tocar la alerta.

## Arquitectura del Proyecto
```text
app/src/main/java/com/example/esucar_app/
├── data/          # AppDatabase (Room v3 + migraciones) y TutorDao
├── model/         # HitoTesis, PreguntaFrecuente, ChecklistGuiaEstado, GuiaPaso, PerfilUsuario y estados de formulario
├── repository/    # TutorRepository (capa de abstracción sobre Room)
├── ui/            # AppNavigation, PantallaHitos, PantallaGuia, PantallaFormularioHito, PantallaFaq y theme/
├── util/          # NotificacionHelper, RecordatorioScheduler, RecordatorioHitoWorker, ImagenHelper, TextoNormalizer
└── viewmodel/     # TutorViewModel (lógica, validaciones y StateFlow reactivos)
```

## Pasos para Ejecutar el Proyecto
1. Clona este repositorio en tu entorno local:
   ```bash
   git clone https://github.com/evaldes13144/TUTOR_VIRTUAL_ESUCAR.git
   ```
2. Abre **Android Studio** y selecciona **Open** sobre la carpeta del proyecto.
3. Espera la sincronización de **Gradle** (catálogo de versiones en `gradle/libs.versions.toml` con Room `2.7.1`, Compose y WorkManager).
4. Selecciona un emulador o dispositivo físico (teléfono o tablet) con **Android 8.0 (API 26)** o superior.
5. Presiona **Run 'app'** (`Shift + F10`) para compilar y lanzar la aplicación.

## Pruebas Automatizadas
Para ejecutar la suite de pruebas unitarias y verificación estática desde terminal:
```bash
./gradlew testDebugUnitTest lintDebug
```
