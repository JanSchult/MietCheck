package com.example.shared.presentation.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.LetterTemplateType
import com.example.shared.presentation.componentes.CostItemCard
import com.example.shared.presentation.componentes.KeyFiguresCard
import com.example.shared.presentation.componentes.LetterSection
import com.example.shared.presentation.componentes.ResultCard
import com.example.shared.presentation.componentes.ValidationSection
import com.example.shared.presentation.utilitybill.UtilityBillOverviewEffect
import com.example.shared.presentation.utilitybill.UtilityBillOverviewIntent
import com.example.shared.util.toDisplayDate
import com.example.shared.viewmodels.UtilityBillOverviewViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UtilityBillOverviewScreen(
    billId: String,
    onWriteLetter: (billId: String, type: LetterTemplateType) -> Unit,
    onShowError: (String) -> Unit,
    viewModel: UtilityBillOverviewViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(billId) { viewModel.onIntent(UtilityBillOverviewIntent.LoadBill(billId)) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is UtilityBillOverviewEffect.NavigateToLetterEditor ->
                    onWriteLetter(effect.billId, effect.templateType)
                is UtilityBillOverviewEffect.ShowError -> onShowError(effect.message)
            }
        }
    }

    val overview = state.overview
    if (overview == null) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) { if (state.isLoading) CircularProgressIndicator() }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val bill = overview.bill
        Text(
            "Abrechnung ${bill.billingPeriodStart.year}",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            "${bill.billingPeriodStart.toDisplayDate()} – ${bill.billingPeriodEnd.toDisplayDate()}",
            style = MaterialTheme.typography.bodyLarge
        )

        ResultCard(overview)
        KeyFiguresCard(overview)

        Text("Kostenpositionen", style = MaterialTheme.typography.titleLarge)
        overview.itemRows.forEach { CostItemCard(it) }

        ValidationSection(
            results = state.validationResults,
            isValidating = state.isValidating,
            hasValidated = state.hasValidated,
            onRerun = { viewModel.onIntent(UtilityBillOverviewIntent.RunValidation) },
            onPrepareLetter = { viewModel.onIntent(UtilityBillOverviewIntent.StartLetterDraft(it)) }
        )

        LetterSection { viewModel.onIntent(UtilityBillOverviewIntent.StartLetterDraft(it)) }

        Text(
            "MietCheck führt eine technische und rechnerische Plausibilitätsprüfung durch. " +
                    "Die Ergebnisse sind keine Rechtsberatung und ersetzen keine Prüfung durch einen " +
                    "Mieterverein, eine Verbraucherzentrale oder eine qualifizierte Rechtsanwältin " +
                    "beziehungsweise einen qualifizierten Rechtsanwalt.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}







