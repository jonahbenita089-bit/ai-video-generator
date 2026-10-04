package com.videogpt.app.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.videogpt.app.ui.viewmodel.VideoUiState

@Composable
fun CreateVideoScreen(
    state: VideoUiState,
    onPromptChanged: (String) -> Unit,
    onGenerate: () -> Unit,
    onSettings: () -> Unit,
    onOpenLibrary: () -> Unit,
    onSelectTemplate: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("VideoGPT", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            TextButton(onClick = onOpenLibrary) { Text("Library") }
        }

        Spacer(Modifier.height(16.dp))

        Text("Create video", fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.prompt,
            onValueChange = onPromptChanged,
            modifier = Modifier.fillMaxWidth().height(180.dp),
            placeholder = { Text("describe the video you want to create...") },
            shape = RoundedCornerShape(20.dp)
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AssistChip(onClick = { /* no-op */ }, label = { Text("${state.settings.durationSec}s") })
            AssistChip(onClick = onSettings, label = { Text("Settings") })
            AssistChip(onClick = { onSelectTemplate("Create a 30 second futuristic city video.") }, label = { Text("City") })
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = state.prompt.isNotBlank() && !state.isGenerating,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (state.isGenerating) "Creating..." else "Generate video")
        }

        if (state.errorMessage != null) {
            Spacer(Modifier.height(10.dp))
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        Text("Popular ideas", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TemplateChip("Futuristic city") { onSelectTemplate("Create a 30 second futuristic city video.") }
            TemplateChip("Nature reel") { onSelectTemplate("Create a 20 second nature travel reel with cinematic transitions.") }
            TemplateChip("Product ad") { onSelectTemplate("Create a 15 second product showcase in a premium style.") }
        }
    }
}

@Composable
private fun TemplateChip(label: String, onClick: () -> Unit) {
    AssistChip(onClick = onClick, label = { Text(label) })
}
