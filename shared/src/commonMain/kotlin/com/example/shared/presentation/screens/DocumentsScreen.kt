package com.example.shared.presentation.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.shared.domain.model.DocumentRecord
import com.example.shared.domain.model.DocumentType
import com.example.shared.presentation.componentes.DocumentCard
import com.example.shared.presentation.componentes.DropdownField
import com.example.shared.presentation.componentes.rememberDocumentImporter
import com.example.shared.presentation.document.label
import com.example.shared.util.formatDate
import com.example.shared.viewmodels.DocumentsViewModel
import com.example.shared.viewmodels.state.DocumentsEffect
import com.example.shared.viewmodels.state.DocumentsIntent
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@Composable
fun DocumentsScreen(
    onOpenDocument: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: DocumentsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var importType by remember { mutableStateOf(DocumentType.UTILITY_BILL) }

    val importer = rememberDocumentImporter(
        onImported = { file -> viewModel.onIntent(DocumentsIntent.Import(file, importType)) },
        onError = onShowMessage
    )

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DocumentsEffect.NavigateToDetail -> onOpenDocument(effect.id)
                is DocumentsEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (state.apartmentId != null) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Dokument hinzufügen") }
                )
            }
        }
    ) { padding ->
        when {
            state.isLoading -> Column(
                Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }

            state.apartmentId == null -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { Text("Lege zuerst auf der Startseite eine Wohnung an.", style = MaterialTheme.typography.bodyLarge) }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text("Dokumente", style = MaterialTheme.typography.headlineSmall) }

                if (state.isImporting) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                            Text("Dokument wird verschlüsselt gespeichert …", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val options: List<DocumentType?> = listOf(null) + DocumentType.entries
                        items(options) { type ->
                            FilterChip(
                                modifier = Modifier.heightIn(min = 48.dp),
                                selected = state.filter == type,
                                onClick = { viewModel.onIntent(DocumentsIntent.FilterChanged(type)) },
                                label = { Text(type?.label() ?: "Alle") }
                            )
                        }
                    }
                }

                if (state.visible.isEmpty()) {
                    item {
                        Text(
                            if (state.documents.isEmpty())
                                "Noch keine Dokumente. Füge Mietvertrag, Abrechnungen oder Fotos hinzu. " +
                                        "Alles bleibt verschlüsselt auf diesem Gerät."
                            else "Keine Dokumente in dieser Kategorie.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    items(state.visible, key = { it.id.value }) { doc ->
                        DocumentCard(doc) { viewModel.onIntent(DocumentsIntent.DocumentClicked(doc.id.value)) }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Dokument hinzufügen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Um welche Art von Dokument handelt es sich?", style = MaterialTheme.typography.bodyLarge)
                    DropdownField(
                        selected = importType,
                        options = DocumentType.entries,
                        label = { it.label() },
                        onSelected = { importType = it }
                    )
                    Button(
                        onClick = { showAddDialog = false; importer.pickFile() },
                        enabled = importer.canPick,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) { Text("Datei auswählen") }
                    OutlinedButton(
                        onClick = { showAddDialog = false; importer.scan() },
                        enabled = importer.canScan,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) { Text("Mit Kamera scannen") }
                    Text("Unterstützt: PDF, JPEG, PNG bis 25 MB.", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Abbrechen") } }
        )
    }
}

