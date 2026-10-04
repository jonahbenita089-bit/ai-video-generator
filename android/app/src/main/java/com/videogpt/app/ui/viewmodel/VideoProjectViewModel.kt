package com.videogpt.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videogpt.app.ai.AiProvider
import com.videogpt.app.ai.LocalFallbackProvider
import com.videogpt.app.ai.OpenAICompatibleProvider
import com.videogpt.app.engine.LocalVideoEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class VideoSettings(
    val provider: String = "OpenAI-compatible",
    val apiKey: String = "",
    val voice: String = "en-US-JennyNeural",
    val language: String = "en",
    val durationSec: Int = 30,
    val aspectRatio: String = "16:9",
    val style: String = "cinematic",
    val backgroundMusic: Boolean = true,
    val captions: Boolean = true,
    val quality: String = "HD",
)
{
    fun copy(
        provider: String = this.provider,
        apiKey: String = this.apiKey,
        voice: String = this.voice,
        language: String = this.language,
        durationSec: Int = this.durationSec,
        aspectRatio: String = this.aspectRatio,
        style: String = this.style,
        backgroundMusic: Boolean = this.backgroundMusic,
        captions: Boolean = this.captions,
        quality: String = this.quality,
    ) = VideoSettings(provider, apiKey, voice, language, durationSec, aspectRatio, style, backgroundMusic, captions, quality)
}

data class VideoProject(
    val id: String,
    val title: String,
    val prompt: String,
    val outputPath: String,
    val createdAt: Long,
)

data class VideoUiState(
    val prompt: String = "",
    val currentScreen: Screen = Screen.Create,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
    val settings: VideoSettings = VideoSettings(),
    val projects: List<VideoProject> = emptyList(),
    val selectedProject: VideoProject? = null,
)

class VideoProjectViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = _uiState

    fun updatePrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(prompt = prompt)
    }

    fun updateSettings(settings: VideoSettings) {
        _uiState.value = _uiState.value.copy(settings = settings)
    }

    fun applyTemplate(prompt: String) {
        _uiState.value = _uiState.value.copy(prompt = prompt)
    }

    fun navigate(screen: Screen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun selectProject(project: VideoProject) {
        _uiState.value = _uiState.value.copy(selectedProject = project)
    }

    fun generateVideo(context: Context) {
        val prompt = _uiState.value.prompt.trim()
        if (prompt.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a prompt first.")
            return
        }

        _uiState.value = _uiState.value.copy(isGenerating = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val settings = _uiState.value.settings
                val rootDir = File(context.getExternalFilesDir(null), "videos")
                rootDir.mkdirs()
                val provider: AiProvider = if (settings.apiKey.isNotBlank()) {
                    OpenAICompatibleProvider(settings.apiKey, settings)
                } else {
                    LocalFallbackProvider(settings)
                }
                val result = provider.generateVideo(prompt, settings.durationSec, settings.style)
                val output = File(rootDir, "${UUID.randomUUID()}.mp4")
                if (!result.exists()) {
                    val engine = LocalVideoEngine(context)
                    engine.createLocalMp4(prompt, output)
                } else {
                    result.copyTo(output, overwrite = true)
                }

                val project = VideoProject(
                    id = UUID.randomUUID().toString(),
                    title = prompt.take(32).ifBlank { "Untitled" },
                    prompt = prompt,
                    outputPath = output.absolutePath,
                    createdAt = System.currentTimeMillis()
                )

                val currentProjects = _uiState.value.projects.toMutableList()
                currentProjects.add(0, project)
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    projects = currentProjects,
                    selectedProject = project,
                    currentScreen = Screen.Preview,
                    errorMessage = null
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    errorMessage = "Video generation failed: ${t.message ?: "unknown error"}"
                )
            }
        }
    }
}
