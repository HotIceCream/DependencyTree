package com.hoticecream.dependency.tree

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File

@Composable
@Preview
fun App() {
    MaterialTheme {
        var selectedFile by remember { mutableStateOf<File?>(null) }
        var tree by remember { mutableStateOf<List<DependencyNode>?>(null) }
        var error by remember { mutableStateOf<String?>(null) }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            DesktopFilePicker { files ->
                val file = files.firstOrNull() ?: return@DesktopFilePicker
                selectedFile = file
                runCatching {
                    parseDependencyTree(readFileContent(file.absolutePath))
                }.onSuccess {
                    tree = it
                    error = null
                }.onFailure {
                    error = it.message
                }
            }
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                selectedFile?.let { file ->
                    Text("Selected File: $file")
                    error?.let { Text("Error: $error") }

                    tree?.let { ComprehensiveSearchableTree(it) }
                }
            }
        }
    }
}

private fun readFileContent(filePath: String): String {
    val file = File(filePath)
    return file.readText()
}
