package com.example.shared.presentation.componentes

import androidx.compose.foundation.clickable
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
import com.example.shared.domain.model.BillSummary
import com.example.shared.domain.model.StatusKind
import com.example.shared.domain.usecase.formatEuro
import com.example.shared.util.toDisplayDate
import kotlin.math.abs

@Composable
 fun BillCard(summary: BillSummary, onClick: () -> Unit) {
    val bill = summary.bill
    val balance = bill.totalCostsCents - bill.advancePaymentsMadeCents
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Abrechnung ${bill.billingPeriodStart.year}", style = MaterialTheme.typography.titleMedium)
            Text(
                "${bill.billingPeriodStart.toDisplayDate()} – ${bill.billingPeriodEnd.toDisplayDate()}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                when {
                    balance < 0 -> "Guthaben ${formatEuro(abs(balance))}"
                    balance > 0 -> "Nachzahlung ${formatEuro(balance)}"
                    else -> "Ausgeglichen"
                },
                style = MaterialTheme.typography.bodyLarge
            )
            when (val n = summary.findingsCount) {
                null -> Unit
                0 -> StatusChip(StatusKind.OK, "Keine Auffälligkeit erkannt")
                else -> StatusChip(
                    StatusKind.NOTE,
                    if (n == 1) "1 Hinweis zur Durchsicht" else "$n Hinweise zur Durchsicht"
                )
            }
        }
    }
}