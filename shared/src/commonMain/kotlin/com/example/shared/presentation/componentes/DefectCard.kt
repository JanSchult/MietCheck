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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.Defect
import com.example.shared.presentation.defect.kind
import com.example.shared.presentation.defect.label
import com.example.shared.util.formatDate
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun DefectCard(defect: Defect, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(defect.room, style = MaterialTheme.typography.titleMedium)
            Text(
                defect.description,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Erfasst am ${defect.createdAt.formatDate()} · Dringlichkeit: ${defect.urgency.label()}",
                style = MaterialTheme.typography.bodyMedium
            )
            StatusChip(defect.status.kind(), defect.status.label())
        }
    }
}