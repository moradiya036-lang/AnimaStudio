package com.example.ui.editor

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.engine.EasingCurve
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.sqrt

@Composable
fun EasingCurveDialog(
  initialCurve: EasingCurve,
  onApplyCurve: (EasingCurve) -> Unit,
  onDismiss: () -> Unit
) {
  var x1 by remember { mutableFloatStateOf(initialCurve.x1) }
  var y1 by remember { mutableFloatStateOf(initialCurve.y1) }
  var x2 by remember { mutableFloatStateOf(initialCurve.x2) }
  var y2 by remember { mutableFloatStateOf(initialCurve.y2) }
  var activeHandle by remember { mutableStateOf<Int?>(null) } // 1 for P1, 2 for P2

  val currentCurve = remember(x1, y1, x2, y2) {
    EasingCurve(x1, y1, x2, y2, "Custom")
  }

  // Animation preview transition
  val infiniteTransition = rememberInfiniteTransition(label = "CurvePreview")
  val rawProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "Progress"
  )
  val easedProgress = currentCurve.evaluate(rawProgress)

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = StudioCardBg,
      border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("easing_curve_dialog")
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Title
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Cubic Bézier Easing Graph", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Fine-tune acceleration & velocity curves", color = TextDim, fontSize = 11.sp)
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val presets = listOf(
            EasingCurve.Linear,
            EasingCurve.EaseIn,
            EasingCurve.EaseOut,
            EasingCurve.EaseInOut,
            EasingCurve.Overshoot
          )
          presets.forEach { preset ->
            val isSel = (x1 == preset.x1 && y1 == preset.y1 && x2 == preset.x2 && y2 == preset.y2)
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSel) NeonMint.copy(alpha = 0.2f) else StudioDarkBg)
                .border(1.dp, if (isSel) NeonMint else StudioBorder, RoundedCornerShape(6.dp))
                .clickable {
                  x1 = preset.x1
                  y1 = preset.y1
                  x2 = preset.x2
                  y2 = preset.y2
                }
                .padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = preset.name,
                color = if (isSel) NeonMint else TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive 2D Easing Curve Canvas (200x200 dp)
        Box(
          modifier = Modifier
            .size(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(StudioDarkBg)
            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
              detectDragGestures(
                onDragStart = { offset ->
                  val w = size.width.toFloat()
                  val h = size.height.toFloat()

                  val p1Screen = Offset(x1 * w, (1f - y1) * h)
                  val p2Screen = Offset(x2 * w, (1f - y2) * h)

                  val d1 = sqrt((offset.x - p1Screen.x) * (offset.x - p1Screen.x) + (offset.y - p1Screen.y) * (offset.y - p1Screen.y))
                  val d2 = sqrt((offset.x - p2Screen.x) * (offset.x - p2Screen.x) + (offset.y - p2Screen.y) * (offset.y - p2Screen.y))

                  activeHandle = when {
                    d1 < 48f -> 1
                    d2 < 48f -> 2
                    d1 < d2 -> 1
                    else -> 2
                  }
                },
                onDrag = { change, dragAmount ->
                  change.consume()
                  val w = size.width.toFloat()
                  val h = size.height.toFloat()

                  if (activeHandle == 1) {
                    x1 = (x1 + dragAmount.x / w).coerceIn(0f, 1f)
                    y1 = (y1 - dragAmount.y / h).coerceIn(-0.5f, 1.5f)
                  } else if (activeHandle == 2) {
                    x2 = (x2 + dragAmount.x / w).coerceIn(0f, 1f)
                    y2 = (y2 - dragAmount.y / h).coerceIn(-0.5f, 1.5f)
                  }
                },
                onDragEnd = { activeHandle = null }
              )
            }
        ) {
          Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val w = size.width
            val h = size.height

            // Grid lines (Diagonal linear guide & center lines)
            drawLine(color = Color(0x33FFFFFF), start = Offset(0f, h), end = Offset(w, 0f), strokeWidth = 1f)
            drawLine(color = Color(0x22FFFFFF), start = Offset(0f, h / 2f), end = Offset(w, h / 2f), strokeWidth = 1f)
            drawLine(color = Color(0x22FFFFFF), start = Offset(w / 2f, 0f), end = Offset(w / 2f, h), strokeWidth = 1f)

            // Screen space points
            val p0 = Offset(0f, h)
            val p1 = Offset(x1 * w, (1f - y1) * h)
            val p2 = Offset(x2 * w, (1f - y2) * h)
            val p3 = Offset(w, 0f)

            // Handle guide lines
            drawLine(color = Color(0x6600E5FF), start = p0, end = p1, strokeWidth = 1.5f)
            drawLine(color = Color(0x66FFD600), start = p3, end = p2, strokeWidth = 1.5f)

            // Draw Cubic Bezier Curve
            val curvePath = Path().apply {
              moveTo(p0.x, p0.y)
              cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
            }
            drawPath(path = curvePath, color = NeonMint, style = Stroke(width = 3.5f))

            // Handle 1 (Cyan)
            drawCircle(color = ElectricCyan, radius = 7f, center = p1)
            drawCircle(color = Color.White, radius = 2.5f, center = p1)

            // Handle 2 (Amber Gold)
            drawCircle(color = AmberGold, radius = 7f, center = p2)
            drawCircle(color = Color.White, radius = 2.5f, center = p2)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Coordinates text
        Text(
          text = "P1: (${String.format("%.2f", x1)}, ${String.format("%.2f", y1)})  |  P2: (${String.format("%.2f", x2)}, ${String.format("%.2f", y2)})",
          color = TextSecondary,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Live Motion Preview Track (Ball sliding with curve velocity)
        Column(modifier = Modifier.fillMaxWidth()) {
          Text("Live Easing Preview", color = TextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(StudioDarkBg)
              .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
              .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val trackWidth = size.width - 24f
              val ballX = 12f + trackWidth * easedProgress.coerceIn(0f, 1f)

              // Track line
              drawLine(color = Color(0x33FFFFFF), start = Offset(12f, size.height / 2f), end = Offset(size.width - 12f, size.height / 2f), strokeWidth = 2f)

              // Motion ball
              drawCircle(color = NeonMint, radius = 7f, center = Offset(ballX, size.height / 2f))
              drawCircle(color = Color.White, radius = 3f, center = Offset(ballX, size.height / 2f))
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Apply Button
        Button(
          onClick = {
            onApplyCurve(EasingCurve(x1, y1, x2, y2, "Custom"))
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
          Text("APPLY EASING CURVE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }
  }
}
