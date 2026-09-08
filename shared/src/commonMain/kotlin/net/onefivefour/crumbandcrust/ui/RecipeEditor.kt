package net.onefivefour.crumbandcrust.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.onefivefour.crumbandcrust.recipe.*

@Composable
internal fun RecipeEditor(existingRecipe: Recipe? = null, saving: Boolean, saveError: String?, onCancel: () -> Unit, onSave: (Recipe) -> Unit) {
    var draft by remember(existingRecipe?.id) { mutableStateOf(existingRecipe?.toDraft() ?: RecipeDraft()) }
    var attempted by remember { mutableStateOf(false) }
    val errors = if (attempted) draft.validate() else emptyMap()
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(saveError) { if (saveError != null) scroll.animateScrollTo(0) }

    BoxWithConstraints(Modifier.fillMaxSize().background(BreadColors.background).safeDrawingPadding().imePadding()) {
        val compact = maxWidth < 600.dp
        val gutter = if (compact) 16.dp else 32.dp
        Column(Modifier.fillMaxSize()) {
            Header(compact)
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll).padding(horizontal = gutter),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Column(Modifier.widthIn(max = 820.dp).fillMaxWidth().padding(vertical = 28.dp)) {
                    Label("DEINE REZEPTSAMMLUNG")
                    Spacer(Modifier.height(8.dp))
                    Heading(if (existingRecipe == null) "Neues Rezept" else "Rezept bearbeiten")
                    Spacer(Modifier.height(8.dp))
                    Body(if (existingRecipe == null) "Halte fest, was dein Brot besonders macht. Felder mit * sind erforderlich."
                        else "Passe dein Rezept an. Änderungen werden erst mit dem Speichern übernommen.")
                    Spacer(Modifier.height(20.dp))
                    if (errors.isNotEmpty()) {
                        Notice("Noch nicht gespeichert. Bitte prüfe die markierten Felder.")
                        Spacer(Modifier.height(16.dp))
                    }
                    if (saveError != null) {
                        Notice(saveError)
                        Spacer(Modifier.height(16.dp))
                    }
                    EditorSection("01", "Das Rezept") {
                        RecipeField("Rezeptname *", draft.name, { draft = draft.copy(name = it) }, errors["name"], !saving,
                            placeholder = "Zum Beispiel: Mein Sauerteigbrot")
                        Spacer(Modifier.height(16.dp))
                        RecipeField("Kurzbeschreibung", draft.description, { draft = draft.copy(description = it) }, errors["description"], !saving,
                            multiline = true, placeholder = "Was zeichnet dein Brot aus? (optional)")
                    }
                    Spacer(Modifier.height(20.dp))
                    EditorSection("02", "Die Zutaten") {
                        Body("Alle Mengen beziehen sich auf einen Ansatz und werden in Gramm angegeben. Dezimalwerte wie 0,5 sind möglich.")
                        Spacer(Modifier.height(16.dp))
                        errors["ingredients"]?.let { FieldError(it) }
                        draft.ingredients.forEachIndexed { index, item ->
                            key(item.id) {
                                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    RecipeField("Zutat *", item.name,
                                        { value -> draft = draft.copy(ingredients = draft.ingredients.map { if (it.id == item.id) it.copy(name = value) else it }) },
                                        errors["${item.id}.name"], !saving, Modifier.weight(1f), placeholder = "Weizenmehl",
                                        accessibilityLabel = "Zutat ${index + 1}: Name *")
                                    RecipeField("Menge (g) *", item.grams,
                                        { value -> draft = draft.copy(ingredients = draft.ingredients.map { if (it.id == item.id) it.copy(grams = value) else it }) },
                                        errors["${item.id}.grams"], !saving, Modifier.width(96.dp), keyboardType = KeyboardType.Decimal, placeholder = "500",
                                        accessibilityLabel = "Zutat ${index + 1}: Menge in g *")
                                    BreadButton("×", { draft = draft.copy(ingredients = draft.ingredients.filterNot { it.id == item.id }) },
                                        Modifier.padding(top = 24.dp).width(54.dp).semantics { contentDescription = "Zutat ${index + 1} entfernen" },
                                        secondary = true, enabled = !saving)
                                }
                                Divider()
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                        BreadButton("Zutat hinzufügen", { draft = draft.copy(ingredients = draft.ingredients + IngredientDraft()) },
                            Modifier.fillMaxWidth(), secondary = true, showPlus = true, enabled = !saving && draft.ingredients.size < 100)
                    }
                    Spacer(Modifier.height(20.dp))
                    EditorSection("03", "Die Arbeitsschritte") {
                        Body("In der Reihenfolge, in der du backst. Die Dauer reicht vom Beginn eines Schritts bis zum nächsten – einschließlich Ruhezeit. Abkühlen kannst du als eigenen Schritt anlegen.")
                        Spacer(Modifier.height(16.dp))
                        errors["steps"]?.let { FieldError(it) }
                        draft.steps.forEachIndexed { index, step ->
                            key(step.id) {
                                Column(Modifier.fillMaxWidth().background(BreadColors.background, RoundedCornerShape(10.dp)).padding(16.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Label("SCHRITT ${index + 1}", Modifier.weight(1f))
                                        BreadButton("↑", { draft = draft.moveStep(step.id, -1) }, Modifier.width(54.dp)
                                            .semantics { contentDescription = "Schritt ${index + 1} nach oben" }, secondary = true, enabled = !saving && index > 0)
                                        BreadButton("↓", { draft = draft.moveStep(step.id, 1) }, Modifier.width(54.dp)
                                            .semantics { contentDescription = "Schritt ${index + 1} nach unten" }, secondary = true, enabled = !saving && index < draft.steps.lastIndex)
                                        BreadButton("×", { draft = draft.copy(steps = draft.steps.filterNot { it.id == step.id }) },
                                            Modifier.width(54.dp).semantics { contentDescription = "Schritt ${index + 1} entfernen" }, secondary = true, enabled = !saving)
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    fun update(transform: (StepDraft) -> StepDraft) { draft = draft.copy(steps = draft.steps.map { if (it.id == step.id) transform(it) else it }) }
                                    RecipeField("Titel *", step.title, { value -> update { it.copy(title = value) } },
                                        errors["${step.id}.title"], !saving, placeholder = "Zum Beispiel: Teig mischen", accessibilityLabel = "Schritt ${index + 1}: Titel *")
                                    Spacer(Modifier.height(12.dp))
                                    RecipeField("Anleitung", step.instruction, { value -> update { it.copy(instruction = value) } },
                                        errors["${step.id}.instruction"], !saving, multiline = true, placeholder = "Was ist zu tun? (optional)", accessibilityLabel = "Schritt ${index + 1}: Anleitung")
                                    Spacer(Modifier.height(12.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        RecipeField("Stunden *", step.hours, { value -> update { it.copy(hours = value) } }, errors["${step.id}.hours"], !saving,
                                            Modifier.weight(1f), keyboardType = KeyboardType.Number, accessibilityLabel = "Schritt ${index + 1}: Stunden")
                                        RecipeField("Minuten *", step.minutes, { value -> update { it.copy(minutes = value) } }, errors["${step.id}.minutes"], !saving,
                                            Modifier.weight(1f), keyboardType = KeyboardType.Number, accessibilityLabel = "Schritt ${index + 1}: Minuten")
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                        BreadButton("Schritt hinzufügen", { draft = draft.copy(steps = draft.steps + StepDraft()) },
                            Modifier.fillMaxWidth(), secondary = true, showPlus = true, enabled = !saving && draft.steps.size < 100)
                        Spacer(Modifier.height(12.dp))
                        Body("0 Stunden und 0 Minuten sind für einen abschließenden Meilenstein möglich.", small = true)
                    }
                    Spacer(Modifier.height(20.dp))
                    Body("Deine Rezepte werden nur in diesem Browser gespeichert. Wenn du dessen Websitedaten löschst, werden auch die Rezepte entfernt.", small = true)
                }
            }
            Divider()
            Box(Modifier.fillMaxWidth().background(BreadColors.paper).padding(horizontal = gutter, vertical = 12.dp), contentAlignment = Alignment.Center) {
                Row(Modifier.widthIn(max = 820.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BreadButton("Abbrechen", onCancel, Modifier.weight(1f), secondary = true, enabled = !saving)
                    BreadButton(if (saving) "Speichert …" else if (existingRecipe == null) "Speichern" else "Änderungen speichern", {
                        attempted = true
                        if (draft.validate().isEmpty()) onSave(draft.toRecipe())
                        else scope.launch { scroll.animateScrollTo(0) }
                    }, Modifier.weight(1f), enabled = !saving)
                }
            }
        }
    }
}

@Composable
private fun EditorSection(number: String, title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().panel().padding(20.dp)) {
        Label(number)
        Spacer(Modifier.height(6.dp))
        Heading(title)
        Spacer(Modifier.height(20.dp))
        content()
    }
}

@Composable
internal fun RecipeField(
    label: String, value: String, onValueChange: (String) -> Unit, errorMessage: String?, enabled: Boolean,
    modifier: Modifier = Modifier, multiline: Boolean = false, keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = "", accessibilityLabel: String = label, showLabel: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Column(modifier.fillMaxWidth()) {
        if (showLabel) {
            Body(label)
            Spacer(Modifier.height(6.dp))
        }
        BasicTextField(
            value = value, onValueChange = onValueChange, enabled = enabled, singleLine = !multiline,
            interactionSource = interaction,
            textStyle = LocalBreadTypography.current.body.copy(color = BreadColors.ink),
            cursorBrush = SolidColor(BreadColors.accent),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = if (multiline) ImeAction.Default else ImeAction.Next),
            modifier = Modifier.fillMaxWidth().heightIn(min = if (multiline) 92.dp else 48.dp)
                .background(BreadColors.paper, RoundedCornerShape(8.dp))
                .border(if (focused) 2.dp else 1.dp, if (errorMessage != null) Color(0xFFBA1A1A) else if (focused) BreadColors.accent else BreadColors.line, RoundedCornerShape(8.dp))
                .padding(12.dp).semantics {
                    contentDescription = accessibilityLabel
                    if (errorMessage != null) error(errorMessage)
                },
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty() && placeholder.isNotEmpty()) Body(placeholder)
                    inner()
                }
            },
        )
        if (errorMessage != null) {
            Spacer(Modifier.height(5.dp))
            FieldError(errorMessage)
        }
    }
}

@Composable
private fun FieldError(message: String) {
    BasicText(message, style = LocalBreadTypography.current.body.copy(color = Color(0xFFBA1A1A)))
}
