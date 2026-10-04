package com.videogpt.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.videogpt.app.ui.viewmodel.VideoProject

@Composable
fun LibraryScreen(
    projects: List<VideoProject>,
    onOpenProject: (VideoProject) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Text("My videos")
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(projects) { project ->
                Button(onClick = { onOpenProject(project) }, modifier = Modifier.fillMaxWidth()) {
                    Text(project.title)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}
