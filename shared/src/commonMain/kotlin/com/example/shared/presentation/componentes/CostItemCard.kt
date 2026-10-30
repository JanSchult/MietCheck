package com.example.shared.presentation.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.CostCategory
import com.example.shared.domain.model.CostItemInput
import com.example.shared.domain.model.CostItemRow
import com.example.shared.domain.model.ItemField
import com.example.shared.domain.usecase.formatEuro
import com.example.shared.presentation.form.displayName
import com.example.shared.presentation.utilitybill.kind
import com.example.shared.presentation.utilitybill.label

@Composable
 fun CostItemCard(
    item: CostItemInput,
    error: String?,
    onCategory: (CostCategory) -> Unit,
    onText: (ItemField, String) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryPicker(item.category, onCategory)
            FormField("Bezeichnung (leer = ${item.category.displayName()})", item.label, null,
                onChange = { onText(ItemField.LABEL, it) })
            FormField("Betrag in €", item.amount, error, KeyboardType.Decimal,
                onChange = { onText(ItemField.AMOUNT, it) })
            FormField("Verteilerschlüssel (optional, z. B. Wohnfläche)", item.distributionKey, null,
                onChange = { onText(ItemField.DISTRIBUTION_KEY, it) })
            OutlinedButton(onClick = onRemove, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Position entfernen")
            }
        }
    }
}

@Composable
 fun CostItemCard(row: CostItemRow) {
    var expanded by remember(row.item.id.value) { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(row.item.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(formatEuro(row.item.amountCents), style = MaterialTheme.typography.titleMedium)
            }
            row.item.distributionKey?.let {
                Text("Verteilerschlüssel: $it", style = MaterialTheme.typography.bodyMedium)
            }
            row.previousAmountCents?.let {
                Text("Vorjahr: ${formatEuro(it)}", style = MaterialTheme.typography.bodyMedium)
            }
            StatusChip(row.status.kind(), row.status.label())
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.heightIn(min = 48.dp)
            ) { Text(if (expanded) "Erklärung ausblenden" else "Was bedeutet das?") }
            if (expanded) Text(row.explanation, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
