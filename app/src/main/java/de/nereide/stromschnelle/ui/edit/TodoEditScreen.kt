package de.nereide.stromschnelle.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.nereide.stromschnelle.data.TodoIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditScreen(
    id: Long?,
    onDone: () -> Unit,
    viewModel: TodoEditViewModel = viewModel(factory = TodoEditViewModel.factory(id))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == null) "New todo" else "Edit todo") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::save) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            PriorityChooser(
                label = "Importance",
                value = uiState.importance,
                onValueChange = viewModel::onImportanceChange
            )
            Spacer(modifier = Modifier.height(12.dp))
            PriorityChooser(
                label = "Effort",
                value = uiState.effort,
                onValueChange = viewModel::onEffortChange
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Icon", style = MaterialTheme.typography.labelLarge)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.fillMaxWidth().height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TodoIcon.entries.toList()) { icon ->
                    val selected = icon == uiState.icon
                    Card(
                        onClick = { viewModel.onIconChange(icon) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = icon.imageVector,
                            contentDescription = icon.key,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = viewModel::save,
                enabled = uiState.title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

/**
 * Direct 1|2|3 selection. Rotation is the right gesture where space is scarce;
 * here there is room, and direct selection avoids paying two taps to go down.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityChooser(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            (1..3).forEach { option ->
                SegmentedButton(
                    selected = value == option,
                    onClick = { onValueChange(option) },
                    shape = SegmentedButtonDefaults.itemShape(index = option - 1, count = 3)
                ) {
                    Text(option.toString())
                }
            }
        }
    }
}
