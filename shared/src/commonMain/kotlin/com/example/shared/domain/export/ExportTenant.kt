package com.example.shared.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportTenant(val name: String, val email: String, val phone: String)