package crepix.java_conf.gr.jp.uibase.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import crepix.java_conf.gr.jp.uibase.theme.wearColorPalette

@Composable
fun JustCompassTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = wearColorPalette,
        typography = Typography,
        // For shapes, we generally recommend using the default Material Wear shapes which are
        // optimized for round and non-round devices.
        content = content
    )
}