package de.nereide.stromschnelle.ui.completed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.nereide.stromschnelle.ui.common.PriorityBadge
import de.nereide.stromschnelle.ui.common.PriorityDimension
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedScreen(
    onBack: () -> Unit,
    viewModel: CompletedViewModel = viewModel(factory = CompletedViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Completed") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(uiState.todos, key = { it.id }) { todo ->
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    leadingContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PriorityBadge(
                                value = todo.importance,
                                dimension = PriorityDimension.IMPORTANCE,
                                dimmed = true
                            )
                            PriorityBadge(
                                value = todo.effort,
                                dimension = PriorityDimension.EFFORT,
                                dimmed = true
                            )
                        }
                    },
                    headlineContent = {
                        Text(
                            text = todo.title,
                            textDecoration = TextDecoration.LineThrough,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = {
                        val completedAt = todo.completedAt
                        Text(
                            text = if (completedAt != null) {
                                "Completed " + dateFormatter.format(
                                    Instant.ofEpochMilli(completedAt).atZone(ZoneId.systemDefault())
                                )
                            } else ""
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { viewModel.uncomplete(todo.id) }) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Restore")
                        }
                    }
                )
            }
        }
    }
}
