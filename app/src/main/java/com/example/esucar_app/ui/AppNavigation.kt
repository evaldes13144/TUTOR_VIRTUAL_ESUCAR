package com.example.esucar_app.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.esucar_app.model.PerfilUsuario
import com.example.esucar_app.ui.theme.VerdeClaroEsucar as ThemeVerdeClaro
import com.example.esucar_app.ui.theme.VerdeEsucar as ThemeVerdeEsucar
import com.example.esucar_app.viewmodel.TutorViewModel

val VerdeEsucar = ThemeVerdeEsucar
val VerdeClaroEsucar = ThemeVerdeClaro

private data class DestinoNav(
    val ruta: String,
    val etiqueta: String,
    val descripcionIcono: String,
    val icono: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: TutorViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route ?: "hitos"
    val perfilActual by viewModel.perfilActual.collectAsState()
    var mostrarSelectorPerfil by remember { mutableStateOf(false) }

    val destinos = listOf(
        DestinoNav("hitos", "Mis Hitos", "Hitos", Icons.Default.Checklist),
        DestinoNav("guia", "Guía", "Guía Metodológica", Icons.AutoMirrored.Filled.MenuBook),
        DestinoNav("nuevo_hito", "Nuevo Hito", "Registrar Hito", Icons.Default.AddCircle),
        DestinoNav("faq", "FAQ", "Preguntas Frecuentes", Icons.AutoMirrored.Filled.HelpOutline)
    )

    val navegarATab: (String) -> Unit = { ruta ->
        navController.navigate(ruta) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val esPantallaAncha = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Tutor Virtual ESUCAR",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Trabajo de Título I, II y III",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    },
                    actions = {
                        Surface(
                            onClick = { mostrarSelectorPerfil = true },
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (perfilActual == PerfilUsuario.PROFESOR_GUIA) {
                                        Icons.Default.School
                                    } else {
                                        Icons.Default.Person
                                    },
                                    contentDescription = "Cambiar perfil de usuario",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = perfilActual.nombreVisible,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = VerdeEsucar
                    )
                )
            },
            bottomBar = {
                if (!esPantallaAncha) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        destinos.forEach { destino ->
                            NavigationBarItem(
                                selected = rutaActual == destino.ruta,
                                onClick = { navegarATab(destino.ruta) },
                                icon = {
                                    Icon(
                                        imageVector = destino.icono,
                                        contentDescription = destino.descripcionIcono
                                    )
                                },
                                label = { Text(destino.etiqueta, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (esPantallaAncha) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        Spacer(modifier = Modifier.height(12.dp))
                        destinos.forEach { destino ->
                            NavigationRailItem(
                                selected = rutaActual == destino.ruta,
                                onClick = { navegarATab(destino.ruta) },
                                icon = {
                                    Icon(
                                        imageVector = destino.icono,
                                        contentDescription = destino.descripcionIcono
                                    )
                                },
                                label = { Text(destino.etiqueta, fontSize = 12.sp) }
                            )
                        }
                    }
                    VerticalDivider()
                }

                NavHost(
                    navController = navController,
                    startDestination = "hitos",
                    enterTransition = { fadeIn(animationSpec = tween(220)) },
                    exitTransition = { fadeOut(animationSpec = tween(180)) },
                    modifier = Modifier.weight(1f)
                ) {
                    composable("hitos") {
                        PantallaHitos(
                            viewModel = viewModel,
                            esPantallaAncha = esPantallaAncha,
                            onIrANuevoHito = {
                                viewModel.cancelarEdicionHito()
                                navegarATab("nuevo_hito")
                            },
                            onEditarHito = { hito ->
                                viewModel.cargarHitoParaEdicion(hito)
                                navegarATab("nuevo_hito")
                            }
                        )
                    }
                    composable("guia") {
                        PantallaGuia(
                            viewModel = viewModel,
                            esPantallaAncha = esPantallaAncha
                        )
                    }
                    composable("nuevo_hito") {
                        PantallaFormularioHito(
                            viewModel = viewModel,
                            onVolverAHitos = { navegarATab("hitos") }
                        )
                    }
                    composable("faq") {
                        PantallaFaq(
                            viewModel = viewModel,
                            esPantallaAncha = esPantallaAncha
                        )
                    }
                }
            }
        }
    }

    if (mostrarSelectorPerfil) {
        AlertDialog(
            onDismissRequest = { mostrarSelectorPerfil = false },
            title = { Text("Seleccionar Perfil de Uso", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Elige el rol con el que utilizarás el Tutor Virtual ESUCAR:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PerfilUsuario.entries.forEach { perfil ->
                        val seleccionado = perfil == perfilActual
                        Surface(
                            onClick = {
                                viewModel.cambiarPerfil(perfil)
                                mostrarSelectorPerfil = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (seleccionado) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = seleccionado,
                                    onClick = {
                                        viewModel.cambiarPerfil(perfil)
                                        mostrarSelectorPerfil = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = perfil.nombreVisible,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = perfil.descripcion,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarSelectorPerfil = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}