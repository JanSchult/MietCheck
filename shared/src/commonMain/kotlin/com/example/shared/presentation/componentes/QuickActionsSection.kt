package com.example.shared.presentation.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.QuickAction

@Composable
 fun QuickActionsSection(onClick: (QuickAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Schnellaktionen")
        val actions = listOf(
            QuickAction.CHECK_BILL to "Abrechnung prüfen",
            QuickAction.ADD_DOCUMENT to "Dokument hinzufügen",
            QuickAction.DOCUMENT_DEFECT to "Mangel dokumentieren",
            QuickAction.WRITE_LANDLORD to "Vermieter anschreiben"
        )
        actions.forEachIndexed { index, (action, label) ->
            // Erste Aktion hervorgehoben, Rest zurückhaltend
            if (index == 0) {
                Button(
                    onClick = { onClick(action) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) { Text(label, style = MaterialTheme.typography.titleMedium) }
            } else {
                OutlinedButton(
                    onClick = { onClick(action) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) { Text(label, style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}