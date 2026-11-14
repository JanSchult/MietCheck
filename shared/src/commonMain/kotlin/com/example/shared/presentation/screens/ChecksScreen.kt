package com.example.shared.presentation.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.viewmodels.effects.ChecksEffect
import com.example.shared.viewmodels.intent.ChecksIntent
import com.example.shared.presentation.componentes.BillCard
import com.example.shared.presentation.componentes.CenteredHint
import com.example.shared.viewmodels.ChecksViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChecksScreen(
    onAddBill: (apartmentId: String) -> Unit,
    onOpenBill: (billId: String) -> Unit,
    viewModel: ChecksViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ChecksEffect.NavigateToForm -> onAddBill(effect.apartmentId)
                is ChecksEffect.NavigateToBill -> onOpenBill(effect.billId)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (state.apartmentId != null) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.onIntent(ChecksIntent.AddBillClicked) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Abrechnung erfassen") }
                )
            }
        }
    ) { padding ->
        when {
            state.isLoading -> Column(
                Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }

            state.apartmentId == null -> CenteredHint(
                "Lege zuerst auf der Startseite eine Wohnung an.", padding
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text("Prüfungen", style = MaterialTheme.typography.headlineSmall) }

                if (state.bills.isEmpty()) {
                    item {
                        Text(
                            "Noch keine Abrechnung erfasst. Erfasse deine erste Abrechnung, " +
                                    "um sie auf Plausibilität zu prüfen.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    item {
                        Text(
                            "Tippe auf eine Abrechnung, um den Prüfbericht zu sehen " +
                                    "und eine Rückfrage vorzubereiten.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    items(state.bills, key = { it.bill.id.value }) { summary ->
                        BillCard(summary) { viewModel.onIntent(ChecksIntent.BillClicked(summary.bill.id.value)) }
                    }
                }
            }
        }
    }
}



