package com.videogpt.app.ai

import javax.inject.Inject

class LocalFallbackProvider @Inject constructor() : AiProvider {
    override suspend fun generateScript(
        prompt: String,
        durationSeconds: Int,
        style: String
    ): String {
        val scenes = minOf((durationSeconds / 5), 10)
        val scenes_list = (1..scenes).map { i ->
            "Scene $i: ${prompt.take(50)} - visual moment $i"
        }
        return scenes_list.joinToString("\n\n")
    }

    override suspend fun generateImages(
        prompt: String,
        count: Int,
        aspectRatio: String
    ): List<String> = emptyList()

    override suspend fun generateVoice(
        script: String,
        voice: String,
        language: String
    ) = null

    override suspend fun getGenerationStatus(jobId: String) = "ready"
}
