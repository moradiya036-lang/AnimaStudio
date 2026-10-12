package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.NeonMint
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioToolbarBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class VectorToolMode {
  SELECT_NODE,
  BEZIER_HANDLE,
  ADD_POINT
}

/**
 * Recreates the exact Vector Editing Toolbar and "Swipe to move point" touchpad
 * from the user's uploaded screenshot (IMG_20261010_093037.jpg).
 */
@Composable
fun VectorTouchPadOverlay(
  activeTool: VectorToolMode,
  onToolSelect: (VectorToolMode) -> Unit,
  onRemovePoint: () -> Unit,
  onAddPoint: () -> Unit,
  onCloseVectorEditor: () -> Unit,
  onSwipeMove: (dx: Float, dy: Float) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(180.dp)
      .background(Color(0xE614161F))
      .border(1.dp, StudioBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
      .padding(8.dp)
  ) {
    // Left Toolbar (mirrors screenshot left vertical rail)
    Column(
      modifier = Modifier
        .width(56.dp)
        .fillMaxHeight(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Back button
      IconButton(
        onClick = onCloseVectorEditor,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = TextPrimary
        )
      }

      // Tool 1: Node Selection (Circle with 4 anchor points)
      val isNodeSel = activeTool == VectorToolMode.SELECT_NODE
      IconButton(
        onClick = { onToolSelect(VectorToolMode.SELECT_NODE) },
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(if (isNodeSel) NeonMint.copy(alpha = 0.2f) else Color.Transparent)
          .border(if (isNodeSel) 1.5.dp else 0.dp, if (isNodeSel) NeonMint else Color.Transparent, CircleShape)
      ) {
        Icon(
          Icons.Default.Grain,
          contentDescription = "Select Node",
          tint = if (isNodeSel) NeonMint else TextSecondary,
          modifier = Modifier.size(24.dp)
        )
      }

      // Tool 2: Bézier Curve Handles (Arc with control handles)
      val isBezier = activeTool == VectorToolMode.BEZIER_HANDLE
      IconButton(
        onClick = { onToolSelect(VectorToolMode.BEZIER_HANDLE) },
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(if (isBezier) NeonMint.copy(alpha = 0.2f) else Color.Transparent)
          .border(if (isBezier) 1.5.dp else 0.dp, if (isBezier) NeonMint else Color.Transparent, CircleShape)
      ) {
        Icon(
          Icons.Default.CallSplit,
          contentDescription = "Bézier Handles",
          tint = if (isBezier) NeonMint else TextSecondary,
          modifier = Modifier.size(22.dp)
        )
      }

      // Tool 3: Minus / Remove Point button (as in screenshot)
      IconButton(
        onClick = onRemovePoint,
        modifier = Modifier.size(38.dp)
      ) {
        Icon(
          Icons.Default.Remove,
          contentDescription = "Remove Point",
          tint = Color(0xFFFF5252),
          modifier = Modifier.size(22.dp)
        )
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Center Virtual Touchpad ("Swipe to move point" with corner brackets)
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight()
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0xFF1E222D))
        .border(1.dp, Color(0xFF333A4D), RoundedCornerShape(12.dp))
        .pointerInput(Unit) {
          detectDragGestures { change, dragAmount ->
            change.consume()
            // Sensible scaling for touch precision
            onSwipeMove(dragAmount.x * 0.9f, dragAmount.y * 0.9f)
          }
        }
        .testTag("vector_swipe_touchpad"),
      contentAlignment = Alignment.Center
    ) {
      // Corner brackets decoration (as in screenshot)
      Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        // Top-Left corner bracket
        Text("┌", color = Color(0xFF6B7488), fontSize = 18.sp, modifier = Modifier.align(Alignment.TopStart))
        // Top-Right corner bracket
        Text("┐", color = Color(0xFF6B7488), fontSize = 18.sp, modifier = Modifier.align(Alignment.TopEnd))
        // Bottom-Left corner bracket
        Text("└", color = Color(0xFF6B7488), fontSize = 18.sp, modifier = Modifier.align(Alignment.BottomStart))
        // Bottom-Right corner bracket
        Text("┘", color = Color(0xFF6B7488), fontSize = 18.sp, modifier = Modifier.align(Alignment.BottomEnd))
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
          Icons.Default.Gesture,
          contentDescription = null,
          tint = Color(0xFF757F96),
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Swipe to move point",
          color = Color(0xFFB0B8C8),
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = if (activeTool == VectorToolMode.BEZIER_HANDLE) "Editing Bézier Curve Handle" else "Editing Anchor Node",
          color = AmberGold,
          fontSize = 10.sp
        )
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Right Action: Add point button
    Column(
      modifier = Modifier
        .width(44.dp)
        .fillMaxHeight(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      IconButton(
        onClick = onAddPoint,
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(NeonMint.copy(alpha = 0.2f))
          .border(1.dp, NeonMint, CircleShape)
      ) {
        Icon(
          Icons.Default.Add,
          contentDescription = "Add Point",
          tint = NeonMint,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}
