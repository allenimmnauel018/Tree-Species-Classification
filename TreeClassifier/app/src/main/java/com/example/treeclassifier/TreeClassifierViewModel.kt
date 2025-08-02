package com.example.treeclassifier

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.metadata.MetadataExtractor
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import androidx.core.graphics.get
import androidx.core.graphics.scale

class TreeClassifierViewModel : ViewModel() {

    var selectedImage by mutableStateOf<Bitmap?>(null)
        private set

    var prediction by mutableStateOf("No prediction yet")
        private set

    var capturedUri: Uri? = null
        private set

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    private val imageSize = 224
    private val numChannels = 3
    private val confidenceThreshold = 0.6f

    fun createTempImageFile(context: Context): Uri {
        val tempFile = File.createTempFile(
            "tree_image_", ".jpg",
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        capturedUri = uri
        return uri
    }



    fun predictFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = loadBitmapFromUri(context, uri)
                selectedImage = bitmap

                ensureModelLoaded(context)

                val input = preprocess(bitmap)
                val outputTensor = interpreter?.getOutputTensor(0)
                    ?: throw IllegalStateException("Output tensor not available")

                val numClasses = outputTensor.shape()[1]
                val output = Array(1) { FloatArray(numClasses) }

                interpreter?.run(input, output)

                val probs = output[0]
                val maxIndex = probs.indices.maxByOrNull { probs[it] } ?: -1
                val confidence = probs.getOrNull(maxIndex) ?: 0f

                if (confidence >= confidenceThreshold) {
                    val label = labels.getOrNull(maxIndex) ?: "Class $maxIndex"
                    val formatted = String.format("%.1f", confidence * 100)
                    prediction = "$label ($formatted%)"
                } else {
                    prediction = "Unsure (Confidence ${String.format("%.1f", confidence * 100)}%)"
                }

            } catch (e: Exception) {
                prediction = "Error: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    private fun ensureModelLoaded(context: Context) {
        if (interpreter == null) {
            val modelBuffer = FileUtil.loadMappedFile(context, "model_with_metadata.tflite")
            interpreter = Interpreter(modelBuffer)

            val metadataExtractor = MetadataExtractor(modelBuffer)
            labels = metadataExtractor.getAssociatedFile("labels.txt")
                ?.let { FileUtil.loadLabels(context, "labels.txt")
                }
                ?: listOf("Unknown")
        }
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
        val original = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                .copy(Bitmap.Config.ARGB_8888, true)
        }
        return original
    }

    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val resized = bitmap.scale(imageSize, imageSize)
        val buffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * numChannels)
        buffer.order(ByteOrder.nativeOrder())

        for (y in 0 until imageSize) {
            for (x in 0 until imageSize) {
                val pixel = resized[x, y]
                buffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
                buffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
                buffer.putFloat((pixel and 0xFF) / 255f)
            }
        }

        buffer.rewind()
        return buffer
    }
}
