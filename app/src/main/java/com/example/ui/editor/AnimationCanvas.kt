package com.example.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine.InverseKinematics
import com.example.engine.LinearBlendSkinning
import com.example.model.AnimationProject
import com.example.model.Bone
import com.example.model.Layer
import com.example.model.LayerType
import com.example.model.MeshPuppet
import com.example.model.ShapeType
import com.example.model.SkeletalRig
import com.example.model.VectorNode
import com.example.model.VectorShape
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BoneFill
import com.example.ui.theme.BoneJoint
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MeshWireframe
import com.example.ui.theme.NeonMint
import com.example.ui.theme.StudioBorder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun AnimationCanvas(
  project: AnimationProject,
  selectedNodeIndex: Int,
  activeVectorTool: VectorToolMode,
  showGrid: Boolean,
  showOnionSkin: Boolean,
  onSelectNode: (Int) -> Unit,
  onNodeMoved: (index: Int, x: Float, y: Float) -> Unit,
  onHandleMoved: (index: Int, isOutHandle: Boolean, x: Float, y: Float) -> Unit,
  onIKTargetMoved: (boneId: String, x: Float, y: Float) -> Unit,
  modifier: Modifier = Modifier
) {
  var panX by remember { mutableFloatStateOf(0f) }
  var panY by remember { mutableFloatStateOf(0f) }
  var zoom by remember { mutableFloatStateOf(1f) }

  // Drag state
  var draggingBoneId by remember { mutableStateOf<String?>(null) }
  var draggingNodeType by remember { mutableStateOf<String?>(null) } // "anchor", "inHandle", "outHandle"

  val activeLayer = project.getSelectedLayer()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(project.backgroundColor))
      .pointerInput(project.currentFrame, activeLayer?.id, selectedNodeIndex, activeVectorTool) {
        detectTapGestures { offset ->
          val canvasX = (offset.x - panX) / zoom
          val canvasY = (offset.y - panY) / zoom

          // Check if clicked vector node
          val shape = activeLayer?.vectorShape
          if (shape != null) {
            val touchRadius = 36f / zoom
            shape.nodes.forEachIndexed { idx, node ->
              val dist = sqrt((canvasX - node.x) * (canvasX - node.x) + (canvasY - node.y) * (canvasY - node.y))
              if (dist < touchRadius) {
                onSelectNode(idx)
                return@detectTapGestures
              }
            }
          }
        }
      }
      .pointerInput(project.currentFrame, activeLayer?.id, selectedNodeIndex, activeVectorTool) {
        detectDragGestures(
          onDragStart = { startOffset ->
            val canvasX = (startOffset.x - panX) / zoom
            val canvasY = (startOffset.y - panY) / zoom
            val touchRadius = 40f / zoom

            // Check IK target dragging first
            val rig = activeLayer?.skeletalRig
            if (rig != null) {
              val ikBone = rig.bones.firstOrNull { it.isIKTarget && sqrt((canvasX - it.ikTargetX) * (canvasX - it.ikTargetX) + (canvasY - it.ikTargetY) * (canvasY - it.ikTargetY)) < touchRadius }
              if (ikBone != null) {
                draggingBoneId = ikBone.id
                return@detectDragGestures
              }
            }

            // Check Vector anchor or handles
            val shape = activeLayer?.vectorShape
            if (shape != null && selectedNodeIndex in shape.nodes.indices) {
              val node = shape.nodes[selectedNodeIndex]
              val distOut = sqrt((canvasX - node.outHandleX) * (canvasX - node.outHandleX) + (canvasY - node.outHandleY) * (canvasY - node.outHandleY))
              val distIn = sqrt((canvasX - node.inHandleX) * (canvasX - node.inHandleX) + (canvasY - node.inHandleY) * (canvasY - node.inHandleY))
              val distAnchor = sqrt((canvasX - node.x) * (canvasX - node.x) + (canvasY - node.y) * (canvasY - node.y))

              when {
                distOut < touchRadius -> draggingNodeType = "outHandle"
                distIn < touchRadius -> draggingNodeType = "inHandle"
                distAnchor < touchRadius -> draggingNodeType = "anchor"
              }
            }
          },
          onDrag = { change, dragAmount ->
            change.consume()
            val dx = dragAmount.x / zoom
            val dy = dragAmount.y / zoom

            if (draggingBoneId != null) {
              val rig = activeLayer?.skeletalRig
              val bone = rig?.getBone(draggingBoneId!!)
              if (bone != null) {
                val newX = bone.ikTargetX + dx
                val newY = bone.ikTargetY + dy
                onIKTargetMoved(bone.id, newX, newY)
              }
            } else if (draggingNodeType != null) {
              val shape = activeLayer?.vectorShape
              if (shape != null && selectedNodeIndex in shape.nodes.indices) {
                val node = shape.nodes[selectedNodeIndex]
                when (draggingNodeType) {
                  "anchor" -> onNodeMoved(selectedNodeIndex, node.x + dx, node.y + dy)
                  "outHandle" -> onHandleMoved(selectedNodeIndex, true, node.outHandleX + dx, node.outHandleY + dy)
                  "inHandle" -> onHandleMoved(selectedNodeIndex, false, node.inHandleX + dx, node.inHandleY + dy)
                }
              }
            } else {
              // Pan canvas if nothing targeted
              panX += dragAmount.x
              panY += dragAmount.y
            }
          },
          onDragEnd = {
            draggingBoneId = null
            draggingNodeType = null
          },
          onDragCancel = {
            draggingBoneId = null
            draggingNodeType = null
          }
        )
      }
      .testTag("animation_canvas")
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      translate(left = panX, top = panY) {
        scale(scaleX = zoom, scaleY = zoom, pivot = Offset.Zero) {
          // Draw Canvas Frame Bounds (Aspect ratio frame)
          drawRect(
            color = Color(0xFF262A36),
            topLeft = Offset(50f, 50f),
            size = Size(860f, 640f),
            style = Stroke(width = 1.5f)
          )

          // Draw Grid if enabled
          if (showGrid) {
            drawGridLines(width = 960f, height = 740f)
          }

          // Render Onion Skinning (Ghost of frame - 2 and frame + 2)
          if (showOnionSkin && project.durationFrames > 4) {
            val prevFrame = (project.currentFrame - 2).coerceAtLeast(0)
            drawProjectFrame(project, prevFrame.toFloat(), ghostColor = Color(0x3300E5FF))
          }

          // Render Main Project at Current Frame
          drawProjectFrame(project, project.currentFrame.toFloat())

          // If Vector shape is active, draw interactive nodes & handles
          if (activeLayer?.type == LayerType.VECTOR && activeLayer.vectorShape != null) {
            drawVectorInteractiveControls(
              shape = activeLayer.vectorShape!!,
              selectedIndex = selectedNodeIndex,
              layerPosX = activeLayer.posX.getValueAt(project.currentFrame.toFloat()),
              layerPosY = activeLayer.posY.getValueAt(project.currentFrame.toFloat())
            )
          }

          // If Skeletal Rig is active, draw IK target handles
          if (activeLayer?.type == LayerType.SKELETON && activeLayer.skeletalRig != null) {
            drawRigInteractiveHandles(rig = activeLayer.skeletalRig!!)
          }
        }
      }
    }
  }
}

private fun DrawScope.drawGridLines(width: Float, height: Float) {
  val step = 40f
  var x = 0f
  while (x < width) {
    drawLine(color = Color(0x1AFFFFFF), start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 1f)
    x += step
  }
  var y = 0f
  while (y < height) {
    drawLine(color = Color(0x1AFFFFFF), start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1f)
    y += step
  }
}

private fun DrawScope.drawProjectFrame(
  project: AnimationProject,
  frame: Float,
  ghostColor: Color? = null
) {
  for (layer in project.layers) {
    if (!layer.isVisible) continue

    val posX = layer.posX.getValueAt(frame)
    val posY = layer.posY.getValueAt(frame)
    val scaleX = layer.scaleX.getValueAt(frame)
    val scaleY = layer.scaleY.getValueAt(frame)
    val rot = layer.rotation.getValueAt(frame)
    val op = (layer.opacity.getValueAt(frame) * if (ghostColor != null) 0.35f else 1f).coerceIn(0f, 1f)

    translate(left = posX, top = posY) {
      rotate(degrees = rot) {
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
          when (layer.type) {
            LayerType.VECTOR -> {
              val shape = layer.vectorShape
              if (shape != null && shape.nodes.isNotEmpty()) {
                val path = shape.toComposePath()
                val strokeColor = ghostColor ?: Color(shape.strokeColor).copy(alpha = op)
                drawPath(path = path, color = strokeColor, style = Stroke(width = shape.strokeWidth))
                if (shape.fillColor != null && shape.isClosed) {
                  drawPath(path = path, color = Color(shape.fillColor!!).copy(alpha = op), style = Fill)
                }
              }
            }

            LayerType.SKELETON -> {
              val rig = layer.skeletalRig
              if (rig != null) {
                // Apply 2-bone IK solver to limbs before drawing
                solveLimbIK(rig)
                rig.updateHierarchy()
                drawSkeletalRig(rig, ghostColor ?: Color(0xFF00E676).copy(alpha = op))
              }
            }

            LayerType.PUPPET_MESH -> {
              val puppet = layer.meshPuppet
              val rig = layer.skeletalRig
              if (puppet != null) {
                if (rig != null) {
                  rig.updateHierarchy()
                  LinearBlendSkinning.applySkinning(puppet, rig)
                }
                drawPuppetMesh(puppet, ghostColor ?: Color(puppet.wireframeColor).copy(alpha = op))
              }
            }

            LayerType.SHAPE -> {
              drawGeometricShape(layer, ghostColor ?: Color(layer.fillColor).copy(alpha = op))
            }
          }
        }
      }
    }
  }
}

private fun solveLimbIK(rig: SkeletalRig) {
  // Solve Left Arm
  val lUpper = rig.getBone("l_upper_arm")
  val lFore = rig.getBone("l_forearm")
  if (lUpper != null && lFore != null && lFore.isIKTarget) {
    val ikRes = InverseKinematics.solve2Bone(
      x0 = lUpper.startX,
      y0 = lUpper.startY,
      targetX = lFore.ikTargetX,
      targetY = lFore.ikTargetY,
      l1 = lUpper.length,
      l2 = lFore.length,
      flipBend = false
    )
    lUpper.localAngleDegrees = ikRes.bone1AngleDeg - (rig.getBone(lUpper.parentId ?: "")?.worldAngleDegrees ?: 0f)
    lFore.localAngleDegrees = ikRes.bone2LocalAngleDeg
  }

  // Solve Right Arm
  val rUpper = rig.getBone("r_upper_arm")
  val rFore = rig.getBone("r_forearm")
  if (rUpper != null && rFore != null && rFore.isIKTarget) {
    val ikRes = InverseKinematics.solve2Bone(
      x0 = rUpper.startX,
      y0 = rUpper.startY,
      targetX = rFore.ikTargetX,
      targetY = rFore.ikTargetY,
      l1 = rUpper.length,
      l2 = rFore.length,
      flipBend = true
    )
    rUpper.localAngleDegrees = ikRes.bone1AngleDeg - (rig.getBone(rUpper.parentId ?: "")?.worldAngleDegrees ?: 0f)
    rFore.localAngleDegrees = ikRes.bone2LocalAngleDeg
  }

  // Solve Left Leg
  val lThigh = rig.getBone("l_thigh")
  val lShin = rig.getBone("l_shin")
  if (lThigh != null && lShin != null && lShin.isIKTarget) {
    val ikRes = InverseKinematics.solve2Bone(
      x0 = lThigh.startX,
      y0 = lThigh.startY,
      targetX = lShin.ikTargetX,
      targetY = lShin.ikTargetY,
      l1 = lThigh.length,
      l2 = lShin.length,
      flipBend = false
    )
    lThigh.localAngleDegrees = ikRes.bone1AngleDeg - (rig.getBone(lThigh.parentId ?: "")?.worldAngleDegrees ?: 0f)
    lShin.localAngleDegrees = ikRes.bone2LocalAngleDeg
  }

  // Solve Right Leg
  val rThigh = rig.getBone("r_thigh")
  val rShin = rig.getBone("r_shin")
  if (rThigh != null && rShin != null && rShin.isIKTarget) {
    val ikRes = InverseKinematics.solve2Bone(
      x0 = rThigh.startX,
      y0 = rThigh.startY,
      targetX = rShin.ikTargetX,
      targetY = rShin.ikTargetY,
      l1 = rThigh.length,
      l2 = rShin.length,
      flipBend = true
    )
    rThigh.localAngleDegrees = ikRes.bone1AngleDeg - (rig.getBone(rThigh.parentId ?: "")?.worldAngleDegrees ?: 0f)
    rShin.localAngleDegrees = ikRes.bone2LocalAngleDeg
  }
}

private fun DrawScope.drawSkeletalRig(rig: SkeletalRig, tintColor: Color) {
  for (bone in rig.bones) {
    // Draw bone tapered segment
    val dx = bone.endX - bone.startX
    val dy = bone.endY - bone.startY
    val len = sqrt(dx * dx + dy * dy)
    if (len < 1f) continue

    val perpX = -dy / len * 9f
    val perpY = dx / len * 9f

    val bonePath = Path().apply {
      moveTo(bone.startX, bone.startY)
      lineTo(bone.startX + dx * 0.25f + perpX, bone.startY + dy * 0.25f + perpY)
      lineTo(bone.endX, bone.endY)
      lineTo(bone.startX + dx * 0.25f - perpX, bone.startY + dy * 0.25f - perpY)
      close()
    }

    drawPath(path = bonePath, color = Color(bone.color).copy(alpha = 0.35f), style = Fill)
    drawPath(path = bonePath, color = Color(bone.color), style = Stroke(width = 2f))

    // Joint circles
    drawCircle(color = Color.White, radius = 4f, center = Offset(bone.startX, bone.startY))
    drawCircle(color = Color(bone.color), radius = 5.5f, center = Offset(bone.endX, bone.endY), style = Stroke(2f))
  }
}

private fun DrawScope.drawRigInteractiveHandles(rig: SkeletalRig) {
  for (bone in rig.bones) {
    if (bone.isIKTarget) {
      val targetPos = Offset(bone.ikTargetX, bone.ikTargetY)
      // Dashed guide line from joint to IK target
      drawLine(
        color = Color(0x66FFD600),
        start = Offset(bone.endX, bone.endY),
        end = targetPos,
        strokeWidth = 1.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
      )

      // Glowing pulsing IK target ring
      drawCircle(color = AmberGold, radius = 10f, center = targetPos, style = Stroke(2.5f))
      drawCircle(color = AmberGold.copy(alpha = 0.3f), radius = 18f, center = targetPos, style = Fill)

      // Crosshair plus inside IK target
      drawLine(color = AmberGold, start = Offset(targetPos.x - 7f, targetPos.y), end = Offset(targetPos.x + 7f, targetPos.y), strokeWidth = 2f)
      drawLine(color = AmberGold, start = Offset(targetPos.x, targetPos.y - 7f), end = Offset(targetPos.x, targetPos.y + 7f), strokeWidth = 2f)
    }
  }
}

private fun DrawScope.drawPuppetMesh(puppet: MeshPuppet, tintColor: Color) {
  // Draw Triangles
  for (t in puppet.triangles) {
    if (t.i1 in puppet.vertices.indices && t.i2 in puppet.vertices.indices && t.i3 in puppet.vertices.indices) {
      val v1 = puppet.vertices[t.i1]
      val v2 = puppet.vertices[t.i2]
      val v3 = puppet.vertices[t.i3]

      val triPath = Path().apply {
        moveTo(v1.deformedX, v1.deformedY)
        lineTo(v2.deformedX, v2.deformedY)
        lineTo(v3.deformedX, v3.deformedY)
        close()
      }

      drawPath(path = triPath, color = Color(puppet.fillColor).copy(alpha = 0.45f), style = Fill)
      if (puppet.showWireframe) {
        drawPath(path = triPath, color = tintColor, style = Stroke(width = 1.2f))
      }
    }
  }

  // Draw Vertices
  if (puppet.showWireframe) {
    for (v in puppet.vertices) {
      drawCircle(color = Color.White, radius = 3.5f, center = Offset(v.deformedX, v.deformedY))
    }
  }
}

private fun DrawScope.drawGeometricShape(layer: Layer, color: Color) {
  val shape = layer.shapeType ?: ShapeType.CIRCLE
  when (shape) {
    ShapeType.CIRCLE -> {
      drawCircle(color = color, radius = 40f, center = Offset(400f, 250f))
      // Anime eyes
      drawCircle(color = Color(0xFF1E2024), radius = 5f, center = Offset(388f, 245f))
      drawCircle(color = Color(0xFF1E2024), radius = 5f, center = Offset(412f, 245f))
      drawCircle(color = Color.White, radius = 2f, center = Offset(386f, 243f))
      drawCircle(color = Color.White, radius = 2f, center = Offset(410f, 243f))
    }
    ShapeType.RECTANGLE -> {
      drawRect(color = color, topLeft = Offset(350f, 200f), size = Size(100f, 100f))
    }
    ShapeType.MANGA_BALL -> {
      // Squash & Stretch Bouncing Ball
      drawOval(color = color, topLeft = Offset(350f, 160f), size = Size(100f, 80f))
      // Highlights & Anime blush
      drawCircle(color = Color(0xFFFF8A80), radius = 6f, center = Offset(370f, 210f))
      drawCircle(color = Color(0xFFFF8A80), radius = 6f, center = Offset(430f, 210f))
      drawCircle(color = Color.White.copy(alpha = 0.6f), radius = 10f, center = Offset(380f, 180f))
    }
    ShapeType.STAR -> {
      val starPath = Path().apply {
        moveTo(400f, 200f)
        lineTo(412f, 235f)
        lineTo(448f, 235f)
        lineTo(418f, 255f)
        lineTo(430f, 290f)
        lineTo(400f, 270f)
        lineTo(370f, 290f)
        lineTo(382f, 255f)
        lineTo(352f, 235f)
        lineTo(388f, 235f)
        close()
      }
      drawPath(starPath, color = color)
    }
  }
}

/**
 * Renders anchor nodes, handles, dashed lines, and "+" crosshairs
 * exactly matching the user's uploaded screenshot (IMG_20261010_093037.jpg)!
 */
private fun DrawScope.drawVectorInteractiveControls(
  shape: VectorShape,
  selectedIndex: Int,
  layerPosX: Float,
  layerPosY: Float
) {
  val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

  shape.nodes.forEachIndexed { i, node ->
    val isSelected = i == selectedIndex
    val nodeCenter = Offset(node.x, node.y)

    // If node is selected, draw control handles with dashed line and '+' crosshair (as in screenshot!)
    if (isSelected) {
      val outHandlePos = Offset(node.outHandleX, node.outHandleY)
      val inHandlePos = Offset(node.inHandleX, node.inHandleY)

      // Dashed line to Out-Handle
      drawLine(
        color = Color(0xFF90A4AE),
        start = nodeCenter,
        end = outHandlePos,
        strokeWidth = 2f,
        pathEffect = dashEffect
      )

      // '+' Crosshair marker at Out-Handle (mirrors screenshot!)
      val crosshairSize = 8f
      drawLine(
        color = Color(0xFFB0BEC5),
        start = Offset(outHandlePos.x - crosshairSize, outHandlePos.y),
        end = Offset(outHandlePos.x + crosshairSize, outHandlePos.y),
        strokeWidth = 2f
      )
      drawLine(
        color = Color(0xFFB0BEC5),
        start = Offset(outHandlePos.x, outHandlePos.y - crosshairSize),
        end = Offset(outHandlePos.x, outHandlePos.y + crosshairSize),
        strokeWidth = 2f
      )

      // Dashed line to In-Handle
      drawLine(
        color = Color(0xFF90A4AE),
        start = nodeCenter,
        end = inHandlePos,
        strokeWidth = 2f,
        pathEffect = dashEffect
      )
      // '+' Crosshair marker at In-Handle
      drawLine(
        color = Color(0xFFB0BEC5),
        start = Offset(inHandlePos.x - crosshairSize, inHandlePos.y),
        end = Offset(inHandlePos.x + crosshairSize, inHandlePos.y),
        strokeWidth = 2f
      )
      drawLine(
        color = Color(0xFFB0BEC5),
        start = Offset(inHandlePos.x, inHandlePos.y - crosshairSize),
        end = Offset(inHandlePos.x, inHandlePos.y + crosshairSize),
        strokeWidth = 2f
      )
    }

    // Anchor Point Circle:
    // If selected: prominent golden yellow (#FFC107 / #FFD600) dot as in screenshot!
    // If not selected: light cyan/white circular dot
    if (isSelected) {
      drawCircle(
        color = AmberGold,
        radius = 9f,
        center = nodeCenter
      )
      drawCircle(
        color = Color.White.copy(alpha = 0.5f),
        radius = 12f,
        center = nodeCenter,
        style = Stroke(2f)
      )
    } else {
      drawCircle(
        color = Color(0xFFB0BEC5),
        radius = 6f,
        center = nodeCenter
      )
      drawCircle(
        color = Color(0xFF263238),
        radius = 7.5f,
        center = nodeCenter,
        style = Stroke(1.5f)
      )
    }
  }
}
