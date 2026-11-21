package com.example.esucar_app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.esucar_app.model.HitoTesis
import com.example.esucar_app.util.ImagenHelper
import com.example.esucar_app.util.NotificacionHelper
import com.example.esucar_app.util.RecordatorioScheduler
import com.example.esucar_app.viewmodel.TutorViewModel
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormularioHito(
    viewModel: TutorViewModel,
    onVolverAHitos: () -> Unit
) {
    val formState by viewModel.formHito.collectAsState()
    val context = LocalContext.current
    val etapasDisponibles = listOf("Trabajo de Título I", "Trabajo de Título II", "Trabajo de Título III")

    var hitoPendienteAlerta by remember { mutableStateOf<HitoTesis?>(null) }
    var archivoFotoPendiente by remember { mutableStateOf<File?>(null) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarFotoAmpliada by remember { mutableStateOf(false) }

    val finalizarYProgramarAlerta: (HitoTesis, Boolean) -> Unit = { hitoGuardado, tienePermiso ->
        val resultado = RecordatorioScheduler.programarRecordatorioHito(
            context = context,
            hitoId = hitoGuardado.id,
            tituloHito = hitoGuardado.titulo,
            fechaLimite = hitoGuardado.fechaLimite
        )
        if (tienePermiso) {
            NotificacionHelper.mostrarNotificacionHito(
                context = context,
                tituloHito = hitoGuardado.titulo,
                fechaLimite = hitoGuardado.fechaLimite,
                notificationId = hitoGuardado.id,
                mensajeExtra = resultado.mensajeResumen
            )
        }
        val prefijo = if (formState.estaEditando) "Hito actualizado" else "Hito guardado"
        Toast.makeText(context, "$prefijo. ${resultado.mensajeResumen}", Toast.LENGTH_LONG).show()
        onVolverAHitos()
    }

    val permisoNotificacionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        val pendiente = hitoPendienteAlerta
        hitoPendienteAlerta = null
        if (pendiente != null) {
            finalizarYProgramarAlerta(pendiente, concedido)
        } else {
            onVolverAHitos()
        }
    }

    val camaraHdLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        val archivo = archivoFotoPendiente
        archivoFotoPendiente = null
        if (exito && archivo != null && archivo.exists() && archivo.length() > 0L) {
            viewModel.onFotoRutaCapturada(archivo.absolutePath)
        } else if (archivo != null) {
            ImagenHelper.eliminarArchivoSiExiste(archivo.absolutePath)
        }
    }

    val lanzarCamaraHd: () -> Unit = {
        val nuevoArchivo = ImagenHelper.crearArchivoEvidencia(context)
        val uri = ImagenHelper.obtenerUriFileProvider(context, nuevoArchivo)
        archivoFotoPendiente = nuevoArchivo
        camaraHdLauncher.launch(uri)
    }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            lanzarCamaraHd()
        } else {
            Toast.makeText(
                context,
                "Se requiere permiso de cámara para adjuntar evidencia",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (formState.estaEditando) "Editar Hito Académico" else "Registrar Nuevo Hito o Entrega",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Completa los datos metodológicos del entregable para programar su seguimiento.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (formState.estaEditando) {
                    TextButton(onClick = {
                        viewModel.cancelarEdicionHito()
                        onVolverAHitos()
                    }) {
                        Text("Cancelar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Asignatura:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                etapasDisponibles.forEach { etapa ->
                    FilterChip(
                        selected = formState.etapa == etapa,
                        onClick = { viewModel.onEtapaChange(etapa) },
                        label = { Text(etapa.replace("Trabajo de ", ""), fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formState.titulo,
                onValueChange = { viewModel.onTituloChange(it) },
                label = { Text("Título del capítulo o hito") },
                placeholder = { Text("Ej: Cap. 2: Marco Normativo") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                trailingIcon = {
                    IconoEstadoCampo(
                        tieneError = formState.errorTitulo != null,
                        esValido = formState.errorTitulo == null && formState.titulo.trim().length >= 6
                    )
                },
                isError = formState.errorTitulo != null,
                supportingText = {
                    formState.errorTitulo?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.descripcion,
                onValueChange = { viewModel.onDescripcionChange(it) },
                label = { Text("Descripción y alcance del avance") },
                placeholder = { Text("Detalla los contenidos que debe incluir esta entrega...") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                trailingIcon = {
                    IconoEstadoCampo(
                        tieneError = formState.errorDescripcion != null,
                        esValido = formState.errorDescripcion == null && formState.descripcion.trim().length >= 15
                    )
                },
                isError = formState.errorDescripcion != null,
                supportingText = {
                    formState.errorDescripcion?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.formatoRequerido,
                onValueChange = { viewModel.onFormatoChange(it) },
                label = { Text("Formato exigido") },
                placeholder = { Text("Ej: PDF - Norma APA 7") },
                leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                trailingIcon = {
                    IconoEstadoCampo(
                        tieneError = formState.errorFormato != null,
                        esValido = formState.errorFormato == null && formState.formatoRequerido.isNotBlank()
                    )
                },
                isError = formState.errorFormato != null,
                supportingText = {
                    formState.errorFormato?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.fechaLimite,
                onValueChange = { viewModel.onFechaChange(it) },
                label = { Text("Fecha límite (DD/MM/AAAA)") },
                placeholder = { Text("Ej: 18/12/2026") },
                leadingIcon = {
                    IconButton(onClick = { mostrarDatePicker = true }) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Abrir selector de calendario",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconoEstadoCampo(
                            tieneError = formState.errorFecha != null,
                            esValido = formState.errorFecha == null && formState.fechaLimite.isNotBlank()
                        )
                        IconButton(onClick = { mostrarDatePicker = true }) {
                            Icon(
                                Icons.Default.EditCalendar,
                                contentDescription = "Elegir fecha en calendario"
                            )
                        }
                    }
                },
                isError = formState.errorFecha != null,
                supportingText = {
                    val textoAyuda = formState.errorFecha ?: "Toca el ícono de calendario para elegir fecha fácilmente"
                    Text(
                        text = textoAyuda,
                        color = if (formState.errorFecha != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.criteriosRubrica,
                onValueChange = { viewModel.onRubricaChange(it) },
                label = { Text("Criterios de pauta / rúbrica") },
                placeholder = { Text("Ej: Coherencia de objetivos (50%), Citación APA (50%)") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = null) },
                trailingIcon = {
                    IconoEstadoCampo(
                        tieneError = formState.errorRubrica != null,
                        esValido = formState.errorRubrica == null && formState.criteriosRubrica.trim().length >= 10
                    )
                },
                isError = formState.errorRubrica != null,
                supportingText = {
                    formState.errorRubrica?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val tienePermiso = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (tienePermiso) {
                        lanzarCamaraHd()
                    } else {
                        permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (formState.rutaFotoEvidencia != null) {
                        "Reemplazar Foto de Evidencia (Cámara HD)"
                    } else {
                        "Capturar Foto de Respaldo / Avance (Cámara HD)"
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Seguridad institucional: evita fotografiar partes policiales con RUT, nombres o direcciones personales.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val bitmapPreview = remember(formState.rutaFotoEvidencia) {
                formState.rutaFotoEvidencia?.let { ruta ->
                    ImagenHelper.decodificarBitmapEscalado(ruta, maxDimensionPx = 384)
                }
            }

            AnimatedVisibility(visible = bitmapPreview != null) {
                bitmapPreview?.let { bmp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Evidencia capturada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { mostrarFotoAmpliada = true }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Evidencia fotográfica lista",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Toca la imagen para ampliar.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.eliminarFotoFormulario() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Quitar foto adjunta",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.validarYGuardarHito { hitoGuardado ->
                        if (NotificacionHelper.tienePermisoNotificaciones(context)) {
                            finalizarYProgramarAlerta(hitoGuardado, true)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            hitoPendienteAlerta = hitoGuardado
                            permisoNotificacionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            finalizarYProgramarAlerta(hitoGuardado, false)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VerdeEsucar),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (formState.estaEditando) {
                        "Actualizar Hito y Recordatorio"
                    } else {
                        "Guardar Hito y Programar Alerta"
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    if (mostrarDatePicker) {
        val initialMillis = remember(formState.fechaLimite) {
            TutorViewModel.parsearFecha(formState.fechaLimite)
                ?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()
                ?.toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        val formateada = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        viewModel.onFechaChange(formateada)
                    }
                    mostrarDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (mostrarFotoAmpliada && !formState.rutaFotoEvidencia.isNullOrBlank()) {
        VisorFotoDialog(
            rutaFoto = formState.rutaFotoEvidencia!!,
            onDismiss = { mostrarFotoAmpliada = false }
        )
    }
}

@Composable
internal fun IconoEstadoCampo(
    tieneError: Boolean,
    esValido: Boolean
) {
    when {
        tieneError -> Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Campo con error",
            tint = MaterialTheme.colorScheme.error
        )
        esValido -> Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Campo válido",
            tint = Color(0xFF2E7D32)
        )
    }
}