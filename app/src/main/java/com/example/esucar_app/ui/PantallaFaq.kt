package com.example.esucar_app.ui

import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esucar_app.model.PerfilUsuario
import com.example.esucar_app.model.PreguntaFrecuente
import com.example.esucar_app.viewmodel.TutorViewModel

@Composable
fun PantallaFaq(
    viewModel: TutorViewModel,
    esPantallaAncha: Boolean = false
) {
    val preguntasFiltradas by viewModel.preguntasFiltradas.collectAsState()
    val textoBusqueda by viewModel.busquedaFaq.collectAsState()
    val categoriaActual by viewModel.categoriaSeleccionada.collectAsState()
    val formFaq by viewModel.formFaq.collectAsState()
    val perfilActual by viewModel.perfilActual.collectAsState()

    var mostrarModalNuevaFaq by remember { mutableStateOf(false) }
    var faqAEliminar by remember { mutableStateOf<PreguntaFrecuente?>(null) }
    val context = LocalContext.current

    val categoriasFiltro = listOf("Todas", "Metodología", "Citación APA", "Formato", "Entregas")
    val categoriasCreacion = listOf("Metodología", "Citación APA", "Formato", "Entregas")

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.limpiarFormularioFaq()
                    mostrarModalNuevaFaq = true
                },
                containerColor = VerdeEsucar,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AddComment, contentDescription = null) },
                text = { Text("Nueva Consulta") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Buscador Metodológico y Norma APA",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { viewModel.actualizarBusquedaFaq(it) },
                label = { Text("Buscar (ej: APA, margenes, objetivos, ley)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.actualizarBusquedaFaq("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoriasFiltro.forEach { cat ->
                    FilterChip(
                        selected = categoriaActual == cat,
                        onClick = { viewModel.seleccionarCategoriaFaq(cat) },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (preguntasFiltradas.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No se encontraron consultas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Prueba con otra palabra clave o registra una nueva orientación usando el botón inferior.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (esPantallaAncha) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(preguntasFiltradas, key = { it.id }) { item ->
                        TarjetaFaqExpandible(
                            faq = item,
                            puedeGestionar = perfilActual == PerfilUsuario.PROFESOR_GUIA,
                            onEditar = {
                                viewModel.cargarFaqParaEdicion(item)
                                mostrarModalNuevaFaq = true
                            },
                            onEliminar = { faqAEliminar = item }
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(preguntasFiltradas, key = { it.id }) { item ->
                        TarjetaFaqExpandible(
                            faq = item,
                            puedeGestionar = perfilActual == PerfilUsuario.PROFESOR_GUIA,
                            onEditar = {
                                viewModel.cargarFaqParaEdicion(item)
                                mostrarModalNuevaFaq = true
                            },
                            onEliminar = { faqAEliminar = item }
                        )
                    }
                }
            }
        }
    }

    if (mostrarModalNuevaFaq) {
        val cerrarModal = {
            viewModel.limpiarFormularioFaq()
            mostrarModalNuevaFaq = false
        }

        AlertDialog(
            onDismissRequest = cerrarModal,
            title = {
                Text(
                    text = if (formFaq.estaEditando) "Editar Consulta FAQ" else "Registrar Apunte o Consulta FAQ",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Categoría:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriasCreacion.forEach { cat ->
                            FilterChip(
                                selected = formFaq.categoria == cat,
                                onClick = { viewModel.onCategoriaFaqChange(cat) },
                                label = { Text(cat, fontSize = 12.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = formFaq.pregunta,
                        onValueChange = { viewModel.onPreguntaFaqChange(it) },
                        label = { Text("Pregunta o duda metodológica") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null) },
                        trailingIcon = {
                            IconoEstadoCampo(
                                tieneError = formFaq.errorPregunta != null,
                                esValido = formFaq.errorPregunta == null && formFaq.pregunta.trim().length >= 10
                            )
                        },
                        isError = formFaq.errorPregunta != null,
                        supportingText = {
                            formFaq.errorPregunta?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formFaq.respuesta,
                        onValueChange = { viewModel.onRespuestaFaqChange(it) },
                        label = { Text("Respuesta instructiva del profesor guía") },
                        leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                        trailingIcon = {
                            IconoEstadoCampo(
                                tieneError = formFaq.errorRespuesta != null,
                                esValido = formFaq.errorRespuesta == null && formFaq.respuesta.trim().length >= 15
                            )
                        },
                        isError = formFaq.errorRespuesta != null,
                        supportingText = {
                            formFaq.errorRespuesta?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val eraEdicion = formFaq.estaEditando
                        if (viewModel.guardarPreguntaFaq()) {
                            Toast.makeText(
                                context,
                                if (eraEdicion) "Consulta actualizada" else "Consulta agregada al banco local",
                                Toast.LENGTH_SHORT
                            ).show()
                            mostrarModalNuevaFaq = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeEsucar)
                ) {
                    Text(if (formFaq.estaEditando) "Actualizar" else "Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = cerrarModal) {
                    Text("Cancelar")
                }
            }
        )
    }

    faqAEliminar?.let { faq ->
        AlertDialog(
            onDismissRequest = { faqAEliminar = null },
            title = { Text("Eliminar consulta FAQ", fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que deseas eliminar esta pregunta frecuente del banco local?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarPreguntaFaq(faq)
                        faqAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { faqAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TarjetaFaqExpandible(
    faq: PreguntaFrecuente,
    puedeGestionar: Boolean = false,
    onEditar: () -> Unit = {},
    onEliminar: () -> Unit = {}
) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expandido = !expandido },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = { expandido = !expandido },
                    label = { Text(faq.categoria, fontSize = 12.sp) }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (puedeGestionar) {
                        IconButton(onClick = onEditar, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar consulta",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar consulta",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Icon(
                        imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandido) "Contraer respuesta" else "Expandir respuesta",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = faq.pregunta,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (expandido) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = faq.respuesta,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}