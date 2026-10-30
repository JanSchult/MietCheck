package com.example.shared.domain.usecase

import com.example.shared.domain.model.CostCategory

/** Allgemeine Erklärungen, keine Rechtsberatung. Später auf String-Resources umstellen (i18n). */
object CostItemExplanations {
    fun forCategory(category: CostCategory): String = when (category) {
        CostCategory.WATER_SEWAGE ->
            "Kosten für Frischwasser und Abwasser, oft nach Zählerstand verteilt. Prüfe, ob die abgerechneten Verbrauchswerte zu deinen eigenen Ablesungen passen."
        CostCategory.HEATING ->
            "Kosten für Brennstoff und Betrieb der Heizungsanlage. Meist teils nach Verbrauch, teils nach Fläche verteilt. Prüfe die Verbrauchswerte und den Verteilerschlüssel."
        CostCategory.HOT_WATER ->
            "Kosten für die Erwärmung und Bereitstellung von Warmwasser, häufig nach Warmwasserzähler abgerechnet."
        CostCategory.WASTE_DISPOSAL ->
            "Gebühren für Müllabfuhr und Müllentsorgung des Hauses. Sie werden meist nach Fläche oder Personenzahl verteilt."
        CostCategory.BUILDING_CLEANING ->
            "Reinigung gemeinsam genutzter Flächen wie Treppenhaus, Flur oder Keller."
        CostCategory.GARDENING ->
            "Pflege gemeinsamer Außenanlagen, zum Beispiel Rasen, Hecken und Wege."
        CostCategory.CARETAKER ->
            "Laufende Hausmeistertätigkeiten. Reparatur- und Verwaltungsaufgaben gehören grundsätzlich nicht dazu. Prüfe, ob der Betrag nur laufende Tätigkeiten enthält."
        CostCategory.ELEVATOR ->
            "Betrieb, Strom und regelmäßige Wartung des Aufzugs. Bei Wohnungen im Erdgeschoss kann die Verteilung eine Rückfrage wert sein."
        CostCategory.GENERAL_ELECTRICITY ->
            "Strom für gemeinsam genutzte Bereiche, zum Beispiel Treppenhausbeleuchtung."
        CostCategory.BUILDING_INSURANCE ->
            "Beiträge für Versicherungen des Gebäudes, etwa gegen Feuer, Sturm oder Wasserschäden."
        CostCategory.PROPERTY_TAX ->
            "Laufende Steuer auf das Grundstück, die der Vermieter an die Gemeinde zahlt."
        CostCategory.CHIMNEY_SWEEP ->
            "Gebühren für Schornsteinfeger, etwa für Messungen und Kehrarbeiten an der Heizungsanlage."
        CostCategory.MAINTENANCE ->
            "Regelmäßige Wartung technischer Anlagen. Prüfe, ob es sich um laufende Wartung handelt und nicht um eine einmalige Instandsetzung."
        CostCategory.REPAIRS ->
            "Reparaturkosten sind nicht automatisch umlagefähig. Prüfe, ob die Position tatsächlich laufende Betriebskosten enthält. Bei Unsicherheit kann eine Mieterberatung oder ein Rechtsanwalt helfen."
        CostCategory.ADMINISTRATION ->
            "Verwaltungskosten sind nicht automatisch umlagefähig. Eine Rückfrage zur Zusammensetzung der Position kann sinnvoll sein. Bei Unsicherheit kann eine Mieterberatung oder ein Rechtsanwalt helfen."
        CostCategory.OTHER ->
            "Diese Position lässt sich keiner Standardkategorie zuordnen. Eine Rückfrage zur genauen Bedeutung kann sinnvoll sein."
    }
}