package com.example.shared.presentation.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.Apartment

@Composable
 fun ApartmentSection(
    apartments: List<Apartment>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Meine Wohnungen")
        if (apartments.isEmpty()) {
            Text(
                "Lege deine erste Wohnung an, um Abrechnungen und Dokumente zu verwalten.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(apartments, key = { it.id.value }) { apartment ->
                    FilterChip(
                        modifier = Modifier.heightIn(min = 48.dp),
                        selected = apartment.id.value == selectedId,
                        onClick = { onSelect(apartment.id.value) },
                        label = { Text(apartment.label, style = MaterialTheme.typography.bodyLarge) }
                    )
                }
            }
        }
        OutlinedButton(
            onClick = onAdd,
            modifier = Modifier.heightIn(min = 48.dp)
        ) { Text("Wohnung hinzufügen") }
    }
}
