package com.example.shared.presentation.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.example.shared.presentation.componentes.PinDots
import com.example.shared.presentation.componentes.PinPad
import com.example.shared.viewmodels.intent.LockIntent
import com.example.shared.viewmodels.LockViewModel
import org.koin.compose.viewmodel.koinViewModel

internal fun formatLockout(seconds: Long): String = when {
    seconds == 1L -> "1 Sekunde"
    seconds < 60 -> "$seconds Sekunden"
    else -> "${(seconds + 59) / 60} Minuten"
}

@Composable
fun LockScreen(viewModel: LockViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    var showForgotDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.onIntent(LockIntent.Entered) }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("MietCheck ist gesperrt", style = MaterialTheme.typography.headlineSmall)
            Text("Gib deine PIN ein.", style = MaterialTheme.typography.bodyLarge)
            PinDots(state.enteredDigits)

            when {
                state.lockoutSeconds > 0 -> Text(
                    "Zu viele Versuche. Bitte warte noch ${formatLockout(state.lockoutSeconds)}.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                state.error != null -> Text(
                    state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            PinPad(
                enabled = !state.isChecking && state.lockoutSeconds == 0L,
                onDigit = { viewModel.onIntent(LockIntent.Digit(it)) },
                onBackspace = { viewModel.onIntent(LockIntent.Backspace) }
            )

            if (state.biometricAvailable) {
                OutlinedButton(
                    onClick = { viewModel.onIntent(LockIntent.BiometricRequested) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) { Text("Mit Fingerabdruck oder Gesicht entsperren") }
            }

            TextButton(onClick = { showForgotDialog = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("PIN vergessen?")
            }
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("PIN vergessen?") },
            text = {
                Text(
                    "Zum Schutz deiner Daten gibt es keine Wiederherstellung. Du kannst nur alle Daten " +
                            "dieser App von diesem Gerät löschen und neu beginnen. Dokumente, Abrechnungen " +
                            "und Mängel gehen dabei verloren."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showForgotDialog = false
                    viewModel.onIntent(LockIntent.EraseAllConfirmed)
                }) { Text("Alle Daten löschen") }
            },
            dismissButton = { TextButton(onClick = { showForgotDialog = false }) { Text("Abbrechen") } }
        )
    }
}