package net.onefivefour.crumbandcrust.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale

// Decorative vector artwork is drawn locally on both platforms.
@Composable
internal fun BreadMark(modifier: Modifier, color: Color = BreadColors.accent) {
    Canvas(modifier) {
        scale(size.width / 64f, size.height / 64f, pivot = Offset.Zero) {
            val loaf = Path().apply {
                moveTo(10f, 43f)
                cubicTo(5f, 29f, 19f, 14f, 35f, 15f)
                cubicTo(51f, 15f, 59f, 31f, 54f, 43f)
                cubicTo(52f, 51f, 14f, 52f, 10f, 43f)
                close()
            }
            drawPath(loaf, color, style = Stroke(1.8f, cap = StrokeCap.Round))
            drawLine(color, Offset(18f, 32f), Offset(26f, 23f), 1.8f, StrokeCap.Round)
            drawLine(color, Offset(29f, 35f), Offset(37f, 24f), 1.8f, StrokeCap.Round)
            drawLine(color, Offset(40f, 37f), Offset(47f, 29f), 1.8f, StrokeCap.Round)
            drawLine(color.copy(alpha = .35f), Offset(16f, 44f), Offset(48f, 44f), 1.2f, StrokeCap.Round)
        }
    }
}

@Composable
internal fun ScheduleIllustration(modifier: Modifier) {
    Canvas(modifier) {
        scale(size.width / 200f, size.height / 160f, pivot = Offset.Zero) {
            drawCircle(BreadColors.tint, 65f, Offset(100f, 80f))
            drawRoundRect(BreadColors.paper, Offset(51f, 20f), Size(100f, 120f), androidx.compose.ui.geometry.CornerRadius(10f))
            drawRoundRect(BreadColors.line, Offset(51f, 20f), Size(100f, 120f), androidx.compose.ui.geometry.CornerRadius(10f), style = Stroke(1.5f))
            drawLine(BreadColors.accent.copy(alpha = .5f), Offset(69f, 40f), Offset(115f, 40f), 2f, StrokeCap.Round)
            drawLine(BreadColors.line, Offset(72f, 62f), Offset(72f, 113f), 1.5f)
            listOf(62f, 87f, 112f).forEachIndexed { index, y ->
                drawCircle(BreadColors.paper, 4f, Offset(72f, y))
                drawCircle(if (index == 2) BreadColors.sage else BreadColors.accent.copy(alpha = .6f), 4f, Offset(72f, y), style = Stroke(1.5f))
                drawLine(BreadColors.line, Offset(85f, y), Offset(if (index == 1) 119f else 132f, y), 3f, StrokeCap.Round)
            }
            drawCircle(BreadColors.tint, 24f, Offset(149f, 119f))
            drawCircle(BreadColors.accent.copy(alpha = .65f), 18f, Offset(149f, 119f), style = Stroke(1.5f))
            drawLine(BreadColors.accent, Offset(149f, 119f), Offset(149f, 109f), 1.5f, StrokeCap.Round)
            drawLine(BreadColors.accent, Offset(149f, 119f), Offset(157f, 123f), 1.5f, StrokeCap.Round)
        }
    }
}
