package com.videogpt.app.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("videogpt_settings")

class SettingsManager(private val context: Context) {
    companion object {
        private val API_KEY = stringPreferencesKey("api_key")
        private val PROVIDER = stringPreferencesKey("provider")
        private val DEFAULT_VOICE = stringPreferencesKey("default_voice")
        private val DEFAULT_LANGUAGE = stringPreferencesKey("default_language")
        private val DEFAULT_DURATION = stringPreferencesKey("default_duration")
        private val DEFAULT_ASPECT_RATIO = stringPreferencesKey("default_aspect_ratio")
    }

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { it[API_KEY] ?: "" }
    val providerFlow: Flow<String> = context.dataStore.data.map { it[PROVIDER] ?: "local" }
    val voiceFlow: Flow<String> = context.dataStore.data.map { it[DEFAULT_VOICE] ?: "en-US" }
    val languageFlow: Flow<String> = context.dataStore.data.map { it[DEFAULT_LANGUAGE] ?: "en" }
    val durationFlow: Flow<Int> = context.dataStore.data.map { (it[DEFAULT_DURATION] ?: "30").toIntOrNull() ?: 30 }
    val aspectRatioFlow: Flow<String> = context.dataStore.data.map { it[DEFAULT_ASPECT_RATIO] ?: "16:9" }

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { it[API_KEY] = key }
    }

    suspend fun setProvider(provider: String) {
        context.dataStore.edit { it[PROVIDER] = provider }
    }

    suspend fun setVoice(voice: String) {
        context.dataStore.edit { it[DEFAULT_VOICE] = voice }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[DEFAULT_LANGUAGE] = language }
    }

    suspend fun setDuration(duration: Int) {
        context.dataStore.edit { it[DEFAULT_DURATION] = duration.toString() }
    }

    suspend fun setAspectRatio(ratio: String) {
        context.dataStore.edit { it[DEFAULT_ASPECT_RATIO] = ratio }
    }
}
