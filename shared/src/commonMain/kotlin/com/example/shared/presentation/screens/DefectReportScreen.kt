package com.example.shared.presentation.screens


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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.StatusKind
import com.example.shared.presentation.componentes.StatusChip
import com.example.shared.viewmodels.DefectReportViewModel
import com.example.shared.viewmodels.effects.DefectReportEffect
import com.example.shared.viewmodels.intent.DefectReportIntent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DefectReportScreen(
    defectId: String,
    onShowMessage: (String) -> Unit,
    viewModel: DefectReportViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(defectId) { viewModel.onIntent(DefectReportIntent.Load(defectId)) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            if (effect is DefectReportEffect.ShowMessage) onShowMessage(effect.message)
        }
    }

    if (state.isLoading || state.notFound) {
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Prüfe den Text und passe ihn an. Es wird nichts automatisch versendet.",
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
            value = state.subject,
            onValueChange = { viewModel.onIntent(DefectReportIntent.SubjectChanged(it)) },
            label = { Text("Betreff (für E-Mail)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.body,
            onValueChange = { viewModel.onIntent(DefectReportIntent.BodyChanged(it)) },
            label = { Text("Text") },
            minLines = 16,
            modifier = Modifier.fillMaxWidth()
        )

        if (state.hasOpenPlaceholders) {
            StatusChip(StatusKind.NOTE, "Im Text stehen noch Platzhalter")
            Text(
                "Stellen in {{…}} oder […] solltest du vor dem Versand ergänzen, zum Beispiel Datum und Unterschrift. " +
                        "Deinen Namen kannst du dauerhaft unter Einstellungen → Mein Profil hinterlegen.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable { viewModel.onIntent(DefectReportIntent.ConfirmedChanged(!state.isConfirmed)) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.isConfirmed,
                onCheckedChange = { viewModel.onIntent(DefectReportIntent.ConfirmedChanged(it)) }
            )
            Text(
                "Ich habe den Text gelesen und möchte ihn so verwenden.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = { viewModel.onIntent(DefectReportIntent.ShareText) },
            enabled = state.isConfirmed,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) { Text("Als Text teilen", style = MaterialTheme.typography.titleMedium) }

        OutlinedButton(
            onClick = { viewModel.onIntent(DefectReportIntent.SharePdf) },
            enabled = state.isConfirmed && !state.isWorking,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) { Text(if (state.isWorking) "PDF wird erstellt …" else "Als PDF teilen") }

        Text(
            "Fotos und Videos sind nicht im Text oder PDF enthalten. Du kannst sie im Mangel über " +
                    "„Teilen“ einzeln versenden. Trage nach dem Versand einen Eintrag im Verlauf ein und setze " +
                    "den Status auf „Gemeldet“.",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Die Mängelanzeige ist eine sachliche Mitteilung und keine Rechtsberatung. Bei Unsicherheit " +
                    "kann ein Mieterverein oder eine Verbraucherzentrale helfen.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}