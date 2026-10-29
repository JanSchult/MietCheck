package com.example.shared.presentation.form

import com.example.shared.domain.model.BillFormField
import com.example.shared.domain.model.CostCategory
import com.example.shared.domain.model.ItemField

sealed interface UtilityBillFormIntent {
    data class Init(val apartmentId: String) : UtilityBillFormIntent
    data class FieldChanged(val field: BillFormField, val value: String) : UtilityBillFormIntent
    data object AddItem : UtilityBillFormIntent
    data class RemoveItem(val key: Long) : UtilityBillFormIntent
    data class ItemCategoryChanged(val key: Long, val category: CostCategory) : UtilityBillFormIntent
    data class ItemTextChanged(val key: Long, val field: ItemField, val value: String) : UtilityBillFormIntent
    data object ApplyAdvanceSuggestion : UtilityBillFormIntent
    data object Save : UtilityBillFormIntent
}