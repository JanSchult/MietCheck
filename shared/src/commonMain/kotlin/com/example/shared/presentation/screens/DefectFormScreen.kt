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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.DefectUrgency
import com.example.shared.presentation.componentes.DropdownField
import com.example.shared.presentation.componentes.FormField
import com.example.shared.presentation.defect.DefectFormEffect
import com.example.shared.presentation.defect.DefectFormIntent
import com.example.shared.presentation.defect.label
import com.example.shared.viewmodels.DefectFormViewModel
import com.example.shared.viewmodels.state.DEFECT_ROOMS
import com.example.shared.viewmodels.state.OTHER_ROOM
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DefectFormScreen(
    onSaved: (String) -> Unit,
    onShowError: (String) -> Unit,
    viewModel: DefectFormViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DefectFormEffect.Saved -> onSaved(effect.defectId)
                is DefectFormEffect.ShowError -> onShowError(effect.message)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Das Datum wird automatisch gespeichert. Fotos und Videos ergänzt du gleich nach dem Speichern.",
            style = MaterialTheme.typography.bodyLarge
        )

        Text("Raum", style = MaterialTheme.typography.titleMedium)
        DropdownField(
            selected = state.room,
            options = DEFECT_ROOMS,
            label = { it },
            onSelected = { viewModel.onIntent(DefectFormIntent.RoomSelected(it)) }
        )
        if (state.room == OTHER_ROOM) {
            FormField(
                label = "Welcher Raum?",
                value = state.customRoom,
                error = state.roomError,
                onChange = { viewModel.onIntent(DefectFormIntent.CustomRoomChanged(it)) }
            )
        }

        Text("Beschreibung", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = state.description,
            onValueChange = { viewModel.onIntent(DefectFormIntent.DescriptionChanged(it)) },
            label = { Text("Was ist das Problem? Seit wann?") },
            isError = state.descriptionError != null,
            supportingText = state.descriptionError?.let { { Text(it) } },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Dringlichkeit", style = MaterialTheme.typography.titleMedium)
        DropdownField(
            selected = state.urgency,
            options = DefectUrgency.entries,
            label = { it.label() },
            onSelected = { viewModel.onIntent(DefectFormIntent.UrgencySelected(it)) }
        )

        Button(
            onClick = { viewModel.onIntent(DefectFormIntent.Save) },
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