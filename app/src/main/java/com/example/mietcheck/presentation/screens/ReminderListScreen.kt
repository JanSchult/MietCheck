package com.example.mietcheck.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.repository.ReminderListEffect
import com.example.shared.domain.repository.ReminderListIntent
import com.example.shared.viewmodels.ReminderListViewModel
import org.koin.androidx.compose.koinViewModel


/**
 * Screen kennt nur ReminderListState/-Intent/-Effect. Kein direkter
 * Repository- oder UseCase-Zugriff, keine fachliche Logik hier.
 */
@Composable
fun ReminderListScreen(
    apartmentId: String,
    onNavigateToAddReminder: () -> Unit,
    onShowError: (String) -> Unit,
    viewModel: ReminderListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

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
            FloatingActionButton(onClick = {
                viewModel.onIntent(ReminderListIntent.AddReminderClicked)
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Erinnerung hinzufügen")
            }
        }
    ) { padding ->
        when {
            state.isLoading -> Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }

            state.reminders.isEmpty() -> Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { Text("Noch keine Erinnerungen für diese Wohnung.") }

            else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(state.reminders, key = { it.id }) { reminder ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(reminder.title)
                        Checkbox(
                            checked = reminder.isCompleted,
                            onCheckedChange = { checked ->
                                viewModel.onIntent(
                                    ReminderListIntent.ToggleCompleted(reminder.id, checked)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}