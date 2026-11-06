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
import com.example.shared.domain.model.DocumentRecord
import com.example.shared.presentation.document.label
import com.example.shared.util.formatDate
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
 fun DocumentCard(doc: DocumentRecord, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(doc.displayName, style = MaterialTheme.typography.titleMedium)
            Text(doc.type.label(), style = MaterialTheme.typography.bodyLarge)
            Text(
                buildString {
                    append("Hinzugefügt am ${doc.importedAt.formatDate()}")
                    if (doc.pageCount > 1) append(" · ${doc.pageCount} Seiten")
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}