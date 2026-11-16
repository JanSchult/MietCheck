package com.example.shared.presentation.componentes

import com.example.shared.domain.model.OcrField
import com.example.shared.domain.model.OcrReview
import com.example.shared.util.toDisplayDate
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
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.StatusKind
import com.example.shared.domain.ocr.Found
import com.example.shared.domain.usecase.formatEuro
import com.example.shared.presentation.labels.displayName

@Composable
internal fun OcrSourceCard(
    isReading: Boolean,
    sourceName: String?,
    onPickFile: () -> Unit,
    onScan: () -> Unit,
    onRemoveSource: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Aus Foto oder PDF auslesen", style = MaterialTheme.typography.titleMedium)
            Text(
                "Die Texterkennung läuft komplett auf deinem Gerät. Erkannte Werte sind Vorschläge: " +
                        "Du bestätigst sie, bevor sie ins Formular übernommen werden.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (isReading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text("Dokument wird ausgelesen …", style = MaterialTheme.typography.bodyMedium)
            } else {
                Button(onClick = onPickFile, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Text("Datei auswählen")
                }
                OutlinedButton(onClick = onScan, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Text("Mit Kamera scannen")
                }
            }
            if (sourceName != null) {
                Text(
                    "Quelle: $sourceName. Wird beim Speichern verschlüsselt abgelegt und mit der Abrechnung verknüpft.",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onRemoveSource, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Quelle entfernen")
                }
            }
        }
    }
}

@Composable
internal fun OcrReviewContent(
    review: OcrReview,
    onToggle: (OcrField) -> Unit,
    onApply: () -> Unit,
    onCancel: () -> Unit
) {
    val p = review.outcome.parsed
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Erkannte Werte prüfen", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Die Texterkennung kann sich irren. Vergleiche jeden Wert mit deinem Dokument. Nur angehakte " +
                    "Werte werden ins Formular übernommen, dort kannst du sie noch ändern.",
            style = MaterialTheme.typography.bodyLarge
        )
        if (review.outcome.pagesRead < review.outcome.pagesTotal) {
            Text(
                "Es wurden nur die ersten ${review.outcome.pagesRead} von ${review.outcome.pagesTotal} Seiten gelesen.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        ValueRow(OcrField.PERIOD, "Abrechnungszeitraum",
            p.period?.let { "${it.value.first.toDisplayDate()} – ${it.value.second.toDisplayDate()}" }, p.period, review, onToggle)
        ValueRow(OcrField.ISSUE_DATE, "Datum der Abrechnung", p.issueDate?.value?.toDisplayDate(), p.issueDate, review, onToggle)
        ValueRow(OcrField.LIVING_SPACE, "Wohnfläche", p.livingSpaceSqm?.let { "${it.value} m²" }, p.livingSpaceSqm, review, onToggle)
        ValueRow(OcrField.OCCUPANTS, "Bewohner", p.occupants?.value?.toString(), p.occupants, review, onToggle)
        ValueRow(OcrField.ADVANCE, "Geleistete Vorauszahlungen", p.advancePaymentsCents?.let { formatEuro(it.value) }, p.advancePaymentsCents, review, onToggle)
        ValueRow(OcrField.TOTAL, "Gesamtkosten", p.totalCostsCents?.let { formatEuro(it.value) }, p.totalCostsCents, review, onToggle)

        p.resultCents?.let { result ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Ergebnis laut Abrechnung", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (result.value < 0) "Guthaben ${formatEuro(-result.value)}" else "Nachzahlung ${formatEuro(result.value)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text("Wird nicht übernommen, sondern später mit deinen Eingaben verglichen.", style = MaterialTheme.typography.bodyMedium)
                    Text("Gefunden in: ${result.evidence}", style = MaterialTheme.typography.bodySmall)
                    if (result.uncertain) StatusChip(StatusKind.NOTE, "Bitte besonders genau prüfen")
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = OcrField.ITEMS in review.selected,
                        enabled = p.items.isNotEmpty(),
                        onCheckedChange = { onToggle(OcrField.ITEMS) }
                    )
                    Text("Kostenpositionen (${p.items.size})", style = MaterialTheme.typography.titleMedium)
                }
                if (p.items.isEmpty()) {
                    Text("Keine Positionen erkannt. Bitte von Hand eintragen.", style = MaterialTheme.typography.bodyMedium)
                }
                p.items.forEach { item ->
                    Column {
                        Text(
                            "${item.label.ifBlank { item.category.displayName() }}  ·  ${formatEuro(item.amountCents)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            buildString {
                                append(item.category.displayName())
                                item.distributionKey?.let { append(" · Schlüssel: $it") }
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        p.notes.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }

        Button(onClick = onApply, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Text("Ausgewählte Werte übernehmen", style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text("Abbrechen, von Hand eintragen")
        }
    }
}

@Composable
private fun <T> ValueRow(
    field: OcrField,
    title: String,
    display: String?,
    found: Found<T>?,
    review: OcrReview,
    onToggle: (OcrField) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Checkbox(
                checked = field in review.selected,
                enabled = found != null,
                onCheckedChange = { onToggle(field) }
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (found == null || display == null) {
                    Text("Nicht erkannt. Bitte von Hand eintragen.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(display, style = MaterialTheme.typography.bodyLarge)
                    Text("Gefunden in: ${found.evidence}", style = MaterialTheme.typography.bodySmall)
                    if (found.uncertain) StatusChip(StatusKind.NOTE, "Bitte besonders genau prüfen")
                }
            }
        }
    }
}