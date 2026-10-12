package com.example.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnimationProject
import com.example.model.Layer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.PlayheadRed
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioToolbarBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TimelineView(
  project: AnimationProject,
  isPlaying: Boolean,
  onPlayPauseToggle: () -> Unit,
  onFrameSeek: (Int) -> Unit,
  onToggleKeyframe: () -> Unit,
  onPrevKeyframe: () -> Unit,
  onNextKeyframe: () -> Unit,
  onOpenCurveEditor: () -> Unit,
  onSelectLayer: (String) -> Unit,
  onToggleLayerVisibility: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val activeLayer = project.getSelectedLayer()
  val hasKeyframe = activeLayer?.hasKeyframeAt(project.currentFrame) == true

  // Format timecode (e.g. 00:01:15)
  val seconds = project.currentFrame / project.fps
  val framePart = project.currentFrame % project.fps
  val timeCode = String.format("%02d:%02d:%02d", seconds / 60, seconds % 60, framePart)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(StudioToolbarBg)
      .border(1.dp, StudioBorder)
  ) {
    // Top Control Strip: Timecode, Playback Controls, Keyframe Diamond, Curve button
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Timecode & Frame Indicator
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StudioDarkBg)
            .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = timeCode,
            color = NeonMint,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "F ${project.currentFrame}/${project.durationFrames}",
          color = TextSecondary,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      // Center Transport Controls
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Step back
        IconButton(
          onClick = { onFrameSeek((project.currentFrame - 1).coerceAtLeast(0)) },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(Icons.Default.ChevronLeft, contentDescription = "Step Back", tint = TextPrimary)
        }

        // Play / Pause Button (Alight Motion center green toggle)
        IconButton(
          onClick = onPlayPauseToggle,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(NeonMint)
            .testTag("play_pause_button")
        ) {
          Icon(
            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
          )
        }

        // Step forward
        IconButton(
          onClick = { onFrameSeek((project.currentFrame + 1).coerceAtMost(project.durationFrames)) },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(Icons.Default.ChevronRight, contentDescription = "Step Forward", tint = TextPrimary)
        }
      }

      // Right Keyframing Tools
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Prev Keyframe
        IconButton(onClick = onPrevKeyframe, modifier = Modifier.size(30.dp)) {
          Icon(Icons.Default.FastRewind, contentDescription = "Prev Key", tint = TextDim, modifier = Modifier.size(18.dp))
        }

        // Keyframe Diamond Button (Turns glowing Gold when active keyframe exists!)
        IconButton(
          onClick = onToggleKeyframe,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (hasKeyframe) AmberGold.copy(alpha = 0.2f) else StudioDarkBg)
            .border(1.5.dp, if (hasKeyframe) AmberGold else StudioBorder, CircleShape)
            .testTag("keyframe_diamond_button")
        ) {
          // Draw Keyframe Diamond
          Canvas(modifier = Modifier.size(16.dp)) {
            val path = Path().apply {
              moveTo(size.width / 2f, 0f)
              lineTo(size.width, size.height / 2f)
              lineTo(size.width / 2f, size.height)
              lineTo(0f, size.height / 2f)
              close()
            }
            drawPath(path = path, color = if (hasKeyframe) AmberGold else TextSecondary, style = if (hasKeyframe) Fill else Stroke(2f))
          }
        }

        // Next Keyframe
        IconButton(onClick = onNextKeyframe, modifier = Modifier.size(30.dp)) {
          Icon(Icons.Default.FastForward, contentDescription = "Next Key", tint = TextDim, modifier = Modifier.size(18.dp))
        }

        // Easing Curve Editor Icon (Alight Motion curve graph)
        IconButton(
          onClick = onOpenCurveEditor,
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(ElectricCyan.copy(alpha = 0.15f))
            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .testTag("open_curve_editor_button")
        ) {
          Icon(Icons.Default.ShowChart, contentDescription = "Easing Graph", tint = ElectricCyan, modifier = Modifier.size(18.dp))
        }
      }
    }

    // Ruler & Tracks Scrubber Area
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(115.dp)
        .background(StudioDarkBg)
    ) {
      Row(modifier = Modifier.fillMaxSize()) {
        // Left Track Labels Header
        Column(
          modifier = Modifier
            .width(130.dp)
            .fillMaxHeight()
            .background(StudioCardBg)
            .border(width = 0.5.dp, color = StudioBorder)
        ) {
          // Ruler header
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(24.dp)
              .background(StudioToolbarBg)
              .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Text("LAYERS", color = TextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }

          // Layer items
          project.layers.forEach { layer ->
            val isSel = layer.id == activeLayer?.id
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(if (isSel) NeonMint.copy(alpha = 0.15f) else Color.Transparent)
                .clickable { onSelectLayer(layer.id) }
                .padding(horizontal = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = layer.name,
                color = if (isSel) NeonMint else TextPrimary,
                fontSize = 11.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                modifier = Modifier.weight(1f)
              )
              IconButton(
                onClick = { onToggleLayerVisibility(layer.id) },
                modifier = Modifier.size(22.dp)
              ) {
                Icon(
                  if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = "Toggle Visibility",
                  tint = if (layer.isVisible) TextSecondary else TextDim,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        }

        // Right Timeline Ruler & Keyframe Tracks Canvas (Interactive Scrubber)
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .pointerInput(project.durationFrames) {
              detectTapGestures { offset ->
                val frame = ((offset.x / size.width) * project.durationFrames).toInt().coerceIn(0, project.durationFrames)
                onFrameSeek(frame)
              }
            }
            .pointerInput(project.durationFrames) {
              detectDragGestures { change, _ ->
                change.consume()
                val frame = ((change.position.x / size.width) * project.durationFrames).toInt().coerceIn(0, project.durationFrames)
                onFrameSeek(frame)
              }
            }
            .testTag("timeline_scrubber_canvas")
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val totalFrames = project.durationFrames.toFloat()

            // 1. Draw Ruler Ticks at top (24dp height)
            val rulerHeight = 24f
            drawRect(color = Color(0xFF181A20), topLeft = Offset.Zero, size = androidx.compose.ui.geometry.Size(width, rulerHeight))
            drawLine(color = Color(0xFF2E3340), start = Offset(0f, rulerHeight), end = Offset(width, rulerHeight), strokeWidth = 1f)

            val frameStep = if (totalFrames > 60) 10 else 5
            for (f in 0..project.durationFrames step frameStep) {
              val x = (f / totalFrames) * width
              val isMajor = f % (frameStep * 2) == 0
              val tickH = if (isMajor) 14f else 8f
              drawLine(
                color = if (isMajor) Color(0xFF8B92A2) else Color(0xFF454B5A),
                start = Offset(x, rulerHeight - tickH),
                end = Offset(x, rulerHeight),
                strokeWidth = 1f
              )
            }

            // 2. Draw Layer Tracks and Keyframe Diamonds
            val trackHeight = 28f
            project.layers.forEachIndexed { idx, layer ->
              val trackY = rulerHeight + idx * trackHeight
              // Subtle track separator
              drawLine(
                color = Color(0x332E3340),
                start = Offset(0f, trackY + trackHeight),
                end = Offset(width, trackY + trackHeight),
                strokeWidth = 1f
              )

              // Collect keyframes on this layer
              val keyframeFrames = mutableSetOf<Int>()
              keyframeFrames.addAll(layer.posX.keyframes.map { it.frame })
              keyframeFrames.addAll(layer.posY.keyframes.map { it.frame })
              keyframeFrames.addAll(layer.rotation.keyframes.map { it.frame })
              keyframeFrames.addAll(layer.scaleX.keyframes.map { it.frame })
              layer.boneAngleTracks.values.forEach { track ->
                keyframeFrames.addAll(track.keyframes.map { it.frame })
              }

              // Draw diamond markers for keyframes
              val diamondCenterY = trackY + trackHeight / 2f
              val diamondRadius = 5.5f
              keyframeFrames.forEach { kFrame ->
                val kX = (kFrame / totalFrames) * width
                val diaPath = Path().apply {
                  moveTo(kX, diamondCenterY - diamondRadius)
                  lineTo(kX + diamondRadius, diamondCenterY)
                  lineTo(kX, diamondCenterY + diamondRadius)
                  lineTo(kX - diamondRadius, diamondCenterY)
                  close()
                }
                drawPath(path = diaPath, color = AmberGold, style = Fill)
                drawPath(path = diaPath, color = Color.Black, style = Stroke(1f))
              }
            }

            // 3. Draw Playhead (Red needle with top triangle)
            val playheadX = (project.currentFrame / totalFrames) * width

            // Top Triangle Cap
            val capPath = Path().apply {
              moveTo(playheadX - 6f, 0f)
              lineTo(playheadX + 6f, 0f)
              lineTo(playheadX, rulerHeight)
              close()
            }
            drawPath(path = capPath, color = PlayheadRed, style = Fill)

            // Red vertical scrubber line
            drawLine(
              color = PlayheadRed,
              start = Offset(playheadX, rulerHeight),
              end = Offset(playheadX, height),
              strokeWidth = 2f
            )
          }
        }
      }
    }
  }
}
