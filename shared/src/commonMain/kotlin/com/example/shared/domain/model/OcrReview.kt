package com.example.shared.domain.model

import com.example.shared.domain.model.enums.OcrField
import com.example.shared.domain.usecase.BillReadOutcome

data class OcrReview(
    val outcome: BillReadOutcome,
    val file: ImportedFile,
    val selected: Set<OcrField>
)