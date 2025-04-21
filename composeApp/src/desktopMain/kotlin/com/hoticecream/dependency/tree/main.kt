package com.hoticecream.dependency.tree

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DependencyTree",
    ) {
        App()
    }
}