package com.example.shared.presentation.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.usecase.OpenCheck

@Composable
 fun OpenChecksSection(checks: List<OpenCheck>, onOpen: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Offene Prüfungen")
        if (checks.isEmpty()) {
            Text("Keine offenen Prüfhinweise.", style = MaterialTheme.typography.bodyLarge)
        } else {
            checks.forEach { check ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable { onOpen(check.bill.id.value) }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Abrechnung ${check.bill.billingPeriodStart.year}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (check.findingsCount == 1) "1 Hinweis zur Durchsicht"
                            else "${check.findingsCount} Hinweise zur Durchsicht",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}