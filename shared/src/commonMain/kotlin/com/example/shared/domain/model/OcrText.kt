package com.example.shared.domain.model


data class OcrText(val rows: List<String>, val pagesRead: Int, val pagesTotal: Int)
