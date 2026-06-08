package com.neuro.photostudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neuro.photostudio.ui.AppViewModel
import com.neuro.photostudio.ui.navigation.NeuroApp
import com.neuro.photostudio.ui.theme.NeuroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppViewModel = viewModel(factory = AppViewModel.Factory)
            val state by vm.state.collectAsState()

            NeuroTheme(
                themeMode = state.settings.themeMode,
                accentArgb = state.settings.accentHex,
                dynamicColor = state.settings.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NeuroApp(viewModel = vm)
                }
            }
        }
    }
}
