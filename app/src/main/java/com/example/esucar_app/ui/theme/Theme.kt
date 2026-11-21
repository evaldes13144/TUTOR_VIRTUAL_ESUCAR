package com.example.esucar_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VerdeEsucarDark,
    onPrimary = Color(0xFF06290A),
    primaryContainer = VerdeContenedorDark,
    onPrimaryContainer = VerdeClaroEsucar,
    secondary = Color(0xFFA5D6A7),
    onSecondary = Color(0xFF0C2A10),
    background = FondoOscuroEsucar,
    onBackground = Color(0xFFECEFED),
    surface = SuperficieOscuraEsucar,
    onSurface = Color(0xFFECEFED),
    surfaceVariant = Color(0xFF252E28),
    onSurfaceVariant = TextoSecundarioOscuro
)

private val LightColorScheme = lightColorScheme(
    primary = VerdeEsucar,
    onPrimary = Color.White,
    primaryContainer = VerdeClaroEsucar,
    onPrimaryContainer = VerdeEsucar,
    secondary = VerdeEsucarMedio,
    onSecondary = Color.White,
    background = FondoClaroEsucar,
    onBackground = Color(0xFF1A1F1C),
    surface = Color.White,
    onSurface = Color(0xFF1A1F1C),
    surfaceVariant = VerdeSuaveEsucar,
    onSurfaceVariant = TextoSecundarioClaro
)

@Composable
fun ESUCAR_APPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}