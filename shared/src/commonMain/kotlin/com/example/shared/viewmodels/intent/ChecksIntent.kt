package com.example.shared.viewmodels.intent

sealed interface ChecksIntent {
    data object AddBillClicked : ChecksIntent
    data class BillClicked(val billId: String) : ChecksIntent
}