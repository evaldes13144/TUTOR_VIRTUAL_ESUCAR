package com.example.esucar_app.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.esucar_app.model.HitoTesis
import com.example.esucar_app.model.PerfilUsuario
import com.example.esucar_app.util.ImagenHelper
import com.example.esucar_app.util.NotificacionHelper
import com.example.esucar_app.util.RecordatorioScheduler
import com.example.esucar_app.viewmodel.TutorViewModel

@Composable
fun PantallaHitos(
    viewModel: TutorViewModel,
    esPantallaAncha: Boolean = false,
    onIrANuevoHito: () -> Unit,
    onEditarHito: (HitoTesis) -> Unit
) {
    val todosHitos by viewModel.listaHitos.collectAsState()
    val hitos by viewModel.hitosFiltrados.collectAsState()
    val etapaFiltro by viewModel.etapaFiltroHitos.collectAsState()
    val perfilActual by viewModel.perfilActual.collectAsState()
    val context = LocalContext.current

    var hitoPendienteNotificacion by remember { mutableStateOf<HitoTesis?>(null) }
    var hitoAEliminar by remember { mutableStateOf<HitoTesis?>(null) }
    var rutaFotoAmpliada by remember { mutableStateOf<String?>(null) }

    val etapasFiltro = listOf("Todas", "Trabajo de Título I", "Trabajo de Título II", "Trabajo de Título III")

    val ejecutarProgramacionYNotificacion: (HitoTesis) -> Unit = { hito ->
        val resultado = RecordatorioScheduler.programarRecordatorioHito(
            context = context,
            hitoId = hito.id,
            tituloHito = hito.titulo,
            fechaLimite = hito.fechaLimite
        )
        val enviada = NotificacionHelper.mostrarNotificacionHito(
            context = context,
            tituloHito = hito.titulo,
            fechaLimite = hito.fechaLimite,
            notificationId = hito.id,
            mensajeExtra = resultado.mensajeResumen
        )
        if (enviada) {
            Toast.makeText(context, resultado.mensajeResumen, Toast.LENGTH_LONG).show()
        }
    }

    val permisoNotificacionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        val pendiente = hitoPendienteNotificacion
        hitoPendienteNotificacion = null
        if (concedido && pendiente != null) {
            ejecutarProgramacionYNotificacion(pendiente)
        } else if (!concedido) {
            Toast.makeText(context, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val totalHitos = hitos.size.coerceAtLeast(1)
    val aprobados = hitos.count { it.estado == "Aprobado" || it.estado == "Enviado" }
    val progresoAnimado by animateFloatAsState(
        targetValue = if (hitos.isEmpty()) 0f else aprobados.toFloat() / totalHitos.toFloat(),
        label = "progreso_tesis"
    )

    val tituloResumen = if (etapaFiltro == "Todas") {
        "Estado de Avance - Trabajo de Título"
    } else {
        "Avance - $etapaFiltro"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = tituloResumen,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$aprobados de ${hitos.size} hitos enviados o aprobados",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progresoAnimado },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            etapasFiltro.forEach { etapa ->
                FilterChip(
                    selected = etapaFiltro == etapa,
                    onClick = { viewModel.seleccionarEtapaHitos(etapa) },
                    label = {
                        Text(
                            text = if (etapa == "Todas") "Todas" else etapa.replace("Trabajo de ", ""),
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (perfilActual == PerfilUsuario.PROFESOR_GUIA) {
                "Modo Profesor Guía: toca el estado o el check para aprobar hitos"
            } else {
                "Etapas y Entregables (Toca el estado para actualizar tu avance)"
            },
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (hitos.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (todosHitos.isEmpty()) {
                            "No tienes hitos registrados"
                        } else {
                            "Sin hitos en $etapaFiltro"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Agrega un nuevo capítulo o entregable para hacer seguimiento de tu Trabajo de Título.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onIrANuevoHito,
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeEsucar)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registrar Nuevo Hito", color = Color.White)
                    }
                }
            }
        } else {
            val renderTarjeta: @Composable (HitoTesis) -> Unit = { hito ->
                TarjetaHitoItem(
                    hito = hito,
                    esProfesorGuia = perfilActual == PerfilUsuario.PROFESOR_GUIA,
                    onCambiarEstado = {
                        val cambiado = viewModel.cambiarEstadoHito(hito)
                        if (!cambiado) {
                            Toast.makeText(
                                context,
                                "Este hito ya fue aprobado por el Profesor Guía",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (perfilActual == PerfilUsuario.ESTUDIANTE && hito.estado == "En desarrollo") {
                            Toast.makeText(
                                context,
                                "Marcado como Enviado (pendiente de aprobación docente)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onAprobarHito = {
                        if (viewModel.aprobarHito(hito)) {
                            RecordatorioScheduler.cancelarRecordatorio(context, hito.id)
                            Toast.makeText(context, "Hito aprobado: ${hito.titulo}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onEditar = { onEditarHito(hito) },
                    onVerFoto = { ruta -> rutaFotoAmpliada = ruta },
                    onRecordarFecha = {
                        if (NotificacionHelper.tienePermisoNotificaciones(context)) {
                            ejecutarProgramacionYNotificacion(hito)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            hitoPendienteNotificacion = hito
                            permisoNotificacionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onEliminar = { hitoAEliminar = hito }
                )
            }

            if (esPantallaAncha) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(hitos, key = { it.id }) { hito ->
                        renderTarjeta(hito)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(hitos, key = { it.id }) { hito ->
                        renderTarjeta(hito)
                    }
                }
            }
        }
    }

    hitoAEliminar?.let { hito ->
        AlertDialog(
            onDismissRequest = { hitoAEliminar = null },
            title = { Text("Eliminar hito", fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que deseas eliminar \"${hito.titulo}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        RecordatorioScheduler.cancelarRecordatorio(context, hito.id)
                        viewModel.eliminarHito(hito)
                        hitoAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { hitoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    rutaFotoAmpliada?.let { ruta ->
        VisorFotoDialog(
            rutaFoto = ruta,
            onDismiss = { rutaFotoAmpliada = null }
        )
    }
}

@Composable
fun TarjetaHitoItem(
    hito: HitoTesis,
    esProfesorGuia: Boolean,
    onCambiarEstado: () -> Unit,
    onAprobarHito: () -> Unit,
    onEditar: () -> Unit,
    onVerFoto: (String) -> Unit,
    onRecordarFecha: () -> Unit,
    onEliminar: () -> Unit
) {
    var mostrarRubrica by remember { mutableStateOf(false) }

    val colorEstado by animateColorAsState(
        targetValue = when (hito.estado) {
            "Aprobado" -> Color(0xFF2E7D32)
            "Enviado" -> Color(0xFF1565C0)
            "En desarrollo" -> Color(0xFFEF6C00)
            else -> Color(0xFF546E7A)
        },
        label = "color_estado"
    )

    val diasRestantes = remember(hito.fechaLimite) {
        TutorViewModel.calcularDiasRestantes(hito.fechaLimite)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hito.etapa,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = colorEstado,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable { onCambiarEstado() }
                ) {
                    Text(
                        text = hito.estado,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = hito.titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = hito.descripcion,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Límite: ${hito.fechaLimite}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = hito.formatoRequerido,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (diasRestantes != null && hito.estado != "Aprobado") {
                Spacer(modifier = Modifier.height(6.dp))
                val (textoPlazo, colorPlazo) = when {
                    diasRestantes < 0 -> "Vencido hace ${-diasRestantes} día(s)" to Color(0xFFC62828)
                    diasRestantes == 0L -> "Vence hoy" to Color(0xFFD84315)
                    diasRestantes <= 7L -> "Faltan $diasRestantes día(s)" to Color(0xFFEF6C00)
                    else -> "Faltan $diasRestantes días" to MaterialTheme.colorScheme.primary
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = colorPlazo,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = textoPlazo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorPlazo
                    )
                }
            }

            if (hito.tieneEvidenciaFoto) {
                Spacer(modifier = Modifier.height(8.dp))
                val bitmapEvidencia = remember(hito.rutaFotoEvidencia) {
                    hito.rutaFotoEvidencia?.let { ruta ->
                        ImagenHelper.decodificarBitmapEscalado(ruta, maxDimensionPx = 256)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = !hito.rutaFotoEvidencia.isNullOrBlank()) {
                        hito.rutaFotoEvidencia?.let { onVerFoto(it) }
                    }
                ) {
                    if (bitmapEvidencia != null) {
                        Image(
                            bitmap = bitmapEvidencia.asImageBitmap(),
                            contentDescription = "Ampliar foto de respaldo del hito",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = if (bitmapEvidencia != null) {
                            "Respaldo fotográfico adjunto (toca para ampliar)"
                        } else {
                            "Respaldo fotográfico adjunto"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { mostrarRubrica = !mostrarRubrica }) {
                    Icon(
                        imageVector = if (mostrarRubrica) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (mostrarRubrica) "Ocultar rúbrica" else "Ver rúbrica")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (esProfesorGuia && hito.estado != "Aprobado") {
                        IconButton(onClick = onAprobarHito) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Aprobar hito",
                                tint = Color(0xFF2E7D32)
                            )
                        }
                    }
                    IconButton(onClick = onEditar) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar hito",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onRecordarFecha) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "Programar alerta de entrega",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar hito",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(visible = mostrarRubrica) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Criterios de Evaluación Simplificados:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hito.criteriosRubrica,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VisorFotoDialog(
    rutaFoto: String,
    onDismiss: () -> Unit
) {
    val bitmapCompleto = remember(rutaFoto) {
        ImagenHelper.decodificarBitmapEscalado(rutaFoto, maxDimensionPx = 1440)
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Evidencia Fotográfica",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (bitmapCompleto != null) {
                    Image(
                        bitmap = bitmapCompleto.asImageBitmap(),
                        contentDescription = "Foto de respaldo en tamaño completo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Text(
                        text = "No se pudo cargar el archivo de imagen.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeEsucar)
                ) {
                    Text("Cerrar", color = Color.White)
                }
            }
        }
    }
}