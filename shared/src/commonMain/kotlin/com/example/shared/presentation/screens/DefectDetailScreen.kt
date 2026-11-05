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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.shared.domain.model.CommunicationDirection
import com.example.shared.domain.model.Defect
import com.example.shared.domain.model.DefectStatus
import com.example.shared.presentation.componentes.AddCommunicationDialog
import com.example.shared.presentation.componentes.DefectHeader
import com.example.shared.presentation.componentes.DropdownField
import com.example.shared.presentation.componentes.StatusChip
import com.example.shared.presentation.defect.DefectDetailEffect
import com.example.shared.presentation.defect.DefectDetailIntent
import com.example.shared.presentation.defect.kind
import com.example.shared.presentation.defect.label
import com.example.shared.util.formatDate
import com.example.shared.viewmodels.DefectDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun DefectDetailScreen(
    defectId: String,
    onDeleted: () -> Unit,
    onShowError: (String) -> Unit,
    viewModel: DefectDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(defectId) { viewModel.onIntent(DefectDetailIntent.Load(defectId)) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                DefectDetailEffect.Deleted -> onDeleted()
                is DefectDetailEffect.ShowError -> onShowError(effect.message)
            }
        }
    }

    val defect = state.defect
    if (defect == null) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.isLoading) CircularProgressIndicator()
            else Text("Dieser Mangel wurde nicht gefunden.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DefectHeader(defect)

        Text("Status", style = MaterialTheme.typography.titleMedium)
        DropdownField(
            selected = defect.status,
            options = DefectStatus.entries,
            label = { it.label() },
            onSelected = { viewModel.onIntent(DefectDetailIntent.StatusSelected(it)) }
        )

        Text("Verlauf", style = MaterialTheme.typography.titleLarge)
        if (defect.communications.isEmpty()) {
            Text(
                "Noch keine Einträge. Halte hier fest, wann du den Mangel gemeldet hast " +
                        "und was geantwortet wurde.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            defect.communications.forEach { entry ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "${entry.occurredAt.formatDate()} · ${entry.direction.label()}",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(entry.summary, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        OutlinedButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) { Text("Eintrag hinzufügen") }

        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) { Text("Mangel löschen") }
    }

    if (showAddDialog) {
        AddCommunicationDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { direction, summary ->
                viewModel.onIntent(DefectDetailIntent.AddCommunication(direction, summary))
                showAddDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Mangel löschen?") },
            text = { Text("Der Mangel und sein Verlauf werden von diesem Gerät gelöscht. Das lässt sich nicht rückgängig machen.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.onIntent(DefectDetailIntent.DeleteConfirmed)
                }) { Text("Löschen") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Abbrechen") } }
        )
    }
}



