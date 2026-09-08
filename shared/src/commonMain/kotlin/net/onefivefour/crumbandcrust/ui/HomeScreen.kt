package net.onefivefour.crumbandcrust.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun HomeScreen() {
    var showEntryNotice by remember { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxSize().background(BreadColors.background).safeDrawingPadding()) {
        val twoColumns = maxWidth >= 840.dp
        val compact = maxWidth < 600.dp
        val gutter = if (compact) 20.dp else 32.dp
        Column(Modifier.fillMaxSize()) {
            Header(compact)
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = gutter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(Modifier.widthIn(max = 1024.dp).fillMaxWidth().padding(top = if (compact) 28.dp else 42.dp, bottom = 24.dp)) {
                    Label("DEINE BACKSTUBE")
                    Spacer(Modifier.height(10.dp))
                    BasicText("Gutes Brot braucht Zeit.", Modifier.semantics { heading() },
                        style = LocalBreadTypography.current.heading.copy(fontSize = if (compact) 32.sp else 40.sp, lineHeight = 46.sp))
                    Spacer(Modifier.height(6.dp))
                    Body("Deine Rezepte und jeder Schritt bis zum fertigen Brot – an einem Ort.")
                    Spacer(Modifier.height(32.dp))
                    if (twoColumns) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.Top) {
                            RecipeSection(Modifier.weight(0.4f), showEntryNotice,
                                onAddRecipe = { showEntryNotice = true }, onDismissNotice = { showEntryNotice = false })
                            ScheduleSection(Modifier.weight(0.6f))
                        }
                    } else {
                        RecipeSection(Modifier.fillMaxWidth(), showEntryNotice,
                            onAddRecipe = { showEntryNotice = true }, onDismissNotice = { showEntryNotice = false })
                        Spacer(Modifier.height(28.dp))
                        ScheduleSection(Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(28.dp))
                    Body("Mehl. Wasser. Zeit. Und ein guter Plan.", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun Header(compact: Boolean) {
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().padding(horizontal = if (compact) 20.dp else 32.dp), contentAlignment = Alignment.Center) {
            Row(Modifier.widthIn(max = 1024.dp).fillMaxWidth().heightIn(min = 76.dp).padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(38.dp).background(BreadColors.tint, CircleShape), contentAlignment = Alignment.Center) {
                    BreadMark(Modifier.size(29.dp))
                }
                BasicText("Crumb & Crust", style = LocalBreadTypography.current.heading.copy(fontSize = 26.sp))
                Spacer(Modifier.weight(1f))
                if (!compact) {
                    Box(Modifier.size(6.dp).background(BreadColors.sage, CircleShape))
                    Body("Rezepte & Zeitplanung")
                }
            }
        }
        Divider()
    }
}

@Composable
private fun RecipeSection(modifier: Modifier, showNotice: Boolean, onAddRecipe: () -> Unit, onDismissNotice: () -> Unit) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth().height(32.dp), verticalAlignment = Alignment.CenterVertically) {
            Label("MEINE REZEPTE", Modifier.semantics { heading() })
            Spacer(Modifier.weight(1f))
            BasicText("0 Rezepte", style = LocalBreadTypography.current.body.copy(fontSize = 11.sp))
        }
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxWidth().panel().padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(12.dp))
            Box(Modifier.size(88.dp).background(BreadColors.tint, CircleShape), contentAlignment = Alignment.Center) {
                BreadMark(Modifier.size(64.dp))
            }
            Spacer(Modifier.height(24.dp))
            Heading("Dein erstes Brot wartet.", Modifier.fillMaxWidth(), TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Body("Sammle hier deine Lieblingsrezepte – vom schnellen Hefebrot bis zum Sauerteig.",
                Modifier.fillMaxWidth(), TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            BreadButton("Rezept hinzufügen", onAddRecipe, Modifier.fillMaxWidth(), showPlus = true)
            Spacer(Modifier.height(12.dp))
            Body("Deine Sammlung beginnt mit einem Rezept.", Modifier.fillMaxWidth(), TextAlign.Center, small = true)
            if (showNotice) {
                Spacer(Modifier.height(20.dp))
                Column(Modifier.fillMaxWidth().background(BreadColors.tint, RoundedCornerShape(8.dp)).padding(14.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }) {
                    Body("Rezepte hinzufügen ist noch nicht verfügbar.")
                    Spacer(Modifier.height(8.dp))
                    BreadButton("Verstanden", onDismissNotice, Modifier.fillMaxWidth(), secondary = true)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BasicText("↳", style = LocalBreadTypography.current.body.copy(color = BreadColors.accent, fontSize = 18.sp))
            Body("Zutaten, Mengen und Arbeitsschritte bleiben bei deinem Rezept zusammen.", Modifier.weight(1f), small = true)
        }
    }
}

@Composable
private fun ScheduleSection(modifier: Modifier) {
    Column(modifier) {
        Box(Modifier.height(32.dp), contentAlignment = Alignment.CenterStart) {
            Label("DEIN BACKPLAN", Modifier.semantics { heading() })
        }
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxWidth().panel().padding(26.dp)) {
            Heading("Schritt für Schritt zum Brot")
            Spacer(Modifier.height(6.dp))
            Body("Dein Rezept gibt den Rhythmus vor.", small = true)
            Spacer(Modifier.height(22.dp))
            Divider()
            Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ScheduleIllustration(Modifier.size(200.dp, 160.dp))
                Spacer(Modifier.height(20.dp))
                Heading("Noch kein Rezept ausgewählt", Modifier.fillMaxWidth(), TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Body("Wähle ein Rezept aus deiner Sammlung aus. Hier erscheint dann der Ablauf mit allen Arbeitsschritten und Ruhezeiten.",
                    Modifier.widthIn(max = 340.dp).fillMaxWidth(), TextAlign.Center)
                Spacer(Modifier.height(16.dp))
            }
            Divider()
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(6.dp).background(BreadColors.sage, CircleShape))
                Body("Vom ersten Mischen bis zum letzten Abkühlen.", Modifier.weight(1f), small = true)
            }
        }
    }
}

@Composable
private fun BreadButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, showPlus: Boolean = false, secondary: Boolean = false) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val hovered by interaction.collectIsHoveredAsState()
    val shape = RoundedCornerShape(8.dp)
    Row(modifier
        .border(2.dp, if (focused) BreadColors.ink else Color.Transparent, RoundedCornerShape(11.dp))
        .padding(3.dp).clip(shape)
        .background(if (secondary) BreadColors.paper else if (hovered) BreadColors.accentHover else BreadColors.accent)
        .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
        .heightIn(min = 48.dp).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        val color = if (secondary) BreadColors.accent else Color.White
        if (showPlus) {
            BasicText("+", style = LocalBreadTypography.current.body.copy(color = color, fontSize = 22.sp))
            Spacer(Modifier.width(10.dp))
        }
        BasicText(label, style = LocalBreadTypography.current.body.copy(color = color, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center))
    }
}

private fun Modifier.panel() = background(BreadColors.paper, RoundedCornerShape(16.dp))
    .border(1.dp, BreadColors.line, RoundedCornerShape(16.dp))

@Composable
private fun Divider() = Box(Modifier.fillMaxWidth().height(1.dp).background(BreadColors.line))

@Composable
private fun Label(text: String, modifier: Modifier = Modifier) = BasicText(text, modifier, style = LocalBreadTypography.current.label)

@Composable
private fun Heading(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) =
    BasicText(text, modifier.semantics { heading() }, style = LocalBreadTypography.current.heading.copy(textAlign = textAlign))

@Composable
private fun Body(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start, small: Boolean = false) =
    BasicText(text, modifier, style = LocalBreadTypography.current.body.copy(textAlign = textAlign, fontSize = if (small) 11.sp else 13.sp, lineHeight = if (small) 18.sp else 21.sp))
