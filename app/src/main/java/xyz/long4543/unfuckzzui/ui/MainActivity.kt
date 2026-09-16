package xyz.long4543.unfuckzzui.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import xyz.long4543.unfuckzzui.ui.screens.SettingsScreen
import xyz.long4543.unfuckzzui.ui.theme.UnfuckZuiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UnfuckZuiTheme {
                SettingsScreen()
            }
        }
    }
}
