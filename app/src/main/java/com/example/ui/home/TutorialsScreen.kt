package com.example.ui.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnimationProject
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class TutorialItem(
  val id: String,
  val title: String,
  val category: String,
  val level: String,
  val duration: String,
  val description: String,
  val icon: ImageVector,
  val accentColor: Color,
  val targetProjectId: String
)

@Composable
fun TutorialsScreen(
  onOpenProjectById: (String) -> Unit
) {
  val tutorials = listOf(
    TutorialItem(
      id = "tut_ik",
      title = "Manga Limbs: 2-Bone Analytical IK",
      category = "Skeletal Rigging",
      level = "Intermediate",
      duration = "4 min",
      description = "Learn how the Law of Cosines computes exact knee and elbow rotations in real time. Drag the glowing IK target to pose limbs effortlessly.",
      icon = Icons.Default.Psychology,
      accentColor = NeonMint,
      targetProjectId = "manga-hero-rig"
    ),
    TutorialItem(
      id = "tut_vector",
      title = "Alight Motion Bézier Architecture",
      category = "Vector Design",
      level = "Beginner",
      duration = "3 min",
      description = "Master anchor points and direction handles. Use the 'Swipe to move point' trackpad and left toolbar to sculpt fluid vector lines.",
      icon = Icons.Default.Gesture,
      accentColor = ElectricCyan,
      targetProjectId = "vector-fluid-morph"
    ),
    TutorialItem(
      id = "tut_mesh",
      title = "Deformable Mesh & Skinning (PNG Puppet)",
      category = "Mesh Deformation",
      level = "Advanced",
      duration = "5 min",
      description = "Discover Delaunay Triangulation and Linear Blend Skinning (LBS) to bend 2D character sprites without breaking texture proportions.",
      icon = Icons.Default.AutoGraph,
      accentColor = AmberGold,
      targetProjectId = "puppet-mesh-lbs"
    ),
    TutorialItem(
      id = "tut_easing",
      title = "Mastering Cubic Bézier Easing Graphs",
      category = "Animation Timing",
      level = "Beginner",
      duration = "2 min",
      description = "Craft lively animation with acceleration handles, overshoot, anticipation, and squash-and-stretch timing curves.",
      icon = Icons.Default.Timeline,
      accentColor = Color(0xFFFF7043),
      targetProjectId = "bouncing-ball-squash"
    )
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(StudioDarkBg)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Column {
        Text(
          text = "Animation Masterclass",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Interactive lessons to master 2D vector & skeletal rigging on Android",
          fontSize = 12.sp,
          color = TextSecondary
        )
      }
    }

    items(tutorials) { tut ->
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(tut.accentColor.copy(alpha = 0.15f))
                  .border(1.dp, tut.accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(tut.icon, contentDescription = null, tint = tut.accentColor, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(tut.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("${tut.category} • ${tut.level} • ${tut.duration}", color = TextDim, fontSize = 11.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = tut.description,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { onOpenProjectById(tut.targetProjectId) },
            colors = ButtonDefaults.buttonColors(containerColor = tut.accentColor, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(40.dp)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Open Interactive Lab", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }
  }
}
