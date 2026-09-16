package xyz.long4543.unfuckzui.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import xyz.long4543.unfuckzui.ui.screens.SettingsScreen
import xyz.long4543.unfuckzui.ui.theme.UnfuckZuiTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UnfuckZuiTheme {
                SettingsScreen()
            }
        }
    }
}
