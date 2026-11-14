package com.example.shared.presentation.screens

import com.example.shared.domain.model.StatusKind
import com.example.shared.presentation.componentes.StatusChip
import com.example.shared.viewmodels.effects.LetterEditorEffect
import com.example.shared.viewmodels.intent.LetterEditorIntent
import com.example.shared.viewmodels.LetterEditorViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.platform.TextSharer
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LetterEditorScreen(
    billId: String,
    templateType: String,
    onShowError: (String) -> Unit,
    viewModel: LetterEditorViewModel = koinViewModel(),
    sharer: TextSharer = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(billId, templateType) {
        viewModel.onIntent(LetterEditorIntent.Load(billId, templateType))
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LetterEditorEffect.Export -> sharer.shareText(effect.subject, effect.body)
                is LetterEditorEffect.ShowError -> onShowError(effect.message)
            }
        }
    }

    if (state.isLoading) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) { CircularProgressIndicator() }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Prüfe den Text und passe ihn an. Es wird nichts automatisch versendet.",
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
            value = state.subject,
            onValueChange = { viewModel.onIntent(LetterEditorIntent.SubjectChanged(it)) },
            label = { Text("Betreff") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.body,
            onValueChange = { viewModel.onIntent(LetterEditorIntent.BodyChanged(it)) },
            label = { Text("Text") },
            minLines = 14,
            modifier = Modifier.fillMaxWidth()
        )

        if (state.hasOpenPlaceholders) {
            StatusChip(StatusKind.NOTE, "Im Text stehen noch Platzhalter")
            Text(
                "Stellen in {{…}} oder […] solltest du vor dem Versand ergänzen, " +
                        "zum Beispiel Datum, Unterschrift und deine Frage.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable { viewModel.onIntent(LetterEditorIntent.ConfirmedChanged(!state.isConfirmed)) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.isConfirmed,
                onCheckedChange = { viewModel.onIntent(LetterEditorIntent.ConfirmedChanged(it)) }
            )
            Text(
                "Ich habe den Text gelesen und möchte ihn so verwenden.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = { viewModel.onIntent(LetterEditorIntent.ConfirmAndExport) },
            enabled = state.isConfirmed,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) { Text("Teilen oder exportieren", style = MaterialTheme.typography.titleMedium) }

        Text(
            "Die Vorlage ist eine neutrale Rückfrage und keine Rechtsberatung. " +
                    "Bei Unsicherheit kann ein Mieterverein oder eine Verbraucherzentrale helfen.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}