package com.guri.guriresumerewrite.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.guri.guriresumerewrite.RemoteConfigImpl
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.io.readBytes

data class PDFPickerState(
    val selectedPDFUri: Uri? = null,
    val selectedFileName: String? = null,
    val response: String? = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class PDFPickerEvent {
    data class PDFSelected(val uri: Uri, val fileName: String) : PDFPickerEvent()
    data class PDFRemoved(val uri: Uri) : PDFPickerEvent()
    data class AnalyzePDF(val uri: Uri) : PDFPickerEvent()
    data class Error(val message: String) : PDFPickerEvent()
}

class PDFPickerViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(PDFPickerState())
    val state: StateFlow<PDFPickerState> = _state.asStateFlow()

    private val remoteConfigImpl = RemoteConfigImpl(Gson())
    private val modelName = lazy {
        remoteConfigImpl.getModel()
    }
    private val prompt = lazy {
        remoteConfigImpl.getPrompt()
    }
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(modelName.value)

    fun onEvent(event: PDFPickerEvent) {
        when (event) {
            is PDFPickerEvent.PDFSelected -> {
                _state.update { currentState ->
                    currentState.copy(
                        selectedPDFUri = event.uri,
                        selectedFileName = event.fileName,
                        error = null
                    )
                }
            }
            is PDFPickerEvent.PDFRemoved -> {
                _state.update { currentState ->
                    currentState.copy(
                        selectedPDFUri = null,
                        selectedFileName = null,
                        response = null,
                        error = null
                    )
                }
            }
            is PDFPickerEvent.AnalyzePDF -> {
                analyzePDF(event.uri)
            }
            is PDFPickerEvent.Error -> {
                _state.update { currentState ->
                    currentState.copy(
                        error = event.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun analyzePDF(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                println("Gurdeep model value ${modelName.value}")
                println("Gurdeep prompt value ${prompt.value}")
                val contentResolver = getApplication<Application>().contentResolver
                val inputStream = contentResolver.openInputStream(uri)

                if (inputStream != null) {
                    inputStream.use { stream ->
                        val prompt = content {
                            inlineData(
                                bytes = stream.readBytes(),
                                mimeType = "application/pdf"
                            )
                            text(prompt.value)
                        }

                        model.generateContentStream(prompt).collect { chunk ->
                            // Log the generated text, handling the case where it might be null
                            val chunkText = chunk.text ?: ""
                            _state.update { currentState ->
                                currentState.copy(
                                    response = currentState.response + chunkText,
                                    isLoading = false
                                )
                            }
                        }
                    }
                } else {
                    onEvent(PDFPickerEvent.Error("Failed to read PDF file"))
                }
            } catch (e: Exception) {
                onEvent(PDFPickerEvent.Error(e.message ?: "An error occurred while analyzing the PDF"))
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PDFPickerViewModel::class.java)) {
                return PDFPickerViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}