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
import kotlin.math.abs

@Composable
 fun ResultCard(overview: BillOverview) {
    val balance = overview.balanceCents
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                when {
                    balance < 0 -> "Guthaben"
                    balance > 0 -> "Nachzahlung"
                    else -> "Ausgeglichen"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Text(formatEuro(abs(balance)), style = MaterialTheme.typography.displaySmall)
            Text(
                "Gesamtkosten abzüglich der geleisteten Vorauszahlungen, so wie du sie erfasst hast.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}