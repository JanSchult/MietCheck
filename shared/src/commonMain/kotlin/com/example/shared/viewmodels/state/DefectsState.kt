package com.example.shared.viewmodels.state

import com.example.shared.domain.model.Defect
import com.example.shared.domain.model.DefectStatus

data class DefectsState(
    val apartmentId: String? = null,
    val defects: List<Defect> = emptyList(),
    val filter: DefectStatus? = null,
    val isLoading: Boolean = true
) {
    val visible: List<Defect> get() = if (filter == null) defects else defects.filter { it.status == filter }
}