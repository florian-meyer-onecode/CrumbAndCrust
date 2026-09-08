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
internal fun Header(compact: Boolean) {
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
internal fun BreadButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, showPlus: Boolean = false, secondary: Boolean = false, enabled: Boolean = true) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val hovered by interaction.collectIsHoveredAsState()
    val shape = RoundedCornerShape(8.dp)
    Row(modifier
        .border(2.dp, if (focused) BreadColors.ink else Color.Transparent, RoundedCornerShape(11.dp))
        .padding(3.dp).clip(shape)
        .background(if (!enabled) BreadColors.line else if (secondary) BreadColors.paper else if (hovered) BreadColors.accentHover else BreadColors.accent)
        .border(1.dp, if (secondary) BreadColors.line else Color.Transparent, shape)
        .clickable(enabled = enabled, interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
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

internal fun Modifier.panel() = background(BreadColors.paper, RoundedCornerShape(16.dp))
    .border(1.dp, BreadColors.line, RoundedCornerShape(16.dp))

@Composable
internal fun Divider() = Box(Modifier.fillMaxWidth().height(1.dp).background(BreadColors.line))

@Composable
internal fun Label(text: String, modifier: Modifier = Modifier) = BasicText(text, modifier, style = LocalBreadTypography.current.label)

@Composable
internal fun Heading(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) =
    BasicText(text, modifier.semantics { heading() }, style = LocalBreadTypography.current.heading.copy(textAlign = textAlign))

@Composable
internal fun Body(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start, small: Boolean = false) =
    BasicText(text, modifier, style = LocalBreadTypography.current.body.copy(textAlign = textAlign, fontSize = if (small) 11.sp else 13.sp, lineHeight = if (small) 18.sp else 21.sp))
