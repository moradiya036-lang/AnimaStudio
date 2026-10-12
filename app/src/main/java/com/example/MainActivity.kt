package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.model.AnimationProject
import com.example.ui.editor.EditorScreen
import com.example.ui.home.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioDarkBg

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = StudioDarkBg
        ) {
          AnimaStudioApp()
        }
      }
    }
  }
}

@Composable
fun AnimaStudioApp() {
  var activeProject by remember { mutableStateOf<AnimationProject?>(null) }

  if (activeProject != null) {
    EditorScreen(
      project = activeProject!!,
      onBackToHome = {
        activeProject = null
      }
    )
  } else {
    HomeScreen(
      onOpenProject = { project ->
        activeProject = project
      }
    )
  }
}
