package com.example.shared.presentation.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.usecase.formatEuro
import com.example.shared.viewmodels.state.UtilityBillFormState

@Composable
 fun SummaryCard(state: UtilityBillFormState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Zusammenfassung", style = MaterialTheme.typography.titleMedium)
            SummaryRow("Summe der Positionen", formatEuro(state.itemsSumCents))
            state.totalCents?.let { SummaryRow("Gesamtkosten laut Abrechnung", formatEuro(it)) }
            state.differenceCents?.let { diff ->
                SummaryRow("Differenz Positionen/Gesamtsumme", formatEuro(diff))
                if (diff != 0L) {
                    Text(
                        "Die Positionen ergeben nicht die angegebene Gesamtsumme. " +
                                "Prüfe, ob eine Position fehlt oder ein Betrag falsch übertragen wurde.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            state.balanceCents?.let { balance ->
                SummaryRow(
                    if (balance < 0) "Guthaben" else "Nachzahlung",
                    formatEuro(kotlin.math.abs(balance))
                )
            }
        }
    }
}