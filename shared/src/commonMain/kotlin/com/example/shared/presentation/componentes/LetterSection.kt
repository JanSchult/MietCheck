package com.example.shared.presentation.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.LetterTemplateType
import com.example.shared.presentation.utilitybill.displayName

@Composable
 fun LetterSection(onSelect: (LetterTemplateType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Rückfrage an den Vermieter", style = MaterialTheme.typography.titleLarge)
        Text(
            "Du kannst jede Vorlage vor dem Export bearbeiten. Es wird nichts automatisch versendet.",
            style = MaterialTheme.typography.bodyMedium
        )
        LetterTemplateType.entries.forEach { type ->
            OutlinedButton(
                onClick = { onSelect(type) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) { Text(type.displayName()) }
        }
    }
}