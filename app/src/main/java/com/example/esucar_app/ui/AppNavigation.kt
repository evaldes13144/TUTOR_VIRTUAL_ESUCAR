package com.example.esucar_app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.esucar_app.viewmodel.TutorViewModel

val VerdeEsucar = Color(0xFF1B5E20)
val VerdeClaroEsucar = Color(0xFFE8F5E9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: TutorViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route ?: "hitos"

    val navegarATab: (String) -> Unit = { ruta ->
        navController.navigate(ruta) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tutor Virtual ESUCAR",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeEsucar
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = rutaActual == "hitos",
                    onClick = { navegarATab("hitos") },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = "Hitos") },
                    label = { Text("Mis Hitos") }
                )
                NavigationBarItem(
                    selected = rutaActual == "nuevo_hito",
                    onClick = { navegarATab("nuevo_hito") },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Registrar") },
                    label = { Text("Nuevo Hito") }
                )
                NavigationBarItem(
                    selected = rutaActual == "faq",
                    onClick = { navegarATab("faq") },
                    icon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "FAQ") },
                    label = { Text("Guía y FAQ") }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "hitos",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("hitos") {
                PantallaHitos(
                    viewModel = viewModel,
                    onIrANuevoHito = { navegarATab("nuevo_hito") }
                )
            }
            composable("nuevo_hito") {
                PantallaFormularioHito(
                    viewModel = viewModel,
                    onVolverAHitos = { navegarATab("hitos") }
                )
            }
            composable("faq") {
                PantallaFaq(viewModel = viewModel)
            }
        }
    }
}