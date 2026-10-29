package com.example.shared.presentation.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.repository.ReminderListEffect
import com.example.shared.domain.repository.ReminderListIntent
import com.example.shared.presentation.componentes.ReminderRow
import com.example.shared.presentation.componentes.SectionTitle
import com.example.shared.viewmodels.ReminderListViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun ReminderListScreen(
    apartmentId: String,
    onNavigateToAddReminder: () -> Unit,
    onShowError: (String) -> Unit,
    viewModel: ReminderListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val now = remember { Clock.System.now() }

    LaunchedEffect(apartmentId) {
        viewModel.onIntent(ReminderListIntent.LoadReminders(apartmentId))
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ReminderListEffect.NavigateToAddReminder -> onNavigateToAddReminder()
                is ReminderListEffect.ShowError -> onShowError(effect.message)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onIntent(ReminderListIntent.AddReminderClicked) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Frist hinzufügen") }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Column(
                Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        val open = state.reminders.filter { !it.isCompleted }.sortedBy { it.dueAt }
        val done = state.reminders.filter { it.isCompleted }.sortedByDescending { it.dueAt }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { Text("Fristen und Erinnerungen", style = MaterialTheme.typography.headlineSmall) }

            if (state.reminders.isEmpty()) {
                item {
                    Text(
                        "Noch keine Erinnerungen für diese Wohnung.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            if (open.isNotEmpty()) {
                item { SectionTitle("Offen") }
                items(open, key = { it.id.value }) { reminder ->
                    ReminderRow(reminder, isOverdue = reminder.dueAt < now, viewModel)
                }
            }
            if (done.isNotEmpty()) {
                item { SectionTitle("Erledigt") }
                items(done, key = { it.id.value }) { reminder ->
                    ReminderRow(reminder, isOverdue = false, viewModel)
                }
            }

            item {
                Text(
                    "Bitte prüfe, welche Frist in deinem konkreten Fall gilt. " +
                            "Die App ersetzt keine Rechtsberatung.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}


