package com.example.shared.presentation.screens

import com.example.shared.viewmodels.intent.DocumentDetailIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.shared.domain.model.DocumentType
import com.example.shared.presentation.componentes.DropdownField
import com.example.shared.presentation.componentes.FormField
import com.example.shared.viewmodels.effects.DocumentDetailEffect
import com.example.shared.presentation.labels.label
import com.example.shared.util.formatDate
import kotlin.time.ExperimentalTime
import com.example.shared.viewmodels.DocumentDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalTime::class)
@Composable
fun DocumentDetailScreen(
    documentId: String,
    onDeleted: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: DocumentDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(documentId) { viewModel.onIntent(DocumentDetailIntent.Load(documentId)) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                DocumentDetailEffect.Deleted -> onDeleted()
                is DocumentDetailEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    val doc = state.document
    if (doc == null) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.isLoading) CircularProgressIndicator()
            else Text("Dieses Dokument wurde nicht gefunden.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FormField(
            label = "Name",
            value = state.name,
            error = state.nameError,
            onChange = { viewModel.onIntent(DocumentDetailIntent.NameChanged(it)) }
        )

        Text("Art des Dokuments", style = MaterialTheme.typography.titleMedium)
        DropdownField(
            selected = state.type,
            options = DocumentType.entries,
            label = { it.label() },
            onSelected = { viewModel.onIntent(DocumentDetailIntent.TypeSelected(it)) }
        )

        val selectedApartment = state.apartments.firstOrNull { it.id.value == state.apartmentId }
        if (state.apartments.size > 1 && selectedApartment != null) {
            Text("Wohnung", style = MaterialTheme.typography.titleMedium)
            DropdownField(
                selected = selectedApartment,
                options = state.apartments,
                label = { it.label },
                onSelected = { viewModel.onIntent(DocumentDetailIntent.ApartmentSelected(it.id.value)) }
            )
        }

        Text(
            buildString {
                append("Hinzugefügt am ${doc.importedAt.formatDate()}")
                if (doc.pageCount > 1) append(" · ${doc.pageCount} Seiten")
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = { viewModel.onIntent(DocumentDetailIntent.Save) },
            enabled = state.isDirty,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) { Text("Änderungen speichern", style = MaterialTheme.typography.titleMedium) }

        OutlinedButton(
            onClick = { viewModel.onIntent(DocumentDetailIntent.Share) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) { Text("Öffnen oder teilen") }
        Text(
            "Zum Öffnen oder Teilen wird das Dokument kurz entschlüsselt und nur für die gewählte App bereitgestellt.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) { Text("Dokument löschen") }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Dokument löschen?") },
            text = { Text("Das Dokument wird von diesem Gerät gelöscht. Das lässt sich nicht rückgängig machen.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.onIntent(DocumentDetailIntent.DeleteConfirmed)
                }) { Text("Löschen") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Abbrechen") } }
        )
    }
}