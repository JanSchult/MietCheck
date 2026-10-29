package com.example.shared.presentation.form

import com.example.shared.domain.model.CostCategory

fun CostCategory.displayName(): String = when (this) {
    CostCategory.WATER_SEWAGE -> "Wasser und Abwasser"
    CostCategory.HEATING -> "Heizung"
    CostCategory.HOT_WATER -> "Warmwasser"
    CostCategory.WASTE_DISPOSAL -> "Müllentsorgung"
    CostCategory.BUILDING_CLEANING -> "Hausreinigung"
    CostCategory.GARDENING -> "Gartenpflege"
    CostCategory.CARETAKER -> "Hausmeister"
    CostCategory.ELEVATOR -> "Aufzug"
    CostCategory.GENERAL_ELECTRICITY -> "Allgemeinstrom"
    CostCategory.BUILDING_INSURANCE -> "Gebäudeversicherung"
    CostCategory.PROPERTY_TAX -> "Grundsteuer"
    CostCategory.CHIMNEY_SWEEP -> "Schornsteinfeger"
    CostCategory.MAINTENANCE -> "Wartung"
    CostCategory.REPAIRS -> "Reparaturen"
    CostCategory.ADMINISTRATION -> "Verwaltung"
    CostCategory.OTHER -> "Sonstiges"
}