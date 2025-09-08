package org.screenlite.webkiosk.ui.theme

import android.content.res.Configuration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DarkBackgroundColor = Color(0xFF121212)
val SurfaceColor = Color(0xFF1E1E1E)
val PrimaryColor = Color(0xFF6200EE)
val TextColorPrimary = Color.White
val TextColorSecondary = Color.Gray
val ErrorColor = Color(0xFFCF6679)

private val tvDarkColorScheme = darkColorScheme(
    background = DarkBackgroundColor,
    surface = SurfaceColor,
    primary = PrimaryColor,
    onPrimary = Color.White,
    onBackground = TextColorPrimary,
    onSurface = TextColorPrimary,
    error = ErrorColor
)

private val tvTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        color = TextColorPrimary
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = TextColorPrimary
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        color = TextColorSecondary
    ),
    labelMedium = TextStyle(
        fontSize = 14.sp,
        color = TextColorSecondary
    )
)

@Composable
fun TvSettingsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = tvDarkColorScheme,
        typography = tvTypography,
        content = content
    )
}

fun isTvDevice(): Boolean {
    val uiMode = android.content.res.Resources.getSystem().configuration.uiMode
    return (uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION
}
