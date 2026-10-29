package com.example.shared.presentation.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.DocumentRecord

@Composable
 fun DocumentsSection(documents: List<DocumentRecord>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Letzte Dokumente")
        if (documents.isEmpty()) {
            Text("Noch keine Dokumente importiert.", style = MaterialTheme.typography.bodyLarge)
        } else {
            documents.forEach { doc ->
                Text(
                    doc.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }
    }
}