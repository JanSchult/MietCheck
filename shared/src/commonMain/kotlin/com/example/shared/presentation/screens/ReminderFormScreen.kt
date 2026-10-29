package com.example.shared.presentation.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.RecurrenceRule
import com.example.shared.domain.model.ReminderType
import com.example.shared.presentation.componentes.DropdownField
import com.example.shared.presentation.componentes.FormField
import com.example.shared.presentation.reminder.ReminderFormEffect
import com.example.shared.presentation.reminder.ReminderFormIntent
import com.example.shared.presentation.reminder.displayName
import com.example.shared.viewmodels.ReminderFormViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReminderFormScreen(
    apartmentId: String,
    onSaved: () -> Unit,
    onShowError: (String) -> Unit,
    viewModel: ReminderFormViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(apartmentId) { viewModel.onIntent(ReminderFormIntent.Init(apartmentId)) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ReminderFormEffect.Saved -> onSaved()
                is ReminderFormEffect.ShowError -> onShowError(effect.message)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Frist hinzufügen", style = MaterialTheme.typography.headlineSmall)

        Text("Art der Erinnerung", style = MaterialTheme.typography.titleMedium)
        DropdownField(
            selected = state.type,
            options = ReminderType.entries,
            label = { it.displayName() },
            onSelected = { viewModel.onIntent(ReminderFormIntent.TypeChanged(it)) }
        )

        FormField(
            label = "Titel (leer = ${state.type.displayName()})",
            value = state.title,
            error = null,
            onChange = { viewModel.onIntent(ReminderFormIntent.TitleChanged(it)) }
        )
        FormField(
            label = "Fällig am (JJJJ-MM-TT)",
            value = state.dueDate,
            error = state.dateError,
            keyboardType = KeyboardType.Number,
            onChange = { viewModel.onIntent(ReminderFormIntent.DueDateChanged(it)) }
        )

        Text("Wiederholung", style = MaterialTheme.typography.titleMedium)
        DropdownField(
            selected = state.recurrence,
            options = RecurrenceRule.entries,
            label = { it.displayName() },
            onSelected = { viewModel.onIntent(ReminderFormIntent.RecurrenceChanged(it)) }
        )

        Text(
            "Bitte prüfe, welche Frist in deinem konkreten Fall gilt. " +
                    "Die App ersetzt keine Rechtsberatung.",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = { viewModel.onIntent(ReminderFormIntent.Save) },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) {
            Text(
                if (state.isSaving) "Wird gespeichert …" else "Speichern",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}