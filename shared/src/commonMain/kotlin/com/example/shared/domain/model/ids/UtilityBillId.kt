package com.example.shared.domain.model.ids

import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
value class UtilityBillId(val value: String) {
    companion object { @OptIn(ExperimentalUuidApi::class)
    fun new(): UtilityBillId = UtilityBillId(Uuid.random().toString()) }
}