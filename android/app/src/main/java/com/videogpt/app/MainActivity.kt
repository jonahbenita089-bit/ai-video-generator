package com.videogpt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.videogpt.app.ui.create.CreateVideoScreen
import com.videogpt.app.ui.generation.GenerationScreen
import com.videogpt.app.ui.library.LibraryScreen
import com.videogpt.app.ui.preview.PreviewScreen
import com.videogpt.app.ui.settings.SettingsScreen
import com.videogpt.app.ui.theme.VideoAiTheme
import com.videogpt.app.ui.viewmodel.Screen
import com.videogpt.app.ui.viewmodel.VideoProjectViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoAiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VideoAiApp()
                }
            }
        }
    }
}

@Composable
fun VideoAiApp(
    viewModel: VideoProjectViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.currentScreen) {
        Screen.Create -> CreateVideoScreen(
            state = uiState,
            onPromptChanged = viewModel::updatePrompt,
            onGenerate = { viewModel.generateVideo(context) },
            onSettings = { viewModel.navigate(Screen.Settings) },
            onOpenLibrary = { viewModel.navigate(Screen.Library) },
            onSelectTemplate = viewModel::applyTemplate
        )
        Screen.Library -> LibraryScreen(
            projects = uiState.projects,
            onOpenProject = { project ->
                viewModel.selectProject(project)
                viewModel.navigate(Screen.Preview)
            },
            onBack = { viewModel.navigate(Screen.Create) }
        )
        Screen.Preview -> PreviewScreen(
            project = uiState.selectedProject,
            onBack = { viewModel.navigate(Screen.Library) }
        )
        Screen.Settings -> SettingsScreen(
            settings = uiState.settings,
            onBack = { viewModel.navigate(Screen.Create) },
            onUpdate = viewModel::updateSettings
        )
    }

    if (uiState.generationState.isGenerating) {
        GenerationScreen(state = uiState.generationState)
    }
}

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
