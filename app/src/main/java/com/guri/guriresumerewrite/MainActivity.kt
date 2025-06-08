package com.guri.guriresumerewrite

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.guri.guriresumerewrite.ui.theme.GuriResumeRewriteTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
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
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun PDFPickerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedPDFUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.0-flash")
    val cs = rememberCoroutineScope()
    var response: GenerateContentResponse? by remember { mutableStateOf(null) }
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (isPDFFile(context, it)) {
                selectedPDFUri = it
                selectedFileName = getFileName(context, it)
                Toast.makeText(context, "PDF file selected successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please select a PDF file", Toast.LENGTH_LONG).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Permissions granted, proceed with PDF picker
            pdfPickerLauncher.launch("application/pdf")
        } else {
            Toast.makeText(
                context,
                "Storage permission is required to pick PDF files",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (selectedPDFUri != null) {
            Button(
                onClick = {
                    cs.launch {
                        val contentResolver = context.contentResolver

                        // Provide the URI for the PDF file you want to send to the model
                        val inputStream = contentResolver.openInputStream(selectedPDFUri!!)

                        if (inputStream != null) {  // Check if the PDF file loaded successfully
                            inputStream.use { stream ->
                                // Provide a prompt that includes the PDF file specified above and text
                                val prompt = content {
                                    inlineData(
                                        bytes = stream.readBytes(),
                                        mimeType = "application/pdf" // Specify the appropriate PDF file MIME type
                                    )
                                    text("Scan this pdf file and give advice what all to improve in this resume so that i can get better job opportunities and if you scan this file and feel this is not a resume just say Oops sorry looks like it is not a resume so i can`t give you resume advice")
                                }

                                // To generate text output, call `generateContent` with the prompt
                                response = model.generateContent(prompt)

                                // Log the generated text, handling the case where it might be null
                                println("Gurdeep response $response resp text ${response!!.text} resp feedbaack ${response!!.promptFeedback}")
                            }
                        } else {
                            println("Gurdeep error")
                            // Handle the error appropriately
                        }
                    }
                },
            ) {
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

        if (response != null) {
            Text(
                "Response ${response?.text}",
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

        if (selectedPDFUri != null && selectedFileName != null) {
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
                        text = selectedFileName ?: "Unknown file",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            selectedPDFUri = null
                            selectedFileName = null
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