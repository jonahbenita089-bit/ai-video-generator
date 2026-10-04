package com.videogpt.app.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videogpt.app.ai.AiProvider
import com.videogpt.app.data.AppDatabase
import com.videogpt.app.data.ProjectEntity
import com.videogpt.app.engine.VideoCompositionEngine
import com.videogpt.app.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class GenerationState(
    val isGenerating: Boolean = false,
    val progress: Int = 0,
    val currentStep: String = "",
    val errorMessage: String? = null
)

data class VideoSettings(
    val provider: String = "local",
    val apiKey: String = "",
    val voice: String = "en-US",
    val language: String = "en",
    val durationSec: Int = 30,
    val aspectRatio: String = "16:9"
)

data class VideoProject(
    val id: String,
    val title: String,
    val prompt: String,
    val outputPath: String,
    val createdAt: Long
)

enum class Screen {
    Create, Library, Preview, Settings
}

data class VideoUiState(
    val prompt: String = "",
    val currentScreen: Screen = Screen.Create,
    val generationState: GenerationState = GenerationState(),
    val settings: VideoSettings = VideoSettings(),
    val projects: List<VideoProject> = emptyList(),
    val selectedProject: VideoProject? = null
)

@HiltViewModel
class VideoProjectViewModel @Inject constructor(
    private val db: AppDatabase,
    private val settingsManager: SettingsManager,
    private val videoEngine: VideoCompositionEngine,
    private val aiProvider: AiProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val apiKey = settingsManager.apiKeyFlow.first()
            val provider = settingsManager.providerFlow.first()
            val voice = settingsManager.voiceFlow.first()
            val language = settingsManager.languageFlow.first()
            val duration = settingsManager.durationFlow.first()
            val aspectRatio = settingsManager.aspectRatioFlow.first()

            _uiState.update {
                it.copy(
                    settings = VideoSettings(
                        provider = provider,
                        apiKey = apiKey,
                        voice = voice,
                        language = language,
                        durationSec = duration,
                        aspectRatio = aspectRatio
                    )
                )
            }
        }
    }

    private fun loadProjects() {
        viewModelScope.launch {
            db.projectDao().getAllProjects().collect { projects ->
                _uiState.update {
                    it.copy(
                        projects = projects.map { entity ->
                            VideoProject(
                                id = entity.id,
                                title = entity.title,
                                prompt = entity.prompt,
                                outputPath = entity.outputPath,
                                createdAt = entity.createdAt
                            )
                        }
                    )
                }
            }
        }
    }

    fun updatePrompt(prompt: String) {
        _uiState.update { it.copy(prompt = prompt) }
    }

    fun updateSettings(settings: VideoSettings) {
        _uiState.update { it.copy(settings = settings) }
        viewModelScope.launch {
            settingsManager.setApiKey(settings.apiKey)
            settingsManager.setProvider(settings.provider)
            settingsManager.setVoice(settings.voice)
            settingsManager.setLanguage(settings.language)
            settingsManager.setDuration(settings.durationSec)
            settingsManager.setAspectRatio(settings.aspectRatio)
        }
    }

    fun navigate(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun selectProject(project: VideoProject) {
        _uiState.update { it.copy(selectedProject = project) }
    }

    fun applyTemplate(prompt: String) {
        _uiState.update { it.copy(prompt = prompt) }
    }

    fun generateVideo(context: Context) {
        val prompt = _uiState.value.prompt.trim()
        if (prompt.isEmpty()) {
            _uiState.update {
                it.copy(
                    generationState = GenerationState(
                        errorMessage = "Please enter a prompt first."
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        generationState = GenerationState(
                            isGenerating = true,
                            currentStep = "Creating script...",
                            progress = 0
                        )
                    )
                }

                val settings = _uiState.value.settings
                val script = aiProvider.generateScript(prompt, settings.durationSec, "cinematic")
                Log.d("VideoVM", "Script: $script")

                _uiState.update {
                    it.copy(
                        generationState = GenerationState(
                            isGenerating = true,
                            currentStep = "Preparing scenes...",
                            progress = 20
                        )
                    )
                }

                val scenes = script.split("\n\n").filter { it.isNotBlank() }
                Log.d("VideoVM", "Scenes count: ${scenes.size}")

                _uiState.update {
                    it.copy(
                        generationState = GenerationState(
                            isGenerating = true,
                            currentStep = "Rendering video...",
                            progress = 50
                        )
                    )
                }

                val outputDir = File(context.getExternalFilesDir(null), "videos")
                outputDir.mkdirs()
                val outputFile = File(outputDir, "${UUID.randomUUID()}.mp4")

                val success = videoEngine.createVideoFromScenes(
                    scenes = scenes,
                    outputFile = outputFile,
                    durationSeconds = settings.durationSec
                ) { frameProgress ->
                    _uiState.update {
                        it.copy(
                            generationState = GenerationState(
                                isGenerating = true,
                                currentStep = "Rendering video...",
                                progress = 50 + (frameProgress / 2)
                            )
                        )
                    }
                }

                if (!success || !outputFile.exists()) {
                    throw Exception("Video rendering failed")
                }

                _uiState.update {
                    it.copy(
                        generationState = GenerationState(
                            isGenerating = true,
                            currentStep = "Exporting...",
                            progress = 95
                        )
                    )
                }

                val project = ProjectEntity(
                    id = UUID.randomUUID().toString(),
                    title = prompt.take(50),
                    prompt = prompt,
                    outputPath = outputFile.absolutePath,
                    createdAt = System.currentTimeMillis(),
                    status = "completed",
                    durationSec = settings.durationSec
                )

                db.projectDao().insertProject(project)

                _uiState.update {
                    it.copy(
                        generationState = GenerationState(
                            isGenerating = false,
                            progress = 100,
                            currentStep = "Complete!"
                        ),
                        selectedProject = VideoProject(
                            id = project.id,
                            title = project.title,
                            prompt = project.prompt,
                            outputPath = project.outputPath,
                            createdAt = project.createdAt
                        ),
                        currentScreen = Screen.Preview
                    )
                }
            } catch (e: Exception) {
                Log.e("VideoVM", "Generation failed", e)
                _uiState.update {
                    it.copy(
                        generationState = GenerationState(
                            isGenerating = false,
                            errorMessage = "Video generation failed: ${e.message ?: "unknown error"}"
                        )
                    )
                }
            }
        }
    }
}
