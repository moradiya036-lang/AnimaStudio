package com.example.ui.editor

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun ExportDialog(
  project: AnimationProject,
  onDismiss: () -> Unit
) {
  var selectedFormat by remember { mutableStateOf("MP4 Video") }
  var isExporting by remember { mutableStateOf(false) }
  var exportProgress by remember { mutableFloatStateOf(0f) }
  var isCompleted by remember { mutableStateOf(false) }

  LaunchedEffect(isExporting) {
    if (isExporting) {
      exportProgress = 0f
      while (exportProgress < 1f) {
        delay(120)
        exportProgress += 0.12f
      }
      exportProgress = 1f
      isCompleted = true
      isExporting = false
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = StudioCardBg,
      border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("export_dialog")
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
          Column {
            Text("Export & Share", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("${project.name} • ${project.fps} FPS", color = TextDim, fontSize = 11.sp)
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isExporting && !isCompleted) {
          // Format selection
          Text("Export Format", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))
          Spacer(modifier = Modifier.height(8.dp))

          val formats = listOf(
            Triple("MP4 Video", "H.264 High Profile", Icons.Default.Movie),
            Triple("Animated GIF", "Looping preview", Icons.Default.Gif),
            Triple("PNG Sequence", "Lossless transparency", Icons.Default.PhotoLibrary)
          )

          formats.forEach { (fmt, sub, icon) ->
            val isSel = selectedFormat == fmt
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSel) NeonMint.copy(alpha = 0.15f) else StudioDarkBg)
                .border(1.dp, if (isSel) NeonMint else StudioBorder, RoundedCornerShape(10.dp))
                .clickable { selectedFormat = fmt }
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(icon, contentDescription = null, tint = if (isSel) NeonMint else TextDim, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(fmt, color = if (isSel) NeonMint else TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(sub, color = TextDim, fontSize = 10.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          Button(
            onClick = { isExporting = true },
            colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp)
          ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("EXPORT NOW", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        } else if (isExporting) {
          // Progress
          Column(
            modifier = Modifier.padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("Rendering frames...", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              "Processing frame ${(exportProgress * project.durationFrames).toInt()} of ${project.durationFrames}",
              color = TextDim,
              fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
              progress = { exportProgress },
              modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
              color = NeonMint,
              trackColor = StudioDarkBg
            )
          }
        } else {
          // Completed
          Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonMint, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("Export Succeeded!", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Saved to Movies/AnimaStudio/${project.name}.mp4", color = TextDim, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(18.dp))
            Button(
              onClick = onDismiss,
              colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
              Text("DONE", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
