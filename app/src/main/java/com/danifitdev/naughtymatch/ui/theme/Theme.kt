package com.danifitdev.naughtymatch.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DeepRed,            // Usando Rojo oscuro
    secondary = Gold,             // Usando Dorado
    tertiary = White,          // Usando Gris oscuro
    background = SoftBlack,       // Fondo en Negro suave
    surface = IntenseBlack,       // Superficie en Negro intenso
    onPrimary = White,        // Texto en Blanco cálido sobre el primario
    onSecondary = White,      // Texto en Blanco cálido sobre el secundario
    onTertiary = WarmWhite,       // Texto en Blanco cálido sobre el terciario
    onBackground = WarmWhite,     // Texto en Blanco cálido sobre el fondo
    onSurface = PlatinumGold,         // Texto en Blanco cálido sobre la superficie
)

private val LightColorScheme = lightColorScheme(
    primary = Coral,              // Usando Coral
    secondary = GoldenYellow,     // Usando Amarillo dorado
    tertiary = IntenseBlack,          // Usando Rosa suave
    background = WarmWhite,       // Fondo en Blanco cálido
    surface = White,           // Superficie en Gris cálido
    onPrimary = IntenseBlack,     // Texto en Negro intenso sobre el primario
    onSecondary = SlateGray,   // Texto en Negro intenso sobre el secundario
    onTertiary = IntenseBlack,    // Texto en Negro intenso sobre el terciario
    onBackground = IntenseBlack,  // Texto en Negro intenso sobre el fondo
    onSurface = DeepRed       // Texto en Negro intenso sobre la superficie
)

@Composable
fun JuegoDeParejasAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}