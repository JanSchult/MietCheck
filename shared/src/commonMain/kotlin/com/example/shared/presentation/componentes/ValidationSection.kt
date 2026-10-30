package com.example.shared.presentation.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.LetterTemplateType
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.ValidationSeverity
import com.example.shared.presentation.utilitybill.kind
import com.example.shared.presentation.utilitybill.label
import com.example.shared.presentation.utilitybill.suggestedTemplateFor

@Composable
fun ValidationSection(
    results: List<ValidationResult>,
    isValidating: Boolean,
    hasValidated: Boolean,
    onRerun: () -> Unit,
    onPrepareLetter: (LetterTemplateType) -> Unit
) {
    var showOk by remember { mutableStateOf(false) }
    val findings = results.filter { it.severity != ValidationSeverity.OK }
    val ok = results.filter { it.severity == ValidationSeverity.OK }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Prüfbericht", style = MaterialTheme.typography.titleLarge)

        if (isValidating) CircularProgressIndicator()

        if (hasValidated && !isValidating) {
            Text(
                if (findings.isEmpty()) "Alle ${results.size} Prüfungen ohne Auffälligkeit."
                else "${findings.size} von ${results.size} Prüfungen mit Hinweis zur Durchsicht.",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        findings.forEach { result ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(result.severity.kind(), result.severity.label())
                    Text(result.whatWasChecked, style = MaterialTheme.typography.titleMedium)
                    LabeledText("Gefunden", result.whatWasFound)
                    LabeledText("Warum das relevant ist", result.whyRelevant)
                    LabeledText("Mögliche nächste Schritte", result.suggestedNextStep)
                    OutlinedButton(
                        onClick = { onPrepareLetter(suggestedTemplateFor(result.ruleId)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) { Text("Rückfrage vorbereiten") }
                }
            }
        }

        if (ok.isNotEmpty() && hasValidated) {
            TextButton(
                onClick = { showOk = !showOk },
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(if (showOk) "Unauffällige Prüfungen ausblenden" else "${ok.size} unauffällige Prüfungen anzeigen")
            }
            if (showOk) {
                ok.forEach { result ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusChip(result.severity.kind(), result.severity.label())
                            Text(result.whatWasChecked, style = MaterialTheme.typography.titleMedium)
                            Text(result.whatWasFound, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onRerun,
            enabled = !isValidating,
            modifier = Modifier.heightIn(min = 48.dp)
        ) { Text("Erneut prüfen") }
    }
}


