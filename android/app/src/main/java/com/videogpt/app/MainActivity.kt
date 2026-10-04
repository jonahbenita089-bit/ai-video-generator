package com.videogpt.app

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.videogpt.app.ui.create.CreateVideoScreen
import com.videogpt.app.ui.library.LibraryScreen
import com.videogpt.app.ui.preview.PreviewScreen
import com.videogpt.app.ui.settings.SettingsScreen
import com.videogpt.app.ui.theme.VideoAiTheme
import com.videogpt.app.ui.viewmodel.VideoProjectViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoAiTheme {
                VideoAiApp()
            }
        }
    }
}

@Composable
fun VideoAiApp(
    viewModel: VideoProjectViewModel = viewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    when (uiState.currentScreen) {
        Screen.Create -> CreateVideoScreen(
            state = uiState,
            onPromptChanged = viewModel::updatePrompt,
            onGenerate = {
                viewModel.generateVideo(context)
            },
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
}

enum class Screen {
    Create,
    Library,
    Preview,
    Settings
}
