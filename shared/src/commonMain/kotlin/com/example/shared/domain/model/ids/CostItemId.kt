package com.example.shared.domain.model.ids

import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
value class CostItemId(val value: String) {
    companion object { @OptIn(ExperimentalUuidApi::class)
    fun new(): CostItemId = CostItemId(Uuid.random().toString()) }
}