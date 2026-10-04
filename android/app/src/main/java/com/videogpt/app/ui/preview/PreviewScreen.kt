package com.videogpt.app.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.videogpt.app.ui.viewmodel.VideoProject

@Composable
fun PreviewScreen(
    project: VideoProject?,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp)
    ) {
        Text("Preview")
        Spacer(Modifier.height(8.dp))
        if (project == null) {
            Text("No project selected")
        } else {
            Text(project.title)
            Text(project.prompt)
            Text("Output: ${project.outputPath}")
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}
