package de.nereide.stromschnelle.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.nereide.stromschnelle.data.Todo
import de.nereide.stromschnelle.data.TodoIcon
import de.nereide.stromschnelle.ui.common.PriorityBadge
import de.nereide.stromschnelle.ui.common.PriorityDimension
import de.nereide.stromschnelle.ui.common.SortModeToggle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    onOpenTodo: (Long) -> Unit,
    onAddTodo: () -> Unit,
    onOpenCompleted: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: TodoListViewModel = viewModel(factory = TodoListViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stromschnelle") },
                actions = {
                    SortModeToggle(
                        mode = uiState.sortMode,
                        onToggle = viewModel::toggleSortMode
                    )
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Completed") },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                            onClick = { menuExpanded = false; onOpenCompleted() }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            onClick = { menuExpanded = false; onOpenSettings() }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTodo) {
                Icon(Icons.Default.Add, contentDescription = "Add todo")
            }
        }
    ) { padding ->
        TodoList(
            todos = uiState.todos,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            onOpenTodo = onOpenTodo,
            onToggle = viewModel::toggleComplete,
            onCycleImportance = viewModel::cycleImportance,
            onCycleEffort = viewModel::cycleEffort
        )
    }
}

@Composable
private fun TodoList(
    todos: List<Todo>,
    modifier: Modifier = Modifier,
    onOpenTodo: (Long) -> Unit,
    onToggle: (Todo) -> Unit,
    onCycleImportance: (Long) -> Unit,
    onCycleEffort: (Long) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(todos, key = { it.id }) { todo ->
            val isCompleted = todo.completedAt != null
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenTodo(todo.id) }
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.surface
                    ),
                leadingContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PriorityBadge(
                            value = todo.importance,
                            dimension = PriorityDimension.IMPORTANCE,
                            onClick = { onCycleImportance(todo.id) },
                            dimmed = isCompleted
                        )
                        PriorityBadge(
                            value = todo.effort,
                            dimension = PriorityDimension.EFFORT,
                            onClick = { onCycleEffort(todo.id) },
                            dimmed = isCompleted
                        )
                    }
                },
                headlineContent = {
                    Text(
                        text = todo.title,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                    )
                },
                supportingContent = if (todo.description.isNotBlank()) {
                    { Text(text = todo.description) }
                } else null,
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Inert spacer between the title and the checkbox: a tap that
                        // misses the checkbox lands here and does nothing.
                        Icon(
                            imageVector = TodoIcon.fromKey(todo.iconKey).imageVector,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Checkbox(checked = isCompleted, onCheckedChange = { onToggle(todo) })
                    }
                }
            )
        }
    }
}
