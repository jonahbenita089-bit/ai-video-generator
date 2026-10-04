package com.videogpt.app.ai

import java.io.File

interface AiProvider {
    suspend fun generateScript(prompt: String, durationSeconds: Int, style: String): String
    suspend fun generateImages(prompt: String, count: Int, aspectRatio: String): List<String>
    suspend fun generateVoice(script: String, voice: String, language: String): File?
    suspend fun getGenerationStatus(jobId: String): String
}
