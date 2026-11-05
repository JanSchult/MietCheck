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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.Defect
import com.example.shared.domain.model.DefectStatus
import com.example.shared.presentation.componentes.DefectCard
import com.example.shared.presentation.componentes.StatusChip
import com.example.shared.presentation.defect.DefectsEffect
import com.example.shared.presentation.defect.DefectsIntent
import com.example.shared.presentation.defect.kind
import com.example.shared.presentation.defect.label
import com.example.shared.util.formatDate
import com.example.shared.viewmodels.DefectsViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@Composable
fun DefectsScreen(
    onAddDefect: () -> Unit,
    onOpenDefect: (String) -> Unit,
    viewModel: DefectsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                DefectsEffect.NavigateToForm -> onAddDefect()
                is DefectsEffect.NavigateToDetail -> onOpenDefect(effect.id)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (state.apartmentId != null) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.onIntent(DefectsIntent.AddClicked) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Mangel erfassen") }
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
                item { Text("Mängel", style = MaterialTheme.typography.headlineSmall) }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val options: List<DefectStatus?> = listOf(null) + DefectStatus.entries
                        items(options) { status ->
                            FilterChip(
                                modifier = Modifier.heightIn(min = 48.dp),
                                selected = state.filter == status,
                                onClick = { viewModel.onIntent(DefectsIntent.FilterChanged(status)) },
                                label = { Text(status?.label() ?: "Alle") }
                            )
                        }
                    }
                }

                if (state.visible.isEmpty()) {
                    item {
                        Text(
                            if (state.defects.isEmpty())
                                "Noch keine Mängel erfasst. Dokumentiere einen Mangel mit Datum, Raum und Beschreibung."
                            else "Keine Mängel mit diesem Status.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    items(state.visible, key = { it.id.value }) { defect ->
                        DefectCard(defect) { viewModel.onIntent(DefectsIntent.DefectClicked(defect.id.value)) }
                    }
                }
            }
        }
    }
}

