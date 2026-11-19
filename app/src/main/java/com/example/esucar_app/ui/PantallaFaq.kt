package com.example.esucar_app.ui

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.esucar_app.model.PreguntaFrecuente
import com.example.esucar_app.viewmodel.TutorViewModel

@Composable
fun PantallaFaq(viewModel: TutorViewModel) {
    val preguntasFiltradas by viewModel.preguntasFiltradas.collectAsState()
    val textoBusqueda by viewModel.busquedaFaq.collectAsState()
    val categoriaActual by viewModel.categoriaSeleccionada.collectAsState()
    val formFaq by viewModel.formFaq.collectAsState()

    var mostrarModalNuevaFaq by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val categoriasFiltro = listOf("Todas", "Metodología", "Citación APA", "Formato", "Entregas")
    val categoriasCreacion = listOf("Metodología", "Citación APA", "Formato", "Entregas")

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { mostrarModalNuevaFaq = true },
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
                .background(Color(0xFFF5F7F6))
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Buscador Metodológico y Norma APA",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = VerdeEsucar
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { viewModel.actualizarBusquedaFaq(it) },
                label = { Text("Buscar por palabra clave (ej: APA, márgenes, objetivos)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.actualizarBusquedaFaq("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
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

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(preguntasFiltradas, key = { it.id }) { item ->
                    TarjetaFaqExpandible(item)
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
            title = { Text("Registrar Apunte o Consulta FAQ", fontWeight = FontWeight.Bold) },
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
                            if (formFaq.errorPregunta != null) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
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
                            if (formFaq.errorRespuesta != null) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
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
                        if (viewModel.guardarPreguntaFaq()) {
                            Toast.makeText(context, "Consulta agregada al banco local", Toast.LENGTH_SHORT).show()
                            mostrarModalNuevaFaq = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeEsucar)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = cerrarModal) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TarjetaFaqExpandible(faq: PreguntaFrecuente) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expandido = !expandido },
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    label = { Text(faq.categoria, fontSize = 11.sp) }
                )
                Icon(
                    imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = VerdeEsucar
                )
            }

            Text(
                text = faq.pregunta,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (expandido) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = faq.respuesta,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}