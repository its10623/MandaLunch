package com.example.mandalunch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mandalunch.presentation.navigation.NavGraph
import com.example.mandalunch.presentation.ui.theme.BackgroundDark
import com.example.mandalunch.presentation.ui.theme.MandaLunchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MandaLunchTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark),
                    color = BackgroundDark
                ) {
                    NavGraph()
                }
            }
        }
    }
}
