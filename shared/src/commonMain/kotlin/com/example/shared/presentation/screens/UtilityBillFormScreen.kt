package com.example.shared.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.BillFormField
import com.example.shared.presentation.componentes.CostItemCard
import com.example.shared.presentation.componentes.FormField
import com.example.shared.presentation.componentes.SummaryCard
import com.example.shared.presentation.form.UtilityBillFormEffect
import com.example.shared.presentation.form.UtilityBillFormIntent
import com.example.shared.viewmodels.UtilityBillFormViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UtilityBillFormScreen(
    apartmentId: String,
    onSaved: (String) -> Unit,
    onShowError: (String) -> Unit,
    viewModel: UtilityBillFormViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(apartmentId) { viewModel.onIntent(UtilityBillFormIntent.Init(apartmentId)) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is UtilityBillFormEffect.Saved -> onSaved(effect.billId)
                is UtilityBillFormEffect.ShowError -> onShowError(effect.message)
            }
        }
    }

    fun field(f: BillFormField): (String) -> Unit =
        { viewModel.onIntent(UtilityBillFormIntent.FieldChanged(f, it)) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Abrechnung erfassen", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Übertrage die Werte aus deiner Nebenkostenabrechnung. Alles bleibt auf diesem Gerät.",
            style = MaterialTheme.typography.bodyLarge
        )

        Text("Abrechnungszeitraum", style = MaterialTheme.typography.titleMedium)
        FormField("Beginn (JJJJ-MM-TT)", state.periodStart, state.fieldErrors[BillFormField.PERIOD_START],
            KeyboardType.Number, field(BillFormField.PERIOD_START))
        FormField("Ende (JJJJ-MM-TT)", state.periodEnd, state.fieldErrors[BillFormField.PERIOD_END],
            KeyboardType.Number, field(BillFormField.PERIOD_END))
        FormField("Datum der Abrechnung (optional)", state.issueDate, state.fieldErrors[BillFormField.ISSUE_DATE],
            KeyboardType.Number, field(BillFormField.ISSUE_DATE))

        Text("Angaben laut Abrechnung", style = MaterialTheme.typography.titleMedium)
        FormField("Wohnfläche in m² (optional)", state.livingSpace, state.fieldErrors[BillFormField.LIVING_SPACE],
            KeyboardType.Decimal, field(BillFormField.LIVING_SPACE))
        FormField("Bewohner (optional)", state.occupants, state.fieldErrors[BillFormField.OCCUPANTS],
            KeyboardType.Number, field(BillFormField.OCCUPANTS))
        FormField("Geleistete Vorauszahlungen gesamt in €", state.advancePayments,
            state.fieldErrors[BillFormField.ADVANCE], KeyboardType.Decimal, field(BillFormField.ADVANCE))
        OutlinedButton(
            onClick = { viewModel.onIntent(UtilityBillFormIntent.ApplyAdvanceSuggestion) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) { Text("Aus Wohnungsdaten berechnen") }
        Text(
            "Der Vorschlag geht von gleichbleibenden Monatsraten aus. Vergleiche ihn mit deinen Kontoauszügen.",
            style = MaterialTheme.typography.bodyMedium
        )
        FormField("Gesamtkosten laut Abrechnung in €", state.totalCosts,
            state.fieldErrors[BillFormField.TOTAL], KeyboardType.Decimal, field(BillFormField.TOTAL))

        Text("Kostenpositionen", style = MaterialTheme.typography.titleMedium)
        state.fieldErrors[BillFormField.ITEMS]?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        state.items.forEach { item ->
            CostItemCard(
                item = item,
                error = state.itemErrors[item.key],
                onCategory = { viewModel.onIntent(UtilityBillFormIntent.ItemCategoryChanged(item.key, it)) },
                onText = { f, v -> viewModel.onIntent(UtilityBillFormIntent.ItemTextChanged(item.key, f, v)) },
                onRemove = { viewModel.onIntent(UtilityBillFormIntent.RemoveItem(item.key)) }
            )
        }
        OutlinedButton(
            onClick = { viewModel.onIntent(UtilityBillFormIntent.AddItem) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) { Text("Position hinzufügen") }

        SummaryCard(state)

        Button(
            onClick = { viewModel.onIntent(UtilityBillFormIntent.Save) },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) {
            Text(
                if (state.isSaving) "Wird gespeichert …" else "Speichern und prüfen",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}