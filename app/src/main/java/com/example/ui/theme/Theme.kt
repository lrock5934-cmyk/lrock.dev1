package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = TealAccent,
    secondary = TealDark,
    background = DeepNavy,
    surface = SlateNavy,
    onPrimary = DeepNavy,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DeepNavy,
    secondary = TealAccent,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = DeepNavy,
    onBackground = Color.Black,
    onSurface = Color.Black,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Disabled dynamic color to preserve exact LRock.dev brand aesthetic
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
