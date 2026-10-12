package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.model.AnimationProject
import com.example.model.Layer
import com.example.model.LayerType
import com.example.model.VectorNode
import com.example.model.VectorShape
import com.example.ui.theme.NeonMint
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardHover
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NewProjectDialog(
  onDismiss: () -> Unit,
  onCreateProject: (AnimationProject) -> Unit
) {
  var projectName by remember { mutableStateOf("New Animation") }
  var selectedAspect by remember { mutableStateOf("16:9") }
  var selectedResolution by remember { mutableStateOf("1080p") }
  var selectedFps by remember { mutableIntStateOf(60) }
  var selectedBgColor by remember { mutableStateOf(0xFF14161B) }

  val aspectRatios = listOf(
    Triple("16:9", 1920, 1080),
    Triple("9:16", 1080, 1920),
    Triple("1:1", 1080, 1080),
    Triple("4:3", 1440, 1080),
    Triple("4:5", 1080, 1350)
  )

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = StudioCardBg,
      border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("new_project_dialog")
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Title Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Create Project",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Project Name Input
        OutlinedTextField(
          value = projectName,
          onValueChange = { projectName = it },
          label = { Text("Project Name", color = TextSecondary) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonMint,
            unfocusedBorderColor = StudioBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = StudioDarkBg,
            unfocusedContainerColor = StudioDarkBg
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_name_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Aspect Ratio Selector
        Text(
          text = "Aspect Ratio",
          color = TextSecondary,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          aspectRatios.forEach { (aspect, _, _) ->
            val isSelected = selectedAspect == aspect
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) NeonMint.copy(alpha = 0.15f) else StudioDarkBg)
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) NeonMint else StudioBorder,
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable { selectedAspect = aspect }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Mini aspect ratio frame preview
                val frameW = if (aspect == "16:9") 22.dp else if (aspect == "9:16") 12.dp else 16.dp
                val frameH = if (aspect == "9:16" || aspect == "4:5") 22.dp else 16.dp
                Box(
                  modifier = Modifier
                    .size(frameW, frameH)
                    .border(
                      1.5.dp,
                      if (isSelected) NeonMint else TextDim,
                      RoundedCornerShape(2.dp)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = aspect,
                  color = if (isSelected) NeonMint else TextPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resolution & FPS Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Resolution
          Column(modifier = Modifier.weight(1f)) {
            Text("Resolution", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf("720p", "1080p").forEach { res ->
                val isSel = selectedResolution == res
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSel) NeonMint.copy(alpha = 0.2f) else StudioDarkBg)
                    .border(1.dp, if (isSel) NeonMint else StudioBorder, RoundedCornerShape(8.dp))
                    .clickable { selectedResolution = res }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(res, color = if (isSel) NeonMint else TextPrimary, fontSize = 12.sp)
                }
              }
            }
          }

          // Frame Rate
          Column(modifier = Modifier.weight(1f)) {
            Text("Frame Rate", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf(24, 30, 60).forEach { fps ->
                val isSel = selectedFps == fps
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSel) NeonMint.copy(alpha = 0.2f) else StudioDarkBg)
                    .border(1.dp, if (isSel) NeonMint else StudioBorder, RoundedCornerShape(8.dp))
                    .clickable { selectedFps = fps }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text("${fps}fps", color = if (isSel) NeonMint else TextPrimary, fontSize = 11.sp)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Background Color Selector
        Text(
          text = "Background Color",
          color = TextSecondary,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          val colors = listOf(
            0xFF14161BL to "Slate",
            0xFF000000L to "Black",
            0xFFFFFFFFL to "White",
            0xFF263238L to "Dark Blue",
            0xFF2E7D32L to "Green Screen"
          )
          colors.forEach { (c, label) ->
            val isSel = selectedBgColor == c
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .clickable { selectedBgColor = c }
                .padding(2.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(Color(c))
                  .border(
                    width = if (isSel) 2.5.dp else 1.dp,
                    color = if (isSel) NeonMint else StudioBorder,
                    shape = CircleShape
                  )
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(label, color = if (isSel) NeonMint else TextDim, fontSize = 9.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Create Project Action Button
        Button(
          onClick = {
            val aspectData = aspectRatios.firstOrNull { it.first == selectedAspect }
            val (width, height) = if (aspectData != null) Pair(aspectData.second, aspectData.third) else Pair(1920, 1080)

            // Create initial vector layer so user can start animating right away
            val defaultPath = VectorShape(name = "Vector Layer 1", isClosed = false, strokeColor = 0xFF00E5FF)
            defaultPath.nodes.addAll(
              listOf(
                VectorNode(x = 250f, y = 450f, inHandleX = 200f, inHandleY = 380f, outHandleX = 300f, outHandleY = 520f),
                VectorNode(x = 450f, y = 300f, inHandleX = 400f, inHandleY = 220f, outHandleX = 500f, outHandleY = 380f),
                VectorNode(x = 650f, y = 450f, inHandleX = 600f, inHandleY = 380f, outHandleX = 700f, outHandleY = 520f)
              )
            )
            val initialLayer = Layer(
              name = "Vector Path 1",
              type = LayerType.VECTOR,
              vectorShape = defaultPath
            )

            val proj = AnimationProject(
              name = projectName.ifBlank { "New Animation" },
              aspectRatio = selectedAspect,
              canvasWidth = width,
              canvasHeight = height,
              fps = selectedFps,
              durationFrames = 120,
              backgroundColor = selectedBgColor,
              layers = mutableListOf(initialLayer),
              selectedLayerId = initialLayer.id
            )
            onCreateProject(proj)
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("create_project_confirm_button")
        ) {
          Text("CREATE PROJECT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
      }
    }
  }
}
