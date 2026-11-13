# Tutor Virtual ESUCAR

Proyecto desarrollado para la Evaluación Parcial 2 de la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)**.

## Descripción
Este proyecto es un MVP (Producto Mínimo Viable) de una app móvil que funciona como tutor virtual para los estudiantes de la Escuela de Suboficiales de Carabineros (ESUCAR). El objetivo es ayudar a los alumnos a estructurar su Trabajo de Título I, II y III, dándoles acceso rápido a las pautas de evaluación, un buscador de preguntas frecuentes (formato, norma APA, metodología) y un sistema para hacer seguimiento del avance de sus entregables sin depender de internet.


## Funcionalidades Implementadas
La aplicación está construida en **Kotlin** con **Jetpack Compose** bajo una arquitectura **MVVM**. Cumple con los siguientes requerimientos:

- **Navegación y UI:** Interfaz estructurada con barra de navegación inferior, modales y animaciones de estado (barras de progreso y transiciones de color).
- **Formularios Validados:** Creación de nuevos hitos y preguntas frecuentes. La lógica de validación (campos vacíos, longitud, formato de fechas) está desacoplada en el `ViewModel`.
- **Persistencia Local (Room):** Base de datos SQLite integrada. Incluye una precarga automática con los datos iniciales del caso ESUCAR (etapas de tesis y FAQ) al instalar la app por primera vez.
- **Integración de Recursos Nativos:**
  - **Cámara:** Solicitud de permisos en tiempo de ejecución para adjuntar evidencia fotográfica de los avances.
  - **Notificaciones Locales:** Sistema de alertas para recordar las fechas límite de entrega de los hitos académicos (requiere permiso en Android 13+).

## Pasos para ejecutar el proyecto
1. Clona este repositorio en tu entorno local:
   `git clone https://github.com/evaldes13144/Tutor_Virtual_ESUCAR.git`
2. Abre **Android Studio** y selecciona la opción *Open* para cargar la carpeta del proyecto.
3. Espera a que finalice la sincronización de dependencias de **Gradle** (el proyecto usa el catálogo TOML y tiene desactivado el built-in Kotlin en las properties para que KAPT compile Room sin errores).
4. Selecciona un emulador con **Android 8.0 (API 26)** o superior, o conecta un dispositivo físico.
5. Haz clic en **Run 'app'** (o presiona `Shift + F10`) para lanzar la aplicación[cite: 3].
