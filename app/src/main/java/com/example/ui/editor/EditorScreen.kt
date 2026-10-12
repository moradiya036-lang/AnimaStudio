package com.example.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.EasingCurve
import com.example.engine.ProjectRepository
import com.example.model.AnimationProject
import com.example.model.Bone
import com.example.model.Layer
import com.example.model.LayerType
import com.example.model.ShapeType
import com.example.model.SkeletalRig
import com.example.model.VectorNode
import com.example.model.VectorShape
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioToolbarBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun EditorScreen(
  project: AnimationProject,
  onBackToHome: () -> Unit
) {
  BackHandler {
    onBackToHome()
  }

  // Playback & Frame state
  var currentFrame by remember { mutableIntStateOf(project.currentFrame) }
  var isPlaying by remember { mutableStateOf(false) }

  // Canvas View state
  var showGrid by remember { mutableStateOf(false) }
  var showOnionSkin by remember { mutableStateOf(false) }
  var isVectorPadOpen by remember { mutableStateOf(project.getSelectedLayer()?.type == LayerType.VECTOR) }
  var selectedNodeIndex by remember { mutableIntStateOf(0) }
  var activeVectorTool by remember { mutableStateOf(VectorToolMode.SELECT_NODE) }

  // UI Panels state
  var bottomPanelTab by remember { mutableIntStateOf(0) } // 0: Timeline, 1: Properties
  var showCurveDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }
  var activeEasingCurve by remember { mutableStateOf(EasingCurve.EaseInOut) }

  // Version counter to trigger re-compositions on deep layer state mutations
  var stateVersion by remember { mutableIntStateOf(0) }

  // 60 / 30 FPS Animation Clock
  LaunchedEffect(isPlaying, project.fps) {
    if (isPlaying) {
      val frameDelay = (1000L / project.fps).coerceAtLeast(16L)
      while (isPlaying) {
        delay(frameDelay)
        currentFrame = (currentFrame + 1) % (project.durationFrames + 1)
        project.currentFrame = currentFrame
      }
    }
  }

  val activeLayer = project.getSelectedLayer()

  Scaffold(
    containerColor = StudioDarkBg,
    topBar = {
      // Editor Top Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(StudioToolbarBg)
          .border(0.5.dp, StudioBorder)
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onBackToHome,
            modifier = Modifier.size(36.dp).testTag("editor_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
          }
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(project.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${project.canvasWidth}x${project.canvasHeight} • ${project.fps}fps", color = TextDim, fontSize = 10.sp)
          }
        }

        // Top Tools: Grid, Onion Skin, Vector Pad, Export
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Toggle Grid
          IconButton(
            onClick = { showGrid = !showGrid },
            modifier = Modifier.size(34.dp)
          ) {
            Icon(Icons.Default.GridOn, contentDescription = "Grid", tint = if (showGrid) NeonMint else TextDim)
          }

          // Toggle Onion Skin
          IconButton(
            onClick = { showOnionSkin = !showOnionSkin },
            modifier = Modifier.size(34.dp)
          ) {
            Icon(Icons.Default.Animation, contentDescription = "Onion Skin", tint = if (showOnionSkin) ElectricCyan else TextDim)
          }

          // Toggle Vector Touchpad (Screenshot feature)
          IconButton(
            onClick = { isVectorPadOpen = !isVectorPadOpen },
            modifier = Modifier.size(34.dp).testTag("toggle_vector_pad_button")
          ) {
            Icon(Icons.Default.Gesture, contentDescription = "Vector Pad", tint = if (isVectorPadOpen) AmberGold else TextDim)
          }

          // Export Button
          IconButton(
            onClick = { showExportDialog = true },
            modifier = Modifier.size(34.dp).testTag("export_button")
          ) {
            Icon(Icons.Default.FileDownload, contentDescription = "Export", tint = NeonMint)
          }
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // 1. Central Animation Canvas Area
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        AnimationCanvas(
          project = project,
          selectedNodeIndex = selectedNodeIndex,
          activeVectorTool = activeVectorTool,
          showGrid = showGrid,
          showOnionSkin = showOnionSkin,
          onSelectNode = { idx ->
            selectedNodeIndex = idx
            stateVersion++
          },
          onNodeMoved = { idx, x, y ->
            val shape = activeLayer?.vectorShape
            if (shape != null && idx in shape.nodes.indices) {
              val node = shape.nodes[idx]
              val dx = x - node.x
              val dy = y - node.y
              node.moveBy(dx, dy)
              stateVersion++
              ProjectRepository.updateProject(project)
            }
          },
          onHandleMoved = { idx, isOut, hx, hy ->
            val shape = activeLayer?.vectorShape
            if (shape != null && idx in shape.nodes.indices) {
              val node = shape.nodes[idx]
              if (isOut) node.setOutHandle(hx, hy) else node.setInHandle(hx, hy)
              stateVersion++
              ProjectRepository.updateProject(project)
            }
          },
          onIKTargetMoved = { boneId, tx, ty ->
            val rig = activeLayer?.skeletalRig
            val bone = rig?.getBone(boneId)
            if (bone != null) {
              bone.ikTargetX = tx
              bone.ikTargetY = ty
              stateVersion++
              ProjectRepository.updateProject(project)
            }
          },
          modifier = Modifier.fillMaxSize()
        )

        // If Vector Pad is open and active layer is a vector, overlay the "Swipe to move point" trackpad!
        if (isVectorPadOpen && activeLayer?.type == LayerType.VECTOR) {
          VectorTouchPadOverlay(
            activeTool = activeVectorTool,
            onToolSelect = { activeVectorTool = it },
            onRemovePoint = {
              val shape = activeLayer.vectorShape
              if (shape != null && shape.nodes.size > 2 && selectedNodeIndex in shape.nodes.indices) {
                shape.nodes.removeAt(selectedNodeIndex)
                selectedNodeIndex = selectedNodeIndex.coerceAtMost(shape.nodes.size - 1)
                stateVersion++
                ProjectRepository.updateProject(project)
              }
            },
            onAddPoint = {
              val shape = activeLayer.vectorShape
              if (shape != null) {
                val last = shape.nodes.lastOrNull()
                val newX = (last?.x ?: 300f) + 80f
                val newY = (last?.y ?: 300f) + 40f
                shape.nodes.add(VectorNode(x = newX, y = newY, inHandleX = newX - 30f, inHandleY = newY - 20f, outHandleX = newX + 30f, outHandleY = newY + 20f))
                selectedNodeIndex = shape.nodes.size - 1
                stateVersion++
                ProjectRepository.updateProject(project)
              }
            },
            onCloseVectorEditor = { isVectorPadOpen = false },
            onSwipeMove = { dx, dy ->
              val shape = activeLayer.vectorShape
              if (shape != null && selectedNodeIndex in shape.nodes.indices) {
                val node = shape.nodes[selectedNodeIndex]
                if (activeVectorTool == VectorToolMode.BEZIER_HANDLE) {
                  node.setOutHandle(node.outHandleX + dx, node.outHandleY + dy)
                } else {
                  node.moveBy(dx, dy)
                }
                stateVersion++
                ProjectRepository.updateProject(project)
              }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
          )
        }
      }

      // 2. Bottom Navigation Tabs (Timeline vs Layer Properties)
      TabRow(
        selectedTabIndex = bottomPanelTab,
        containerColor = StudioToolbarBg,
        contentColor = NeonMint,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            Modifier.tabIndicatorOffset(tabPositions[bottomPanelTab]),
            color = NeonMint,
            height = 2.5.dp
          )
        },
        modifier = Modifier.height(38.dp)
      ) {
        Tab(
          selected = bottomPanelTab == 0,
          onClick = { bottomPanelTab = 0 },
          text = { Text("Timeline", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = bottomPanelTab == 1,
          onClick = { bottomPanelTab = 1 },
          text = { Text("Properties & Rig", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        )
      }

      // 3. Bottom Panel Content
      if (bottomPanelTab == 0) {
        TimelineView(
          project = project,
          isPlaying = isPlaying,
          onPlayPauseToggle = { isPlaying = !isPlaying },
          onFrameSeek = { frame ->
            currentFrame = frame
            project.currentFrame = frame
            stateVersion++
          },
          onToggleKeyframe = {
            if (activeLayer != null) {
              if (activeLayer.hasKeyframeAt(currentFrame)) {
                activeLayer.removeKeyframeAt(currentFrame)
              } else {
                // Add keyframes for current transform values
                activeLayer.posX.addOrUpdateKeyframe(currentFrame, activeLayer.posX.getValueAt(currentFrame.toFloat()), activeEasingCurve)
                activeLayer.posY.addOrUpdateKeyframe(currentFrame, activeLayer.posY.getValueAt(currentFrame.toFloat()), activeEasingCurve)
                activeLayer.rotation.addOrUpdateKeyframe(currentFrame, activeLayer.rotation.getValueAt(currentFrame.toFloat()), activeEasingCurve)
                activeLayer.scaleX.addOrUpdateKeyframe(currentFrame, activeLayer.scaleX.getValueAt(currentFrame.toFloat()), activeEasingCurve)
              }
              stateVersion++
              ProjectRepository.updateProject(project)
            }
          },
          onPrevKeyframe = {
            // Find highest keyframe < currentFrame
            val allKeys = mutableSetOf<Int>()
            activeLayer?.posX?.keyframes?.forEach { allKeys.add(it.frame) }
            activeLayer?.posY?.keyframes?.forEach { allKeys.add(it.frame) }
            activeLayer?.rotation?.keyframes?.forEach { allKeys.add(it.frame) }
            val prevKey = allKeys.filter { it < currentFrame }.maxOrNull()
            if (prevKey != null) {
              currentFrame = prevKey
              project.currentFrame = prevKey
            }
          },
          onNextKeyframe = {
            val allKeys = mutableSetOf<Int>()
            activeLayer?.posX?.keyframes?.forEach { allKeys.add(it.frame) }
            activeLayer?.posY?.keyframes?.forEach { allKeys.add(it.frame) }
            activeLayer?.rotation?.keyframes?.forEach { allKeys.add(it.frame) }
            val nextKey = allKeys.filter { it > currentFrame }.minOrNull()
            if (nextKey != null) {
              currentFrame = nextKey
              project.currentFrame = nextKey
            }
          },
          onOpenCurveEditor = { showCurveDialog = true },
          onSelectLayer = { id ->
            project.selectedLayerId = id
            isVectorPadOpen = (project.getSelectedLayer()?.type == LayerType.VECTOR)
            stateVersion++
          },
          onToggleLayerVisibility = { id ->
            val l = project.layers.firstOrNull { it.id == id }
            if (l != null) {
              l.isVisible = !l.isVisible
              stateVersion++
            }
          }
        )
      } else {
        LayerInspector(
          project = project,
          activeLayer = activeLayer,
          onAddNewLayer = { type ->
            val newLayer = when (type) {
              LayerType.VECTOR -> {
                val s = VectorShape(name = "Vector Layer ${project.layers.size + 1}")
                s.nodes.addAll(
                  listOf(
                    VectorNode(x = 250f, y = 350f, inHandleX = 200f, inHandleY = 300f, outHandleX = 300f, outHandleY = 400f),
                    VectorNode(x = 450f, y = 350f, inHandleX = 400f, inHandleY = 400f, outHandleX = 500f, outHandleY = 300f)
                  )
                )
                Layer(name = "Vector ${project.layers.size + 1}", type = LayerType.VECTOR, vectorShape = s)
              }
              LayerType.SKELETON -> {
                val r = SkeletalRig(name = "Rig ${project.layers.size + 1}", rootX = 400f, rootY = 400f)
                r.bones.add(Bone(id = "root", name = "Root", parentId = null, length = 80f, localAngleDegrees = -90f))
                r.bones.add(Bone(id = "limb", name = "Limb", parentId = "root", length = 70f, localAngleDegrees = 45f, isIKTarget = true, ikTargetX = 450f, ikTargetY = 350f))
                r.updateHierarchy()
                Layer(name = "Skeleton ${project.layers.size + 1}", type = LayerType.SKELETON, skeletalRig = r)
              }
              LayerType.SHAPE -> {
                Layer(name = "Star Shape", type = LayerType.SHAPE, shapeType = ShapeType.STAR, fillColor = 0xFFFFD600)
              }
              LayerType.PUPPET_MESH -> {
                Layer(name = "Puppet Mesh", type = LayerType.PUPPET_MESH)
              }
            }
            project.layers.add(newLayer)
            project.selectedLayerId = newLayer.id
            stateVersion++
            ProjectRepository.updateProject(project)
          },
          onDuplicateLayer = {
            if (activeLayer != null) {
              project.duplicateLayer(activeLayer.id)
              stateVersion++
              ProjectRepository.updateProject(project)
            }
          },
          onDeleteLayer = {
            if (activeLayer != null) {
              project.removeLayer(activeLayer.id)
              stateVersion++
              ProjectRepository.updateProject(project)
            }
          },
          onMoveUp = {
            if (activeLayer != null) {
              project.moveLayerUp(activeLayer.id)
              stateVersion++
            }
          },
          onMoveDown = {
            if (activeLayer != null) {
              project.moveLayerDown(activeLayer.id)
              stateVersion++
            }
          },
          onPropertyChange = {
            stateVersion++
            ProjectRepository.updateProject(project)
          }
        )
      }
    }
  }

  // Curve Graph Dialog
  if (showCurveDialog) {
    EasingCurveDialog(
      initialCurve = activeEasingCurve,
      onApplyCurve = { curve ->
        activeEasingCurve = curve
        // Update current frame keyframe easing if present
        activeLayer?.posX?.keyframes?.firstOrNull { it.frame == currentFrame }?.easing = curve
        activeLayer?.posY?.keyframes?.firstOrNull { it.frame == currentFrame }?.easing = curve
        activeLayer?.rotation?.keyframes?.firstOrNull { it.frame == currentFrame }?.easing = curve
        stateVersion++
        ProjectRepository.updateProject(project)
      },
      onDismiss = { showCurveDialog = false }
    )
  }

  // Export Dialog
  if (showExportDialog) {
    ExportDialog(
      project = project,
      onDismiss = { showExportDialog = false }
    )
  }
}
