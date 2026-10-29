package com.example.shared.presentation.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.QuickAction
import com.example.shared.presentation.componentes.ApartmentSection
import com.example.shared.presentation.componentes.DocumentsSection
import com.example.shared.presentation.componentes.LegalNotice
import com.example.shared.presentation.componentes.OpenChecksSection
import com.example.shared.presentation.componentes.QuickActionsSection
import com.example.shared.presentation.componentes.RemindersSection
import com.example.shared.presentation.dashboard.DashboardEffect
import com.example.shared.presentation.dashboard.DashboardIntent
import com.example.shared.viewmodels.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    onQuickAction: (QuickAction, String?) -> Unit,
    onAddApartment: () -> Unit,
    onOpenBill: (String) -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DashboardEffect.StartQuickAction -> onQuickAction(effect.action, effect.apartmentId)
                DashboardEffect.NavigateToAddApartment -> onAddApartment()
                is DashboardEffect.NavigateToBill -> onOpenBill(effect.billId)
            }
        }
    }

    Scaffold { padding ->
        if (state.isLoading) {
            Column(
                Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ApartmentSection(
                    apartments = state.apartments,
                    selectedId = state.selectedApartmentId,
                    onSelect = { viewModel.onIntent(DashboardIntent.SelectApartment(it)) },
                    onAdd = { viewModel.onIntent(DashboardIntent.AddApartmentClicked) }
                )
            }

            if (state.selectedApartmentId != null) {
                item {
                    QuickActionsSection { viewModel.onIntent(DashboardIntent.QuickActionClicked(it)) }
                }
                item {
                    OpenChecksSection(state.openChecks) {
                        viewModel.onIntent(DashboardIntent.OpenCheckClicked(it))
                    }
                }
                item { RemindersSection(state.upcomingReminders) }
                item { DocumentsSection(state.recentDocuments) }
            }

            item { LegalNotice() }
        }
    }
}



