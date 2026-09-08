package net.onefivefour.crumbandcrust

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@JsModule("@js-joda/timezone")
external object JsJodaTimeZoneModule

private val jsJodaTz = JsJodaTimeZoneModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val repository = IndexedDbRecipeRepository()
    ComposeViewport {
        App(repository)
    }
}
