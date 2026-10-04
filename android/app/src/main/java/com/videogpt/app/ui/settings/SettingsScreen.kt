package com.videogpt.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.videogpt.app.ui.viewmodel.VideoSettings

@Composable
fun SettingsScreen(
    settings: VideoSettings,
    onBack: () -> Unit,
    onUpdate: (VideoSettings) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("AI provider settings")
        OutlinedTextField(value = settings.provider, onValueChange = { onUpdate(settings.copy(provider = it)) }, label = { Text("Provider") })
        OutlinedTextField(value = settings.apiKey, onValueChange = { onUpdate(settings.copy(apiKey = it)) }, label = { Text("User API key") })
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = settings.voice, onValueChange = { onUpdate(settings.copy(voice = it)) }, label = { Text("Voice") })
        OutlinedTextField(value = settings.language, onValueChange = { onUpdate(settings.copy(language = it)) }, label = { Text("Language") })
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}
