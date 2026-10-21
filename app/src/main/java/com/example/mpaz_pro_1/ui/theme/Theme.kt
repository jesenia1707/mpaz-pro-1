package com.example.mpaz_pro_1.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Azul,
    onPrimary = Blanco,
    primaryContainer = AzulSuave,
    onPrimaryContainer = Tinta,
    secondary = Rosado,
    onSecondary = Blanco,
    tertiary = Verde,
    onTertiary = Tinta,
    background = Fondo,
    onBackground = Tinta,
    surface = Blanco,
    onSurface = Tinta,
    surfaceVariant = AzulSuave,
    onSurfaceVariant = TintaSuave,
    outline = TintaSuave
)

private val DarkColorScheme = darkColorScheme(
    primary = AzulSuave,
    onPrimary = Tinta,
    primaryContainer = AzulProfundo,
    onPrimaryContainer = Blanco,
    secondary = Rosado,
    onSecondary = Tinta,
    tertiary = Verde,
    onTertiary = Tinta,
    background = FondoOscuro,
    onBackground = Blanco,
    surface = SuperficieOscura,
    onSurface = Blanco,
    surfaceVariant = AzulProfundo,
    onSurfaceVariant = Blanco
)

@Composable
fun Mpazpro1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // La app tiene identidad de color propia (paleta MPAZ), por eso no usamos
    // dynamicColor: el diseño se mantiene igual en cualquier dispositivo.
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
