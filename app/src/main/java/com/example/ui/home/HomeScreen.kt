package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ProjectRepository
import com.example.model.AnimationProject
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardHover
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioToolbarBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
  onOpenProject: (AnimationProject) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var showNewProjectDialog by remember { mutableStateOf(false) }

  val projects by ProjectRepository.projects.collectAsState()

  Scaffold(
    containerColor = StudioDarkBg,
    bottomBar = {
      AlightBottomNav(
        selectedTab = selectedTab,
        onSelectTab = { selectedTab = it },
        onOpenCreate = { showNewProjectDialog = true }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        0 -> HomeFeedTab(
          projects = projects,
          onOpenProject = onOpenProject,
          onCreateNew = { showNewProjectDialog = true }
        )
        1 -> TutorialsScreen(
          onOpenProjectById = { id ->
            val p = ProjectRepository.getProject(id)
            if (p != null) onOpenProject(p)
          }
        )
        2 -> ProjectsListTab(
          projects = projects,
          onOpenProject = onOpenProject,
          onCreateNew = { showNewProjectDialog = true }
        )
        3 -> RigsLibraryScreen(
          onOpenProjectById = { id ->
            val p = ProjectRepository.getProject(id)
            if (p != null) onOpenProject(p)
          }
        )
      }
    }
  }

  if (showNewProjectDialog) {
    NewProjectDialog(
      onDismiss = { showNewProjectDialog = false },
      onCreateProject = { newProj ->
        showNewProjectDialog = false
        ProjectRepository.addProject(newProj)
        onOpenProject(newProj)
      }
    )
  }
}

@Composable
private fun AlightBottomNav(
  selectedTab: Int,
  onSelectTab: (Int) -> Unit,
  onOpenCreate: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(StudioToolbarBg)
  ) {
    NavigationBar(
      containerColor = StudioToolbarBg,
      tonalElevation = 0.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      // Home
      NavigationBarItem(
        selected = selectedTab == 0,
        onClick = { onSelectTab(0) },
        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
        label = { Text("Home", fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = NeonMint,
          selectedTextColor = NeonMint,
          unselectedIconColor = TextDim,
          unselectedTextColor = TextDim,
          indicatorColor = StudioToolbarBg
        )
      )

      // Tutorials
      NavigationBarItem(
        selected = selectedTab == 1,
        onClick = { onSelectTab(1) },
        icon = { Icon(Icons.Default.School, contentDescription = "Tutorials") },
        label = { Text("Tutorials", fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = NeonMint,
          selectedTextColor = NeonMint,
          unselectedIconColor = TextDim,
          unselectedTextColor = TextDim,
          indicatorColor = StudioToolbarBg
        )
      )

      // Center (+) Create Button
      Box(
        modifier = Modifier
          .weight(1f)
          .padding(top = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        FloatingActionButton(
          onClick = onOpenCreate,
          containerColor = NeonMint,
          contentColor = Color.Black,
          shape = CircleShape,
          modifier = Modifier
            .size(50.dp)
            .testTag("home_create_fab")
        ) {
          Icon(Icons.Default.Add, contentDescription = "Create", modifier = Modifier.size(28.dp))
        }
      }

      // Projects
      NavigationBarItem(
        selected = selectedTab == 2,
        onClick = { onSelectTab(2) },
        icon = { Icon(Icons.Default.Folder, contentDescription = "Projects") },
        label = { Text("Projects", fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = NeonMint,
          selectedTextColor = NeonMint,
          unselectedIconColor = TextDim,
          unselectedTextColor = TextDim,
          indicatorColor = StudioToolbarBg
        )
      )

      // Rigs / Elements
      NavigationBarItem(
        selected = selectedTab == 3,
        onClick = { onSelectTab(3) },
        icon = { Icon(Icons.Default.Widgets, contentDescription = "Rigs") },
        label = { Text("Elements", fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = NeonMint,
          selectedTextColor = NeonMint,
          unselectedIconColor = TextDim,
          unselectedTextColor = TextDim,
          indicatorColor = StudioToolbarBg
        )
      )
    }
  }
}

@Composable
private fun HomeFeedTab(
  projects: List<AnimationProject>,
  onOpenProject: (AnimationProject) -> Unit,
  onCreateNew: () -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Bar (AnimaStudio Brand)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(NeonMint),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "AnimaStudio",
                fontWeight = FontWeight.Black,
                fontSize = 19.sp,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(NeonMint.copy(alpha = 0.2f))
                  .border(0.5.dp, NeonMint, RoundedCornerShape(4.dp))
                  .padding(horizontal = 5.dp, vertical = 2.dp)
              ) {
                Text("STUDIO", color = NeonMint, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              }
            }
            Text("Vector & Skeletal Engine", color = TextDim, fontSize = 10.sp)
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
          }
          IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = TextSecondary)
          }
          Box(
            modifier = Modifier
              .size(30.dp)
              .clip(CircleShape)
              .background(ElectricCyan.copy(alpha = 0.25f))
              .border(1.dp, ElectricCyan, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text("A", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    }

    // Hero Promo Carousel Banner (Alight Motion style)
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.linearGradient(
                colors = listOf(Color(0xFF00381B), Color(0xFF141A22))
              )
            )
            .padding(18.dp)
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(AmberGold)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text("NEW ENGINE", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text("Desktop-Grade Rigging", color = TextSecondary, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Manga Characters & 2-Bone IK",
              color = TextPrimary,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Animate Bézier vectors, skeletal limbs, and deformable PNG meshes at 60 FPS natively.",
              color = TextSecondary,
              fontSize = 11.sp,
              lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = {
                  val hero = ProjectRepository.getProject("manga-hero-rig")
                  if (hero != null) onOpenProject(hero)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open Manga Rig", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = onCreateNew,
                colors = ButtonDefaults.buttonColors(containerColor = StudioCardHover, contentColor = TextPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Project", fontSize = 11.sp)
              }
            }
          }
        }
      }
    }

    // Quick Templates Carousel
    item {
      Column {
        Text("Starter Templates", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(end = 8.dp)
        ) {
          items(projects) { proj ->
            QuickTemplateCard(proj = proj, onClick = { onOpenProject(proj) })
          }
        }
      }
    }

    // Recent Projects Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Recent Projects", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(
          "${projects.size} items",
          color = TextDim,
          fontSize = 12.sp
        )
      }
    }

    // Projects list
    items(projects) { proj ->
      ProjectListItem(
        project = proj,
        onOpen = { onOpenProject(proj) },
        onDuplicate = { ProjectRepository.duplicateProject(proj.id) },
        onDelete = { ProjectRepository.deleteProject(proj.id) }
      )
    }

    item {
      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}

@Composable
private fun QuickTemplateCard(
  proj: AnimationProject,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
    modifier = Modifier
      .width(170.dp)
      .clickable(onClick = onClick)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(80.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(proj.backgroundColor))
          .border(1.dp, StudioBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonMint, modifier = Modifier.size(24.dp))
          Text("${proj.fps}fps • ${proj.durationFrames}f", color = TextDim, fontSize = 9.sp)
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        proj.name,
        color = TextPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1
      )
      Text(
        "${proj.layers.size} Layers • ${proj.aspectRatio}",
        color = TextDim,
        fontSize = 10.sp
      )
    }
  }
}

@Composable
private fun ProjectsListTab(
  projects: List<AnimationProject>,
  onOpenProject: (AnimationProject) -> Unit,
  onCreateNew: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("All Projects", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Manage your animation assets & renders", color = TextSecondary, fontSize = 12.sp)
      }
      Button(
        onClick = onCreateNew,
        colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = Color.Black),
        shape = RoundedCornerShape(8.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      items(projects) { proj ->
        ProjectListItem(
          project = proj,
          onOpen = { onOpenProject(proj) },
          onDuplicate = { ProjectRepository.duplicateProject(proj.id) },
          onDelete = { ProjectRepository.deleteProject(proj.id) }
        )
      }
    }
  }
}

@Composable
fun ProjectListItem(
  project: AnimationProject,
  onOpen: () -> Unit,
  onDuplicate: () -> Unit,
  onDelete: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onOpen)
      .testTag("project_item_${project.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Thumbnail
      Box(
        modifier = Modifier
          .size(68.dp, 48.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(project.backgroundColor))
          .border(1.dp, StudioBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text(project.aspectRatio, color = NeonMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = project.name,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${project.layers.size} Layers • ${project.fps} FPS • ${project.durationFrames} Frames",
          color = TextDim,
          fontSize = 11.sp
        )
      }

      Box {
        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
          Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextDim)
        }
        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text("Open Project") },
            onClick = {
              showMenu = false
              onOpen()
            },
            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) }
          )
          DropdownMenuItem(
            text = { Text("Duplicate") },
            onClick = {
              showMenu = false
              onDuplicate()
            },
            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
          )
          DropdownMenuItem(
            text = { Text("Delete", color = Color.Red) },
            onClick = {
              showMenu = false
              onDelete()
            },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
          )
        }
      }
    }
  }
}
