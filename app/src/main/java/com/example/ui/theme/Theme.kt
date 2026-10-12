package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioColorScheme = darkColorScheme(
  primary = NeonMint,
  onPrimary = Color.Black,
  primaryContainer = Color(0xFF00381B),
  onPrimaryContainer = NeonMint,
  secondary = ElectricCyan,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF00363D),
  onSecondaryContainer = ElectricCyan,
  tertiary = AmberGold,
  onTertiary = Color.Black,
  background = StudioDarkBg,
  onBackground = TextPrimary,
  surface = StudioCardBg,
  onSurface = TextPrimary,
  surfaceVariant = StudioToolbarBg,
  onSurfaceVariant = TextSecondary,
  outline = StudioBorder,
  error = PlayheadRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = StudioColorScheme,
    typography = Typography,
    content = content
  )
}
