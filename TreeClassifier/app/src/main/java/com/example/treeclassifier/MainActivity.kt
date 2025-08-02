package com.example.treeclassifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.treeclassifier.ui.theme.TreeClassifierTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TreeClassifierTheme {
                val context = LocalContext.current
                val viewModel: TreeClassifierViewModel = viewModel()

                val selectedImage = viewModel.selectedImage
                val prediction = viewModel.prediction

                // 📷 Camera launcher
                val cameraLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicture()
                ) { success ->
                    if (success) {
                        viewModel.capturedUri?.let { viewModel.predictFromUri(context, it) }
                    }
                }

                // 🖼️ Gallery launcher
                val galleryLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    uri?.let { viewModel.predictFromUri(context, it) }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Button(onClick = {
                            val uri = viewModel.createTempImageFile(context)
                            cameraLauncher.launch(uri)
                        }) {
                            Text("Capture Image")
                        }

                        Button(onClick = {
                            galleryLauncher.launch("image/*")
                        }) {
                            Text("Pick from Gallery")
                        }

                        selectedImage?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Tree Image",
                                modifier = Modifier
                                    .size(300.dp)
                                    .padding(8.dp)
                            )
                        }

                        Text(
                            text = prediction,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
