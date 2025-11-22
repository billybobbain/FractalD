package com.billybobbain.fractald

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.billybobbain.fractald.ui.FractalDApp
import com.billybobbain.fractald.ui.theme.FractalDTheme
import com.billybobbain.fractald.viewmodel.FractalViewModel
import com.billybobbain.fractald.viewmodel.FractalViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: FractalViewModel by viewModels {
        FractalViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FractalDTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FractalDApp(viewModel = viewModel)
                }
            }
        }
    }
}
