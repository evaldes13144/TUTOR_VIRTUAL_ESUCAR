package com.example.esucar_app.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.esucar_app.util.NotificacionHelper
import com.example.esucar_app.viewmodel.TutorViewModel
import java.io.File
import java.io.FileOutputStream

private fun guardarBitmapEnArchivo(context: Context, bitmap: Bitmap): String? {
    return runCatching {
        val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
        val archivo = File(carpeta, "evidencia_${System.currentTimeMillis()}.jpg")
        FileOutputStream(archivo).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        archivo.absolutePath
    }.getOrNull()
}

@Composable
fun PantallaFormularioHito(
    viewModel: TutorViewModel,
    onVolverAHitos: () -> Unit
) {
    val formState by viewModel.formHito.collectAsState()
    val context = LocalContext.current
    val etapasDisponibles = listOf("Trabajo de Título I", "Trabajo de Título II", "Trabajo de Título III")

    var alertaPendiente by remember { mutableStateOf<Pair<String, String>?>(null) }

    val permisoNotificacionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        val pendiente = alertaPendiente
        alertaPendiente = null
        if (concedido && pendiente != null) {
            NotificacionHelper.mostrarNotificacionHito(context, pendiente.first, pendiente.second)
        }
        Toast.makeText(context, "Hito guardado correctamente", Toast.LENGTH_SHORT).show()
        onVolverAHitos()
    }

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val rutaArchivo = guardarBitmapEnArchivo(context, bitmap)
            viewModel.onFotoCapturada(bitmap, rutaArchivo)
        }
    }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            camaraLauncher.launch()
        } else {
            Toast.makeText(
                context,
                "Se requiere permiso de cámara para adjuntar evidencia",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Registrar Nuevo Hito o Entrega",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = VerdeEsucar
        )
        Text(
            text = "Completa los datos metodológicos del entregable para programar su seguimiento.",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(text = "Asignatura:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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
                if (formState.errorTitulo != null) {
                    Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                } else if (formState.titulo.trim().length >= 6) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Correcto", tint = VerdeEsucar)
                }
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
                if (formState.errorDescripcion != null) {
                    Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                }
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
                if (formState.errorFormato != null) {
                    Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                }
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
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            trailingIcon = {
                if (formState.errorFecha != null) {
                    Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                }
            },
            isError = formState.errorFecha != null,
            supportingText = {
                formState.errorFecha?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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
                if (formState.errorRubrica != null) {
                    Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                }
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
                    camaraLauncher.launch()
                } else {
                    permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Capturar Foto de Respaldo / Avance (Cámara)")
        }

        AnimatedVisibility(visible = formState.fotoEvidencia != null) {
            formState.fotoEvidencia?.let { bmp ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = VerdeClaroEsucar)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Evidencia capturada",
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Evidencia fotográfica lista",
                                fontWeight = FontWeight.Bold,
                                color = VerdeEsucar
                            )
                            Text(
                                text = "Se guardará en el almacenamiento local del hito.",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val tituloActual = formState.titulo.trim()
                val fechaActual = formState.fechaLimite.trim()
                if (viewModel.validarYGuardarHito()) {
                    if (NotificacionHelper.tienePermisoNotificaciones(context)) {
                        NotificacionHelper.mostrarNotificacionHito(context, tituloActual, fechaActual)
                        Toast.makeText(context, "Hito guardado correctamente", Toast.LENGTH_SHORT).show()
                        onVolverAHitos()
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        alertaPendiente = tituloActual to fechaActual
                        permisoNotificacionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        Toast.makeText(context, "Hito guardado correctamente", Toast.LENGTH_SHORT).show()
                        onVolverAHitos()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = VerdeEsucar),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar Hito y Enviar Alerta", fontWeight = FontWeight.Bold)
        }
    }
}