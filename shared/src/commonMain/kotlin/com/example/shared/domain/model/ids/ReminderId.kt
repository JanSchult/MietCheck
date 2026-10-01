package com.example.shared.domain.model.ids

import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
value class ReminderId(val value: String) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun new(): ReminderId = ReminderId(Uuid.random().toString())
    }
}
