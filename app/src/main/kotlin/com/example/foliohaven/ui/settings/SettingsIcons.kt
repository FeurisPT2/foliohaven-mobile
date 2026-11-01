package com.example.foliohaven.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Native Jetpack Compose Canvas vector icons for FolioHaven Settings screen.
 * 100% self-contained with zero third-party icon library dependencies.
 */

@Composable
fun SettingsGearIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val strokeWidth = 2.dp.toPx()

        // Outer gear ring
        drawCircle(
            color = color,
            radius = size.width * 0.22f,
            center = Offset(cx, cy),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Center hub dot
        drawCircle(
            color = color,
            radius = size.width * 0.08f,
            center = Offset(cx, cy)
        )

        // 8 gear cogs radiating from ring
        for (i in 0 until 8) {
            val angle = (i * 45f) * (Math.PI / 180f).toFloat()
            val rIn = size.width * 0.28f
            val rOut = size.width * 0.44f
            val startX = cx + cos(angle) * rIn
            val startY = cy + sin(angle) * rIn
            val endX = cx + cos(angle) * rOut
            val endY = cy + sin(angle) * rOut

            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SlidersTuneIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = 1.8.dp.toPx()
        val knobR = 2.5.dp.toPx()

        // Row 1 Track & Knob (Slider 1)
        val y1 = size.height * 0.26f
        drawLine(
            color = color,
            start = Offset(size.width * 0.15f, y1),
            end = Offset(size.width * 0.85f, y1),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawCircle(color = color, radius = knobR, center = Offset(size.width * 0.38f, y1))

        // Row 2 Track & Knob (Slider 2)
        val y2 = size.height * 0.50f
        drawLine(
            color = color,
            start = Offset(size.width * 0.15f, y2),
            end = Offset(size.width * 0.85f, y2),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawCircle(color = color, radius = knobR, center = Offset(size.width * 0.68f, y2))

        // Row 3 Track & Knob (Slider 3)
        val y3 = size.height * 0.74f
        drawLine(
            color = color,
            start = Offset(size.width * 0.15f, y3),
            end = Offset(size.width * 0.85f, y3),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawCircle(color = color, radius = knobR, center = Offset(size.width * 0.32f, y3))
    }
}

@Composable
fun TtsVoiceIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val xFractions = listOf(0.18f, 0.34f, 0.50f, 0.66f, 0.82f)
        val heightFractions = listOf(0.38f, 0.72f, 0.92f, 0.64f, 0.36f)

        for (i in xFractions.indices) {
            val x = size.width * xFractions[i]
            val h = size.height * heightFractions[i]
            val yStart = (size.height - h) / 2f
            drawLine(
                color = color,
                start = Offset(x, yStart),
                end = Offset(x, yStart + h),
                strokeWidth = 2.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun StorageDbIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val arcWidth = size.width * 0.70f
        val arcHeight = size.height * 0.22f
        val left = size.width * 0.15f
        val right = left + arcWidth

        // Top cylinder disc (full oval)
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(left, size.height * 0.12f),
            size = Size(arcWidth, arcHeight),
            style = stroke
        )

        // Middle cylinder lower rim
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(left, size.height * 0.38f),
            size = Size(arcWidth, arcHeight),
            style = stroke
        )

        // Bottom cylinder lower rim
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(left, size.height * 0.64f),
            size = Size(arcWidth, arcHeight),
            style = stroke
        )

        // Side vertical walls
        val topCy = size.height * 0.23f
        val bottomCy = size.height * 0.75f
        drawLine(
            color = color,
            start = Offset(left, topCy),
            end = Offset(left, bottomCy),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(right, topCy),
            end = Offset(right, bottomCy),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun ShieldCheckIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = Stroke(
            width = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Shield border
        val path = Path().apply {
            moveTo(size.width * 0.50f, size.height * 0.12f)
            lineTo(size.width * 0.85f, size.height * 0.24f)
            lineTo(size.width * 0.85f, size.height * 0.54f)
            cubicTo(
                size.width * 0.85f, size.height * 0.76f,
                size.width * 0.50f, size.height * 0.90f,
                size.width * 0.50f, size.height * 0.90f
            )
            cubicTo(
                size.width * 0.50f, size.height * 0.90f,
                size.width * 0.15f, size.height * 0.76f,
                size.width * 0.15f, size.height * 0.54f
            )
            lineTo(size.width * 0.15f, size.height * 0.24f)
            close()
        }
        drawPath(path, color = color, style = stroke)

        // Internal checkmark
        val checkPath = Path().apply {
            moveTo(size.width * 0.34f, size.height * 0.52f)
            lineTo(size.width * 0.46f, size.height * 0.64f)
            lineTo(size.width * 0.66f, size.height * 0.42f)
        }
        drawPath(checkPath, color = color, style = stroke)
    }
}

@Composable
fun TrashIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = Stroke(
            width = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Horizontal lid rim
        drawLine(
            color = color,
            start = Offset(size.width * 0.20f, size.height * 0.26f),
            end = Offset(size.width * 0.80f, size.height * 0.26f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Lid handle
        drawLine(
            color = color,
            start = Offset(size.width * 0.40f, size.height * 0.16f),
            end = Offset(size.width * 0.60f, size.height * 0.16f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Bin body
        val bodyPath = Path().apply {
            moveTo(size.width * 0.27f, size.height * 0.28f)
            lineTo(size.width * 0.32f, size.height * 0.86f)
            lineTo(size.width * 0.68f, size.height * 0.86f)
            lineTo(size.width * 0.73f, size.height * 0.28f)
        }
        drawPath(bodyPath, color = color, style = stroke)

        // Vertical interior slats
        drawLine(
            color = color,
            start = Offset(size.width * 0.43f, size.height * 0.38f),
            end = Offset(size.width * 0.43f, size.height * 0.74f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.57f, size.height * 0.38f),
            end = Offset(size.width * 0.57f, size.height * 0.74f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
