package com.hoticecream.dependency.tree

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter

@Composable
fun DesktopFilePicker(
    title: String = "Choose a file",
    allowedExtensions: List<String> = emptyList(),
    allowMultiSelection: Boolean = false,
    onFileSelected: (List<File>) -> Unit,
) {
    var isFileDialogOpen by remember { mutableStateOf(false) }

    Button(
        onClick = { isFileDialogOpen = true }
    ) {
        Text("Select File")
    }

    if (isFileDialogOpen) {
        FileDialog(
            title = title,
            allowedExtensions = allowedExtensions,
            allowMultiSelection = allowMultiSelection,
            onFileSelected = { files ->
                onFileSelected(files)
                isFileDialogOpen = false
            },
            onDialogCancelled = {
                isFileDialogOpen = false
            }
        )
    }
}

@Composable
private fun FileDialog(
    title: String,
    allowedExtensions: List<String>,
    allowMultiSelection: Boolean,
    onFileSelected: (List<File>) -> Unit,
    onDialogCancelled: () -> Unit,
) {
    LaunchedEffect(Unit) {
        val fileDialog = FileDialog(Frame(), title).apply {
            isMultipleMode = allowMultiSelection

            // Set file filter if extensions are provided
            if (allowedExtensions.isNotEmpty()) {
                filenameFilter = FilenameFilter { _, name ->
                    allowedExtensions.any { ext ->
                        name.endsWith(ext, ignoreCase = true)
                    }
                }
            }
        }

        fileDialog.isVisible = true

        if (fileDialog.files.isNotEmpty()) {
            onFileSelected(fileDialog.files.toList())
        } else {
            onDialogCancelled()
        }
    }
}

// Advanced version with more features
@Composable
fun AdvancedDesktopFilePicker(
    modifier: Modifier = Modifier,
    title: String = "Choose a file",
    allowedExtensions: List<String> = emptyList(),
    allowMultiSelection: Boolean = false,
    buttonText: String = "Select File",
    showSelectedFiles: Boolean = true,
    onFileSelected: (List<File>) -> Unit,
) {
    var isFileDialogOpen by remember { mutableStateOf(false) }
    var selectedFiles by remember { mutableStateOf<List<File>>(emptyList()) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { isFileDialogOpen = true }
        ) {
            Text(buttonText)
        }

        if (showSelectedFiles && selectedFiles.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Selected Files:", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedFiles.forEach { file ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = file.name,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    selectedFiles = selectedFiles - file
                                    onFileSelected(selectedFiles)
                                }
                            ) {
                                Text("×")
                            }
                        }
                    }
                }
            }
        }
    }

    if (isFileDialogOpen) {
        FileDialog(
            title = title,
            allowedExtensions = allowedExtensions,
            allowMultiSelection = allowMultiSelection,
            onFileSelected = { files ->
                selectedFiles = files
                onFileSelected(files)
                isFileDialogOpen = false
            },
            onDialogCancelled = {
                isFileDialogOpen = false
            }
        )
    }
}

// Usage example with different configurations
@Composable
fun FilePickerExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Simple file picker
        DesktopFilePicker { files ->
            println("Selected files: ${files.map { it.name }}")
        }

        // File picker for specific file types
        DesktopFilePicker(
            title = "Select Images",
            allowedExtensions = listOf(".jpg", ".png", ".gif")
        ) { files ->
            println("Selected images: ${files.map { it.name }}")
        }

        // Multiple file selection
        DesktopFilePicker(
            title = "Select Multiple Files",
            allowMultiSelection = true
        ) { files ->
            println("Selected multiple files: ${files.map { it.name }}")
        }

        // Advanced file picker with UI feedback
        AdvancedDesktopFilePicker(
            title = "Select Documents",
            allowedExtensions = listOf(".pdf", ".doc", ".docx"),
            allowMultiSelection = true,
            buttonText = "Choose Documents",
            showSelectedFiles = true
        ) { files ->
            println("Selected documents: ${files.map { it.name }}")
        }
    }
}

// Directory Picker
@Composable
fun DirectoryPicker(
    title: String = "Choose a directory",
    onDirectorySelected: (File) -> Unit,
) {
    var isDialogOpen by remember { mutableStateOf(false) }

    Button(
        onClick = { isDialogOpen = true }
    ) {
        Text("Select Directory")
    }

    if (isDialogOpen) {
        LaunchedEffect(Unit) {
            val fileDialog = FileDialog(Frame(), title).apply {
                mode = FileDialog.LOAD
                isMultipleMode = false
                // Set to directory selection mode
                setFilenameFilter { _, _ -> true }
            }

            System.setProperty("apple.awt.fileDialogForDirectories", "true") // For macOS
            fileDialog.isVisible = true

            if (fileDialog.directory != null) {
                onDirectorySelected(File(fileDialog.directory))
            }
            isDialogOpen = false
            System.setProperty("apple.awt.fileDialogForDirectories", "false")
        }
    }
}
