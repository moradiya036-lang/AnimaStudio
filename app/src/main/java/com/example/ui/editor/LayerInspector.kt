package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnimationProject
import com.example.model.Layer
import com.example.model.LayerType
import com.example.model.ShapeType
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardHover
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LayerInspector(
  project: AnimationProject,
  activeLayer: Layer?,
  onAddNewLayer: (LayerType) -> Unit,
  onDuplicateLayer: () -> Unit,
  onDeleteLayer: () -> Unit,
  onMoveUp: () -> Unit,
  onMoveDown: () -> Unit,
  onPropertyChange: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxWidth()
      .height(260.dp)
      .background(StudioDarkBg)
      .border(1.dp, StudioBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
      .padding(12.dp)
      .verticalScroll(scrollState)
      .testTag("layer_inspector")
  ) {
    // Header & Actions
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = activeLayer?.name ?: "No Layer Selected",
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Type: ${activeLayer?.type ?: "None"}",
          color = NeonMint,
          fontSize = 11.sp
        )
      }

      // Layer Actions
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDuplicateLayer, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDeleteLayer, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (activeLayer != null) {
      // 1. Transform Sliders (Position X, Position Y, Scale, Rotation, Opacity)
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Text("Transform Properties", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(6.dp))

          // Position X
          PropertySliderRow(
            label = "Pos X",
            value = activeLayer.posX.getValueAt(project.currentFrame.toFloat()),
            range = -300f..300f,
            onValueChange = {
              activeLayer.posX.defaultValue = it
              if (activeLayer.posX.hasKeyframeAt(project.currentFrame)) {
                activeLayer.posX.addOrUpdateKeyframe(project.currentFrame, it)
              }
              onPropertyChange()
            }
          )

          // Position Y
          PropertySliderRow(
            label = "Pos Y",
            value = activeLayer.posY.getValueAt(project.currentFrame.toFloat()),
            range = -300f..300f,
            onValueChange = {
              activeLayer.posY.defaultValue = it
              if (activeLayer.posY.hasKeyframeAt(project.currentFrame)) {
                activeLayer.posY.addOrUpdateKeyframe(project.currentFrame, it)
              }
              onPropertyChange()
            }
          )

          // Rotation
          PropertySliderRow(
            label = "Rotation",
            value = activeLayer.rotation.getValueAt(project.currentFrame.toFloat()),
            range = -180f..180f,
            unit = "°",
            onValueChange = {
              activeLayer.rotation.defaultValue = it
              if (activeLayer.rotation.hasKeyframeAt(project.currentFrame)) {
                activeLayer.rotation.addOrUpdateKeyframe(project.currentFrame, it)
              }
              onPropertyChange()
            }
          )

          // Scale
          PropertySliderRow(
            label = "Scale",
            value = activeLayer.scaleX.getValueAt(project.currentFrame.toFloat()),
            range = 0.2f..3.0f,
            onValueChange = {
              activeLayer.scaleX.defaultValue = it
              activeLayer.scaleY.defaultValue = it
              if (activeLayer.scaleX.hasKeyframeAt(project.currentFrame)) {
                activeLayer.scaleX.addOrUpdateKeyframe(project.currentFrame, it)
                activeLayer.scaleY.addOrUpdateKeyframe(project.currentFrame, it)
              }
              onPropertyChange()
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Specialized Layer Controls
      when (activeLayer.type) {
        LayerType.SKELETON -> {
          val rig = activeLayer.skeletalRig
          if (rig != null) {
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = StudioCardBg),
              border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("2-Bone Analytical IK Solver", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  Switch(
                    checked = rig.isIKActive,
                    onCheckedChange = {
                      rig.isIKActive = it
                      onPropertyChange()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonMint)
                  )
                }
                Text(
                  "Law of Cosines automatically poses elbows and knees as IK targets move.",
                  color = TextDim,
                  fontSize = 10.sp
                )
              }
            }
          }
        }

        LayerType.PUPPET_MESH -> {
          val puppet = activeLayer.meshPuppet
          if (puppet != null) {
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = StudioCardBg),
              border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Delaunay Mesh Wireframe", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  Switch(
                    checked = puppet.showWireframe,
                    onCheckedChange = {
                      puppet.showWireframe = it
                      onPropertyChange()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonMint)
                  )
                }
                Text(
                  "Linear Blend Skinning (LBS) deforms vertices dynamically with bone weights.",
                  color = TextDim,
                  fontSize = 10.sp
                )
              }
            }
          }
        }

        LayerType.VECTOR -> {
          val shape = activeLayer.vectorShape
          if (shape != null) {
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = StudioCardBg),
              border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Close Bézier Path", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  Switch(
                    checked = shape.isClosed,
                    onCheckedChange = {
                      shape.isClosed = it
                      onPropertyChange()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonMint)
                  )
                }
                PropertySliderRow(
                  label = "Stroke Width",
                  value = shape.strokeWidth,
                  range = 1f..20f,
                  onValueChange = {
                    shape.strokeWidth = it
                    onPropertyChange()
                  }
                )
              }
            }
          }
        }

        LayerType.SHAPE -> {}
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Add Layer Quick Bar
    Text("Add New Layer", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(6.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Button(
        onClick = { onAddNewLayer(LayerType.VECTOR) },
        colors = ButtonDefaults.buttonColors(containerColor = StudioCardHover, contentColor = TextPrimary),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.weight(1f).height(36.dp)
      ) {
        Text("Vector", fontSize = 11.sp)
      }
      Button(
        onClick = { onAddNewLayer(LayerType.SKELETON) },
        colors = ButtonDefaults.buttonColors(containerColor = StudioCardHover, contentColor = TextPrimary),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.weight(1f).height(36.dp)
      ) {
        Text("Skeleton", fontSize = 11.sp)
      }
      Button(
        onClick = { onAddNewLayer(LayerType.SHAPE) },
        colors = ButtonDefaults.buttonColors(containerColor = StudioCardHover, contentColor = TextPrimary),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.weight(1f).height(36.dp)
      ) {
        Text("Shape", fontSize = 11.sp)
      }
    }
  }
}

@Composable
private fun PropertySliderRow(
  label: String,
  value: Float,
  range: ClosedFloatingPointRange<Float>,
  unit: String = "",
  onValueChange: (Float) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      color = TextDim,
      fontSize = 11.sp,
      modifier = Modifier.width(60.dp)
    )
    Slider(
      value = value.coerceIn(range.start, range.endInclusive),
      onValueChange = onValueChange,
      valueRange = range,
      colors = SliderDefaults.colors(
        thumbColor = NeonMint,
        activeTrackColor = NeonMint,
        inactiveTrackColor = Color(0xFF2E3340)
      ),
      modifier = Modifier.weight(1f)
    )
    Text(
      text = "${String.format("%.1f", value)}$unit",
      color = TextPrimary,
      fontSize = 10.sp,
      modifier = Modifier.width(48.dp)
    )
  }
}
