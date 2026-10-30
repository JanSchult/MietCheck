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
import com.example.shared.domain.model.BillOverview
import com.example.shared.domain.usecase.formatEuro
import com.example.shared.domain.usecase.formatSignedTenthPercent
import com.example.shared.presentation.screens.FigureRow

@Composable
 fun KeyFiguresCard(overview: BillOverview) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Kennzahlen", style = MaterialTheme.typography.titleMedium)
            FigureRow("Gesamtkosten", formatEuro(overview.bill.totalCostsCents))
            FigureRow("Geleistete Vorauszahlungen", formatEuro(overview.bill.advancePaymentsMadeCents))
            overview.monthlyAdvanceCents?.let {
                FigureRow("Monatliche Vorauszahlung (laut Wohnung)", formatEuro(it))
            }
            overview.costPerSqmCents?.let { FigureRow("Kosten pro m²", formatEuro(it)) }
            if (overview.previousBill != null) {
                overview.previousCostPerSqmCents?.let { FigureRow("Kosten pro m² im Vorjahr", formatEuro(it)) }
                overview.yearOverYearTenthPercent?.let {
                    FigureRow("Veränderung zum Vorjahr", formatSignedTenthPercent(it))
                }
            } else {
                Text(
                    "Für einen Vorjahresvergleich fehlt noch die Abrechnung des Vorjahres.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
