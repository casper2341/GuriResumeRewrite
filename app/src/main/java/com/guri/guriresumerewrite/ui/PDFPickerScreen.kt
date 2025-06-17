package com.guri.guriresumerewrite.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.guri.guriresumerewrite.ui.viewmodel.PDFPickerEvent
import com.guri.guriresumerewrite.ui.viewmodel.PDFPickerViewModel

@Composable
fun PDFPickerScreen(
    modifier: Modifier = Modifier,
    viewModel: PDFPickerViewModel
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (isPDFFile(context, it)) {
                val fileName = getFileName(context, it)
                viewModel.onEvent(PDFPickerEvent.PDFSelected(it, fileName ?: "Unknown file"))
                Toast.makeText(context, "PDF file selected successfully", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.onEvent(PDFPickerEvent.Error("Please select a PDF file"))
                Toast.makeText(context, "Please select a PDF file", Toast.LENGTH_LONG).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            pdfPickerLauncher.launch("application/pdf")
        } else {
            viewModel.onEvent(PDFPickerEvent.Error("Storage permission is required to pick PDF files"))
            Toast.makeText(
                context,
                "Storage permission is required to pick PDF files",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (state.selectedPDFUri != null) {
            Button(
                onClick = { viewModel.onEvent(PDFPickerEvent.AnalyzePDF(state.selectedPDFUri!!)) },
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text("Resume Advice")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                when {
                    hasStoragePermission(context) -> {
                        pdfPickerLauncher.launch("application/pdf")
                    }
                    else -> {
                        requestStoragePermission(permissionLauncher)
                    }
                }
            }
        ) {
            Text("Select PDF File")
        }

        Spacer(modifier = Modifier.height(16.dp))

        state.error?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (state.response?.isNotBlank() == true) {
            Text(
                "Response ${state.response}",
                modifier = Modifier
                    .padding(16.dp)
                    .border(
                        width = 4.dp,
                        shape = RoundedCornerShape(10),
                        color = Color.Red
                    )
                    .padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.selectedPDFUri != null && state.selectedFileName != null) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.selectedFileName ?: "Unknown file",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            viewModel.onEvent(PDFPickerEvent.PDFRemoved(state.selectedPDFUri!!))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove PDF"
                        )
                    }
                }
            }
        }
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    var fileName: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = cursor.getString(nameIndex)
            }
        }
    }
    return fileName
}

private fun hasStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        true // No permission needed for Android 13 and above when using GetContent
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}

private fun requestStoragePermission(permissionLauncher: ActivityResultLauncher<Array<String>>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        )
    } else {
        permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
    }
}

private fun isPDFFile(context: Context, uri: Uri): Boolean {
    val mimeType = context.contentResolver.getType(uri)
    return mimeType == "application/pdf"
}