package com.example.shared.presentation.checks

sealed interface ChecksIntent {
    data object AddBillClicked : ChecksIntent
    data class BillClicked(val billId: String) : ChecksIntent
}