package com.guri.guriresumerewrite

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.guri.guriresumerewrite.ui.PDFPickerScreen
import com.guri.guriresumerewrite.ui.theme.GuriResumeRewriteTheme
import com.guri.guriresumerewrite.ui.viewmodel.PDFPickerViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PDFPickerViewModel by viewModels {
        PDFPickerViewModel.Factory(application)
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GuriResumeRewriteTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    PDFPickerScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}