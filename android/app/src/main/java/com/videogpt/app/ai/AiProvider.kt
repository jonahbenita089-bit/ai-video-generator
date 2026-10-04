package com.videogpt.app.ai

import android.content.Context
import com.videogpt.app.ui.viewmodel.VideoSettings
import java.io.File

interface AiProvider {
    suspend fun generateScript(prompt: String, durationSeconds: Int, style: String): String
    suspend fun generateImages(prompt: String, count: Int): List<String>
    suspend fun generateVoice(script: String, voice: String, language: String): File?
    suspend fun generateVideo(prompt: String, durationSeconds: Int, style: String): File
    suspend fun getGenerationStatus(jobId: String): String
}

class LocalFallbackProvider(
    private val settings: VideoSettings,
) : AiProvider {
    override suspend fun generateScript(prompt: String, durationSeconds: Int, style: String): String {
        val sentence = prompt.trim().ifEmpty { "A cinematic short video" }
        return "Scene 1: Open on a compelling visual of $sentence.\n" +
            "Scene 2: Introduce the idea with a clean, modern visual.\n" +
            "Scene 3: Highlight the key benefit with crisp text overlays.\n" +
            "Scene 4: End with a strong final message and call to action."
    }

    override suspend fun generateImages(prompt: String, count: Int): List<String> = emptyList()

    override suspend fun generateVoice(script: String, voice: String, language: String): File? = null

    override suspend fun generateVideo(prompt: String, durationSeconds: Int, style: String): File {
        val tmp = File.createTempFile("fallback-video", ".txt")
        tmp.writeText(prompt)
        return tmp
    }

    override suspend fun getGenerationStatus(jobId: String): String = "ready"
}

class OpenAICompatibleProvider(
    private val apiKey: String,
    private val settings: VideoSettings,
) : AiProvider {
    override suspend fun generateScript(prompt: String, durationSeconds: Int, style: String): String {
        return "OpenAI-compatible provider selected. User must provide a valid API key and internet access."
    }

    override suspend fun generateImages(prompt: String, count: Int): List<String> = emptyList()

    override suspend fun generateVoice(script: String, voice: String, language: String): File? = null

    override suspend fun generateVideo(prompt: String, durationSeconds: Int, style: String): File {
        throw UnsupportedOperationException("OpenAI-compatible provider requires remote cloud generation and is optional. Use local fallback until external provider is configured.")
    }

    override suspend fun getGenerationStatus(jobId: String): String = "ready"
}
