package net.onefivefour.crumbandcrust

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val repository = IndexedDbRecipeRepository()
    ComposeViewport {
        App(repository)
    }
}
