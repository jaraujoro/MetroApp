package com.metroapp.lima.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metroapp.lima.ui.theme.MetroGreenLight
import com.metroapp.lima.ui.theme.MetroGreenPrimary

@Composable
fun SimpleBarChart(
    title: String,
    dataPoints: List<Pair<String, Long>>, // Label to amount in cents
    modifier: Modifier = Modifier,
    barColor: Color = MetroGreenPrimary
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (dataPoints.isEmpty() || dataPoints.all { it.second == 0L }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin gastos registrados en este periodo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                val maxVal = (dataPoints.maxOfOrNull { it.second } ?: 100L).coerceAtLeast(150L)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val width = size.width
                    val height = size.height - 30f // Leave space for labels
                    val barCount = dataPoints.size
                    val slotWidth = width / barCount
                    val barWidth = (slotWidth * 0.6f).coerceAtMost(36.dp.toPx())

                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.DKGRAY
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }

                    val valPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                        isAntiAlias = true
                    }

                    dataPoints.forEachIndexed { index, (label, cents) ->
                        val barHeight = (cents.toFloat() / maxVal) * (height - 25f)
                        val left = (index * slotWidth) + (slotWidth - barWidth) / 2f
                        val top = height - barHeight

                        // Draw Bar
                        drawRoundRect(
                            color = if (cents > 0) barColor else Color.LightGray.copy(alpha = 0.5f),
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight.coerceAtLeast(6f)),
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // Value on top if > 0
                        if (cents > 0) {
                            val solesStr = "S/${cents / 100.0}"
                            drawContext.canvas.nativeCanvas.drawText(
                                solesStr,
                                left + barWidth / 2f,
                                (top - 8f).coerceAtLeast(20f),
                                valPaint
                            )
                        }

                        // Label below
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            left + barWidth / 2f,
                            size.height - 4f,
                            textPaint
                        )
                    }
                }
            }
        }
    }
}
