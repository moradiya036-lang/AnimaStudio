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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.Star
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
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class RigAsset(
  val id: String,
  val name: String,
  val type: String,
  val icon: ImageVector,
  val color: Color,
  val projectId: String
)

@Composable
fun RigsLibraryScreen(
  onOpenProjectById: (String) -> Unit
) {
  val assets = listOf(
    RigAsset("r1", "Full Manga Humanoid", "Skeleton + 4x 2-Bone IK", Icons.Default.SportsGymnastics, NeonMint, "manga-hero-rig"),
    RigAsset("r2", "Anime Facial Features", "Bézier Eyes & Hair", Icons.Default.Face, AmberGold, "manga-hero-rig"),
    RigAsset("r3", "Fluid Wave Morph", "Dynamic Bézier Spline", Icons.Default.Star, ElectricCyan, "vector-fluid-morph"),
    RigAsset("r4", "Deformable Puppet", "Delaunay LBS Mesh", Icons.Default.Handshake, Color(0xFFE040FB), "puppet-mesh-lbs")
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(StudioDarkBg)
      .padding(16.dp)
  ) {
    Text(
      text = "Rigs & Presets Library",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = TextPrimary
    )
    Text(
      text = "Ready-to-animate skeletons, limbs, vectors, and character puppets",
      fontSize = 12.sp,
      color = TextSecondary
    )

    Spacer(modifier = Modifier.height(16.dp))

    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(assets) { asset ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = StudioCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(asset.color.copy(alpha = 0.15f))
                .border(1.dp, asset.color.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(asset.icon, contentDescription = null, tint = asset.color, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(asset.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(asset.type, color = TextDim, fontSize = 10.sp)

            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = { onOpenProjectById(asset.projectId) },
              colors = ButtonDefaults.buttonColors(containerColor = asset.color, contentColor = Color.Black),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().height(36.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Open Rig", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
