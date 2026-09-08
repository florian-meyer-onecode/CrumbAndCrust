package net.onefivefour.crumbandcrust.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import net.onefivefour.crumbandcrust.resources.Res
import net.onefivefour.crumbandcrust.resources.newsreader
import net.onefivefour.crumbandcrust.resources.plus_jakarta_sans
import org.jetbrains.compose.resources.Font

internal object BreadColors {
    val background = Color(0xFFFFF8F5)
    val paper = Color.White
    val tint = Color(0xFFFFF1E8)
    val accent = Color(0xFF90490B)
    val accentHover = Color(0xFF723600)
    val ink = Color(0xFF231A11)
    val muted = Color(0xFF756357)
    val line = Color(0xFFEADDD3)
    val sage = Color(0xFF536345)
}

internal data class BreadTypography(
    val body: TextStyle,
    val heading: TextStyle,
    val label: TextStyle,
)

internal val LocalBreadTypography = staticCompositionLocalOf {
    BreadTypography(TextStyle.Default, TextStyle.Default, TextStyle.Default)
}

@Composable
internal fun BreadTheme(content: @Composable () -> Unit) {
    val sans = FontFamily(Font(Res.font.plus_jakarta_sans))
    val serif = FontFamily(Font(Res.font.newsreader))
    CompositionLocalProvider(
        LocalBreadTypography provides BreadTypography(
            body = TextStyle(fontFamily = sans, fontSize = 13.sp, lineHeight = 21.sp, color = BreadColors.muted),
            heading = TextStyle(fontFamily = serif, fontSize = 26.sp, lineHeight = 32.sp, color = BreadColors.ink),
            label = TextStyle(fontFamily = sans, fontSize = 11.sp, lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp, color = BreadColors.muted),
        ),
        content = content,
    )
}
