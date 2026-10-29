package com.example.shared.presentation.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.RecurrenceRule
import com.example.shared.domain.model.Reminder
import com.example.shared.domain.repository.ReminderListIntent
import com.example.shared.presentation.reminder.displayName
import com.example.shared.util.formatDate
import com.example.shared.viewmodels.ReminderListViewModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun ReminderRow(reminder: Reminder, isOverdue: Boolean, viewModel: ReminderListViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = reminder.isCompleted,
            onCheckedChange = { checked ->
                viewModel.onIntent(ReminderListIntent.ToggleCompleted(reminder.id, checked))
            }
        )
        Column(Modifier.weight(1f)) {
            Text(
                reminder.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
            )
            val meta = buildString {
                if (isOverdue) append("Überfällig · ")
                append(reminder.dueAt.formatDate())
                if (reminder.recurrenceRule != RecurrenceRule.NONE) {
                    append(" · ${reminder.recurrenceRule?.displayName()}")
                }
            }
            Text(
                meta,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOverdue) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { viewModel.onIntent(ReminderListIntent.DeleteReminder(reminder.id)) }) {
            Icon(Icons.Filled.Delete, contentDescription = "Erinnerung löschen")
        }
    }
}