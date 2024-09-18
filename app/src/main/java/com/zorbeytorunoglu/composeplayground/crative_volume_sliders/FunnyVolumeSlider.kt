package com.zorbeytorunoglu.composeplayground.crative_volume_sliders

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun BalanceBar(
    modifier: Modifier = Modifier,
    barLength: Dp = 400.dp,
    barThickness: Dp = 16.dp,
    barColor: Color = Color.Gray,
    ballColor: Color = Color.Red,
    ballRadius: Dp = 12.dp
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val maxDragOffset = 100f

    val rotationAngle = remember { Animatable(0f) }

    var ballPosition by remember { mutableFloatStateOf(0f) }
    var ballVelocity by remember { mutableFloatStateOf(0f) }

    val scope = rememberCoroutineScope()

    val volume = remember(ballPosition) { ((ballPosition + 1) / 2 * 100).roundToInt().coerceIn(0, 100) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { _ ->
                val angle = rotationAngle.value * (PI / 180f).toFloat()
                val gravity = 9.81f
                val acceleration = sin(angle) * gravity

                ballVelocity += acceleration * 0.016f
                ballPosition = (ballPosition + ballVelocity * 0.016f).coerceIn(-1f, 1f)

                ballVelocity *= 0.99f
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Volume: $volume",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        Box(
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(barLength, barThickness)
                    .graphicsLayer {
                        rotationZ = rotationAngle.value
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    rotationAngle.animateTo(
                                        0f,
                                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                                    )
                                }
                            },
                            onDrag = { _, dragAmount ->
                                val newOffset =
                                    (dragOffset + dragAmount.y).coerceIn(
                                        -maxDragOffset,
                                        maxDragOffset
                                    )
                                dragOffset = newOffset
                                scope.launch {
                                    rotationAngle.snapTo((dragOffset / maxDragOffset) * 30f)
                                }
                            }
                        )
                    }
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    drawRect(
                        color = barColor,
                        size = Size(size.width, barThickness.toPx())
                    )

                    val ballX = size.width / 2 + ballPosition * (size.width / 2 - ballRadius.toPx())
                    val ballY = barThickness.toPx() / 2
                    drawCircle(
                        color = ballColor,
                        radius = ballRadius.toPx(),
                        center = Offset(ballX, ballY)
                    )
                }
            }
        }

    }
}