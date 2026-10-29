package com.example.shared.presentation.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.CostCategory
import com.example.shared.domain.model.CostItemInput
import com.example.shared.domain.model.ItemField
import com.example.shared.presentation.form.displayName

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