package com.example.mietcheck.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.shared.domain.repository.ReminderListEffect
import com.example.shared.domain.repository.ReminderListIntent
import com.example.shared.viewmodels.ReminderListViewModel


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
                items(state.reminders, key = { it.id.value }) { reminder ->
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