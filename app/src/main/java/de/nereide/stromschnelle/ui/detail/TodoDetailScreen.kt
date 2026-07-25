package de.nereide.stromschnelle.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.nereide.stromschnelle.data.TodoIcon
import de.nereide.stromschnelle.ui.common.PriorityBadge
import de.nereide.stromschnelle.ui.common.PriorityDimension
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")

private fun formatMillis(millis: Long): String =
    dateFormatter.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailScreen(
    id: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: TodoDetailViewModel = viewModel(factory = TodoDetailViewModel.factory(id))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { padding ->
        val todo = uiState.todo
        if (todo == null) {
            if (uiState.loading) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = TodoIcon.fromKey(todo.iconKey).imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = todo.title, style = MaterialTheme.typography.headlineSmall)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityBadge(value = todo.importance, dimension = PriorityDimension.IMPORTANCE)
                PriorityBadge(value = todo.effort, dimension = PriorityDimension.EFFORT)
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 16.dp))

            Text(
                text = "Created: ${formatMillis(todo.createdAt)}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (todo.completedAt != null) {
                Text(
                    text = "Completed: ${formatMillis(todo.completedAt)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (todo.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = todo.description, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
