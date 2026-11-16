package com.example.shared.domain.usecase


import com.example.shared.domain.model.ImportedFile
import com.example.shared.domain.ocr.BillTextParser
import com.example.shared.domain.ocr.ParsedBill
import com.example.shared.domain.platform.OcrEngine

data class BillReadOutcome(val parsed: ParsedBill, val pagesRead: Int, val pagesTotal: Int)

class ReadBillDocumentUseCase(private val ocr: OcrEngine) {
    val isAvailable: Boolean get() = ocr.isAvailable

    suspend operator fun invoke(file: ImportedFile): BillReadOutcome {
        val text = ocr.recognize(file.bytes, file.mimeType)
        return BillReadOutcome(BillTextParser.parse(text.rows), text.pagesRead, text.pagesTotal)
    }
}