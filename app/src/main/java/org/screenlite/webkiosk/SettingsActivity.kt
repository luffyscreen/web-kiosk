package org.screenlite.webkiosk

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import org.screenlite.webkiosk.components.SettingsScreen
import org.screenlite.webkiosk.ui.theme.DarkBackgroundColor
import org.screenlite.webkiosk.ui.theme.TvSettingsTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawable(DarkBackgroundColor.toArgb().toDrawable())
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false

        setContent {
            TvSettingsTheme {
                SettingsScreen()
            }
        }
    }
}

@Preview(
    name = "UHD TV Preview",
    device = "spec:width=3840px,height=2160px,dpi=320",
    uiMode = Configuration.UI_MODE_TYPE_TELEVISION
)
@Composable
fun SettingsScreenUhdTvPreview() {
    TvSettingsTheme { SettingsScreen() }
}

@Preview(name = "Settings Screen TV Preview", device = "id:tv_1080p")
@Composable
fun SettingsScreenTvPreview() {
    TvSettingsTheme { SettingsScreen() }
}

@Preview(name = "Settings Screen Phone Preview", device = "id:pixel_4")
@Composable
fun SettingsScreenPhonePreview() {
    TvSettingsTheme { SettingsScreen() }
}