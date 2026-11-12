package com.example.shared.presentation.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.lock.AUTO_LOCK_OPTIONS_SECONDS
import com.example.shared.presentation.componentes.DropdownField
import com.example.shared.presentation.componentes.SwitchRow
import com.example.shared.presentation.settings.SettingsEffect
import com.example.shared.presentation.settings.SettingsIntent
import com.example.shared.viewmodels.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

private fun timeoutLabel(seconds: Int) = when (seconds) {
    0 -> "Sofort"
    60 -> "Nach 1 Minute"
    else -> "Nach ${seconds / 60} Minuten"
}

@Composable
fun SettingsScreen(
    onSetupPin: () -> Unit,
    onChangePin: () -> Unit,
    onRemovePin: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showEraseDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            if (effect is SettingsEffect.ShowMessage) onShowMessage(effect.message)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("App-Sperre", style = MaterialTheme.typography.titleLarge)

        if (!state.hasPin) {
            Text(
                "Ohne PIN kann jeder, der dein entsperrtes Handy benutzt, deine Dokumente sehen.",
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedButton(onClick = onSetupPin, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("PIN festlegen")
            }
        } else {
            OutlinedButton(onClick = onChangePin, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("PIN ändern")
            }
            OutlinedButton(
                onClick = { viewModel.onIntent(SettingsIntent.LockNow) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) { Text("Jetzt sperren") }

            if (state.biometricAvailable) {
                SwitchRow(
                    "Mit Fingerabdruck oder Gesicht entsperren",
                    state.biometricEnabled
                ) { viewModel.onIntent(SettingsIntent.BiometricToggled(it)) }
            }

            Text("Automatisch sperren", style = MaterialTheme.typography.titleMedium)
            DropdownField(
                selected = state.timeoutSeconds,
                options = AUTO_LOCK_OPTIONS_SECONDS,
                label = { timeoutLabel(it) },
                onSelected = { viewModel.onIntent(SettingsIntent.TimeoutSelected(it)) }
            )
            Text(
                "Gilt, wenn du die App verlässt. Beim Scannen, Auswählen oder Teilen von Dokumenten " +
                        "wird nicht gesperrt.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedButton(onClick = onRemovePin, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("App-Sperre ausschalten")
            }
        }

        Text("Bildschirm", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
        SwitchRow("Bildschirmfotos und App-Vorschau erlauben", state.screenshotsAllowed) {
            viewModel.onIntent(SettingsIntent.ScreenshotsToggled(it))
        }
        Text(
            "Standardmäßig sind Bildschirmfotos blockiert und die App-Vorschau im App-Wechsler bleibt leer.",
            style = MaterialTheme.typography.bodyMedium
        )

        Text("Deine Daten", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
        Text(
            "Alle Daten liegen verschlüsselt auf diesem Gerät. Es gibt kein Konto und keine Cloud.",
            style = MaterialTheme.typography.bodyLarge
        )
        OutlinedButton(
            onClick = { showExportDialog = true},
            enabled = !state.isExporting,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) {Text(if(state.isExporting) "Export wird ersetllt..." else " Alle Daten exportieren")}
        OutlinedButton(
            onClick = onOpenPrivacy,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) { Text("Datenschutzerklärung") }

        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Alle Daten exportieren?") },
                text = {
                    Text(
                        "Du erhältst eine ZIP-Datei mit allen Daten und Dokumenten. Die Datei ist nicht " +
                                "verschlüsselt. Teile sie nur mit Orten, denen du vertraust, und lösche sie danach."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showExportDialog = false
                        viewModel.onIntent(SettingsIntent.ExportConfirmed)
                    }) { Text("Export erstellen") }
                },
                dismissButton = { TextButton(onClick = { showExportDialog = false }) { Text("Abbrechen") } }
            )
        }
        OutlinedButton(
            onClick = { showEraseDialog = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) { Text("Alle Daten löschen") }
    }

    if (showEraseDialog) {
        AlertDialog(
            onDismissRequest = { showEraseDialog = false },
            title = { Text("Alle Daten löschen?") },
            text = {
                Text(
                    "Wohnungen, Abrechnungen, Dokumente, Mängel, Fristen und Einstellungen werden von " +
                            "diesem Gerät gelöscht. Die App wird danach beendet. Das lässt sich nicht rückgängig machen."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showEraseDialog = false
                    viewModel.onIntent(SettingsIntent.EraseAllConfirmed)
                }) { Text("Alles löschen") }
            },
            dismissButton = { TextButton(onClick = { showEraseDialog = false }) { Text("Abbrechen") } }
        )
    }
}

