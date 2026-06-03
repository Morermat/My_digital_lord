package com.example.my_digital_lord

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun InactivityCircleSelector(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int = 5,
    maxValue: Int = 300,
    modifier: Modifier = Modifier.size(200.dp)
) {
    val activeColor = MaterialTheme.colorScheme.secondary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    var dragAngle by remember(value) { mutableStateOf(valueToAngle(value, minValue, maxValue)) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { /* можно ничего не делать */ },
                        onDrag = { change, dragAmount ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touch = change.position
                            val newAngle = atan2(touch.y - center.y, touch.x - center.x)
                            val normalized = (newAngle + 2 * PI.toFloat()) % (2 * PI.toFloat())
                            dragAngle = normalized
                            val newVal = angleToValue(normalized, minValue, maxValue)
                            onValueChange(newVal)
                        }
                    )
                }
        ) {
            val strokeWidth = 16.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2f, size.height / 2f)

            // Track (background circle)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active arc
            val sweep = Math.toDegrees(dragAngle.toDouble()).toFloat()
            drawArc(
                color = activeColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Pointer circle
            val pointerAngleRad = dragAngle - PI.toFloat() / 2f
            val pointerX = center.x + radius * kotlin.math.cos(pointerAngleRad)
            val pointerY = center.y + radius * kotlin.math.sin(pointerAngleRad)
            drawCircle(
                color = activeColor,
                radius = 10.dp.toPx(),
                center = Offset(pointerX, pointerY)
            )
        }

        Text(
            text = "${value}с",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = activeColor
            )
        )
    }
}

private fun valueToAngle(value: Int, min: Int, max: Int): Float {
    val ratio = (value - min).toFloat() / (max - min)
    return ratio * 2 * PI.toFloat()
}

private fun angleToValue(angle: Float, min: Int, max: Int): Int {
    val ratio = angle / (2 * PI.toFloat())
    val raw = min + (max - min) * ratio
    val stepped = ((raw / 5).roundToInt() * 5).coerceIn(min, max)
    return stepped
}