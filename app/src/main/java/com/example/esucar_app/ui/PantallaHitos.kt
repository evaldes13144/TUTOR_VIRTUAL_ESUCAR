package com.example.esucar_app.ui

import android.Manifest
import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esucar_app.model.HitoTesis
import com.example.esucar_app.util.NotificacionHelper
import com.example.esucar_app.viewmodel.TutorViewModel

@Composable
fun PantallaHitos(
    viewModel: TutorViewModel,
    onIrANuevoHito: () -> Unit
) {
    val hitos by viewModel.listaHitos.collectAsState()
    val context = LocalContext.current

    var hitoPendienteNotificacion by remember { mutableStateOf<HitoTesis?>(null) }
    var hitoAEliminar by remember { mutableStateOf<HitoTesis?>(null) }

    val permisoNotificacionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        val pendiente = hitoPendienteNotificacion
        hitoPendienteNotificacion = null
        if (concedido && pendiente != null) {
            val enviada = NotificacionHelper.mostrarNotificacionHito(
                context,
                pendiente.titulo,
                pendiente.fechaLimite
            )
            if (enviada) {
                Toast.makeText(
                    context,
                    "Notificación enviada para ${pendiente.titulo}",
                    Toast.LENGTH_SHORT
                ).show()
            }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7F6))
            .padding(16.dp)
    ) {

        Card(
            colors = CardDefaults.cardColors(containerColor = VerdeClaroEsucar),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Estado de Avance - Trabajo de Título",
                    fontWeight = FontWeight.Bold,
                    color = VerdeEsucar,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$aprobados de ${hitos.size} hitos enviados o aprobados",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progresoAnimado },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = VerdeEsucar,
                    trackColor = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Etapas y Entregables (Toca el estado para actualizar)",
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (hitos.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No tienes hitos registrados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = VerdeEsucar
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Agrega un nuevo capítulo o entregable para hacer seguimiento de tu Trabajo de Título.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onIrANuevoHito,
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeEsucar)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registrar Nuevo Hito")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(hitos, key = { it.id }) { hito ->
                    TarjetaHitoItem(
                        hito = hito,
                        onCambiarEstado = { viewModel.cambiarEstadoHito(hito) },
                        onRecordarFecha = {
                            if (NotificacionHelper.tienePermisoNotificaciones(context)) {
                                val enviada = NotificacionHelper.mostrarNotificacionHito(
                                    context,
                                    hito.titulo,
                                    hito.fechaLimite
                                )
                                if (enviada) {
                                    Toast.makeText(
                                        context,
                                        "Notificación enviada para ${hito.titulo}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                hitoPendienteNotificacion = hito
                                permisoNotificacionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onEliminar = { hitoAEliminar = hito }
                    )
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
}

@Composable
fun TarjetaHitoItem(
    hito: HitoTesis,
    onCambiarEstado: () -> Unit,
    onRecordarFecha: () -> Unit,
    onEliminar: () -> Unit
) {
    var mostrarRubrica by remember { mutableStateOf(false) }

    val colorEstado by animateColorAsState(
        targetValue = when (hito.estado) {
            "Aprobado" -> Color(0xFF2E7D32)
            "Enviado" -> Color(0xFF1565C0)
            "En desarrollo" -> Color(0xFFEF6C00)
            else -> Color(0xFF616161)
        },
        label = "color_estado"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    color = VerdeEsucar
                )

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
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = hito.descripcion,
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = VerdeEsucar,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Límite: ${hito.fechaLimite}", fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = VerdeEsucar,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = hito.formatoRequerido, fontSize = 12.sp)
                }
            }

            if (hito.tieneEvidenciaFoto) {
                Spacer(modifier = Modifier.height(8.dp))
                val bitmapEvidencia = remember(hito.rutaFotoEvidencia) {
                    hito.rutaFotoEvidencia?.let { ruta ->
                        runCatching { BitmapFactory.decodeFile(ruta) }.getOrNull()
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (bitmapEvidencia != null) {
                        Image(
                            bitmap = bitmapEvidencia.asImageBitmap(),
                            contentDescription = "Foto de respaldo del hito",
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
                        text = "Respaldo fotográfico adjunto",
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
                    Text(if (mostrarRubrica) "Ocultar rúbrica" else "Ver pauta / rúbrica")
                }

                Row {
                    IconButton(onClick = onRecordarFecha) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "Activar alerta de entrega",
                            tint = VerdeEsucar
                        )
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar hito",
                            tint = Color.Gray
                        )
                    }
                }
            }

            AnimatedVisibility(visible = mostrarRubrica) {
                Surface(
                    color = Color(0xFFF1F8E9),
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
                            color = VerdeEsucar
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hito.criteriosRubrica,
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}