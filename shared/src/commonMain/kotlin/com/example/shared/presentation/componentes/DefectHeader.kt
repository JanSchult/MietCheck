package com.example.shared.presentation.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.Defect
import com.example.shared.presentation.defect.kind
import com.example.shared.presentation.defect.label
import com.example.shared.util.formatDate
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun DefectHeader(defect: Defect) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(defect.room, style = MaterialTheme.typography.headlineSmall)
        StatusChip(defect.status.kind(), defect.status.label())
        Text(defect.description, style = MaterialTheme.typography.bodyLarge)
        Text("Dringlichkeit: ${defect.urgency.label()}", style = MaterialTheme.typography.bodyMedium)
        Text("Erfasst am ${defect.createdAt.formatDate()}", style = MaterialTheme.typography.bodyMedium)
        defect.reportedAt?.let {
            Text("Gemeldet am ${it.formatDate()}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}