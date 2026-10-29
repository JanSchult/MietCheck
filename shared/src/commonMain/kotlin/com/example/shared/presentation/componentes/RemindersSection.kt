package com.example.shared.presentation.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.Reminder
import com.example.shared.util.formatDate
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
 fun RemindersSection(reminders: List<Reminder>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Anstehende Fristen")
        if (reminders.isEmpty()) {
            Text("Keine anstehenden Erinnerungen.", style = MaterialTheme.typography.bodyLarge)
        } else {
            reminders.forEach { reminder ->
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(reminder.title, style = MaterialTheme.typography.bodyLarge)
                    Text(reminder.dueAt.formatDate(), style = MaterialTheme.typography.bodyLarge)
                }
            }
            Text(
                "Bitte prüfe, welche Frist in deinem konkreten Fall gilt. " +
                        "Die App ersetzt keine Rechtsberatung.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
