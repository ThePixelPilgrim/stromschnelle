package de.nereide.stromschnelle.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Text(
                text = "Keep completed todos visible for",
                style = MaterialTheme.typography.titleMedium
            )
            GraceOptions.forEach { option ->
                val selected = option.millis == uiState.gracePeriodMillis
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected,
                            onClick = { viewModel.setGracePeriod(option.millis) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected, onClick = { viewModel.setGracePeriod(option.millis) })
                    Text(text = option.label, modifier = Modifier.padding(start = 8.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Widget header title",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            // The field edits a local draft rather than binding straight to the
            // DataStore-backed flow. Binding directly would round-trip every
            // keystroke through stateIn(WhileSubscribed) before it reappears on
            // screen — fast typing can drop characters — and would rebuild every
            // placed widget once per character. The draft is persisted when the
            // field loses focus, and again on dispose so navigating away with the
            // back button does not discard the edit.
            var draft by rememberSaveable { mutableStateOf<String?>(null) }
            val persisted = uiState.widgetTitle
            val commit by rememberUpdatedState {
                draft?.let { if (it != persisted) viewModel.setWidgetTitle(it) }
            }
            DisposableEffect(Unit) { onDispose { commit() } }

            OutlinedTextField(
                value = draft ?: persisted,
                onValueChange = { draft = it },
                singleLine = true,
                placeholder = { Text("No title") },
                supportingText = { Text("Leave empty to show only the sort toggle") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) commit() }
            )
        }
    }
}
