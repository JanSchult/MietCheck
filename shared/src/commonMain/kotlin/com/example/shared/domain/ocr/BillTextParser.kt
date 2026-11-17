package com.example.shared.domain.ocr


import com.example.shared.domain.model.enums.CostCategory
import com.example.shared.domain.usecase.formatEuro
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlin.math.abs

data class Found<T>(val value: T, val evidence: String, val uncertain: Boolean = false)

data class ParsedItem(
    val category: CostCategory,
    val label: String,
    val amountCents: Long,
    val distributionKey: String?,
    val evidence: String
)

data class ParsedBill(
    val period: Found<Pair<LocalDate, LocalDate>>?,
    val issueDate: Found<LocalDate>?,
    val livingSpaceSqm: Found<Double>?,
    val occupants: Found<Int>?,
    val advancePaymentsCents: Found<Long>?,
    val totalCostsCents: Found<Long>?,
    val resultCents: Found<Long>?,          // positiv = Nachzahlung, negativ = Guthaben
    val items: List<ParsedItem>,
    val notes: List<String>
) {
    val isEmpty: Boolean
        get() = period == null && issueDate == null && livingSpaceSqm == null && occupants == null &&
                advancePaymentsCents == null && totalCostsCents == null && items.isEmpty()
}

/**
 * Sucht Werte einer Nebenkostenabrechnung in erkannten Textzeilen (Schlüsselwörter, Datums- und Betragsmuster).
 * Das Ergebnis sind Vorschläge: Jeder Wert nennt seine Fundstelle, unsichere Werte sind markiert.
 */
object BillTextParser {

    fun parse(rawRows: List<String>): ParsedBill {
        val rows = rawRows.map { it.trim() }.filter { it.isNotEmpty() }
        val norm = rows.map { normalize(it) }

        val period = findPeriod(rows, norm)
        val issueDate = findIssueDate(rows, norm, period?.value)
        val area = findArea(rows, norm)
        val occupants = findOccupants(rows, norm)
        val result = findResult(rows, norm)
        val total = findTotal(rows, norm)
        val advance = pickAdvance(advanceCandidates(rows, norm), total?.value, result?.value)
        val items = findItems(rows, norm)

        val notes = mutableListOf<String>()
        var totalFinal = total
        var advanceFinal = advance

        // Fehlenden Wert aus den beiden anderen ableiten (als unsicher markiert)
        if (total == null && advance != null && result != null) {
            totalFinal = Found(advance.value + result.value, "berechnet aus Vorauszahlungen und Ergebnis", true)
        }
        if (advance == null && total != null && result != null) {
            advanceFinal = Found(total.value - result.value, "berechnet aus Gesamtkosten und Ergebnis", true)
        }
        if (total != null && advance != null && result != null &&
            abs(total.value - advance.value - result.value) > 1
        ) {
            notes += "Gesamtkosten, Vorauszahlungen und Ergebnis passen rechnerisch nicht zusammen. " +
                    "Bitte alle drei Werte mit dem Dokument vergleichen."
        }

        val shownTotal = totalFinal?.value
        if (items.isNotEmpty() && shownTotal != null) {
            val sum = items.sumOf { it.amountCents }
            notes += if (abs(sum - shownTotal) <= 100) {
                "Die Summe der erkannten Positionen (${formatEuro(sum)}) passt zur Gesamtsumme."
            } else {
                "Die erkannten Positionen ergeben ${formatEuro(sum)}, die Gesamtsumme ist ${formatEuro(shownTotal)}. " +
                        "Möglicherweise fehlen Positionen oder Beträge wurden falsch gelesen."
            }
        }

        return ParsedBill(period, issueDate, area, occupants, advanceFinal, totalFinal, result, items, notes)
    }

    // ---------- Zeitraum und Datum ----------

    private val DATE = Regex("""(\d{1,2})\.\s?(\d{1,2})\.\s?(\d{4}|\d{2})""")
    private val PERIOD_SHORT = Regex("""(\d{1,2})\.(\d{1,2})\.\s*(?:bis|[-–−])\s*(\d{1,2})\.(\d{1,2})\.(\d{4})""")
    private val YEAR_ONLY = Regex("""(?:abrechnungsjahr|abrechnungszeitraum|abrechnung)\D{0,15}(20\d{2})""")

    private fun safeDate(y: Int, m: Int, d: Int): LocalDate? = runCatching { LocalDate(y, m, d) }.getOrNull()

    private fun datesIn(row: String): List<LocalDate> = DATE.findAll(row).mapNotNull { m ->
        val year = m.groupValues[3].toInt().let { if (it < 100) it + 2000 else it }
        safeDate(year, m.groupValues[2].toInt(), m.groupValues[1].toInt())
    }.toList()

    private data class PeriodCandidate(val start: LocalDate, val end: LocalDate, val row: String, val score: Int)

    private fun findPeriod(rows: List<String>, norm: List<String>): Found<Pair<LocalDate, LocalDate>>? {
        val candidates = mutableListOf<PeriodCandidate>()
        rows.forEachIndexed { i, row ->
            val keyword = "zeitraum" in norm[i] || "abrechnung" in norm[i]
            var start: LocalDate? = null
            var end: LocalDate? = null
            val dates = datesIn(row)
            if (dates.size >= 2) {
                start = dates[0]; end = dates[1]
            } else {
                PERIOD_SHORT.find(row)?.let { m ->               // "01.01. bis 31.12.2025"
                    val endYear = m.groupValues[5].toInt()
                    val startMonth = m.groupValues[2].toInt()
                    val endMonth = m.groupValues[4].toInt()
                    val startYear = if (startMonth > endMonth) endYear - 1 else endYear
                    start = safeDate(startYear, startMonth, m.groupValues[1].toInt())
                    end = safeDate(endYear, endMonth, m.groupValues[3].toInt())
                }
            }
            val s = start
            val e = end
            if (s != null && e != null && e > s) {
                val days = s.daysUntil(e) + 1
                if (days in 28..400) {
                    candidates += PeriodCandidate(s, e, row, (if (keyword) 2 else 0) + (if (days in 350..380) 1 else 0))
                }
            }
        }
        val best = candidates.maxByOrNull { it.score }
        if (best != null) return Found(best.start to best.end, display(best.row), uncertain = best.score < 2)

        for (i in rows.indices) {                                  // nur ein Jahr genannt: "Abrechnungsjahr 2025"
            val year = YEAR_ONLY.find(norm[i])?.groupValues?.get(1)?.toInt() ?: continue
            return Found(LocalDate(year, 1, 1) to LocalDate(year, 12, 31), display(rows[i]), uncertain = true)
        }
        return null
    }

    private fun findIssueDate(rows: List<String>, norm: List<String>, period: Pair<LocalDate, LocalDate>?): Found<LocalDate>? {
        val keys = listOf("datum", "erstellt", "ausgestellt", "abrechnung vom", "abrechnungsdatum")
        val skip = listOf("faellig", "frist", "zahlung", "lastschrift", "einzug", "zeitraum")
        var fallback: Found<LocalDate>? = null
        for (i in rows.indices) {
            if (skip.any { it in norm[i] }) continue
            val dates = datesIn(rows[i]).filter { d -> period == null || (d != period.first && d != period.second) }
            val date = dates.firstOrNull { period == null || it > period.second } ?: continue
            if (period != null && period.second.daysUntil(date) > 600) continue
            if (keys.any { it in norm[i] }) return Found(date, display(rows[i]))
            if (period != null && fallback == null) fallback = Found(date, display(rows[i]), uncertain = true)
        }
        return fallback
    }

    // ---------- Fläche und Personen ----------

    private val AREA = Regex("""(\d{1,3}(?:[.,]\d{1,2})?)\s*(?:m²|m2|qm|㎡)""")

    private fun findArea(rows: List<String>, norm: List<String>): Found<Double>? {
        var fallback: Found<Double>? = null
        for (i in rows.indices) {
            if ("gesamt" in norm[i] || "haus" in norm[i] || "objekt" in norm[i]) continue
            val value = AREA.find(rows[i])?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull() ?: continue
            if (value !in 8.0..500.0) continue
            if ("flaeche" in norm[i]) return Found(value, display(rows[i]))
            if (fallback == null) fallback = Found(value, display(rows[i]), uncertain = true)
        }
        return fallback
    }

    private val PERSONS_AFTER = Regex("""(\d{1,2})\s*(?:personen|bewohner|pers\b)""")
    private val PERSONS_BEFORE = Regex("""(?:personenzahl|personen|bewohner)\D{0,25}?(\d{1,2})\b""")

    private fun findOccupants(rows: List<String>, norm: List<String>): Found<Int>? {
        for (i in rows.indices) {
            val n = norm[i]
            if (("person" !in n && "bewohner" !in n) || "aufzug" in n) continue
            val value = (PERSONS_AFTER.find(n) ?: PERSONS_BEFORE.find(n))?.groupValues?.get(1)?.toIntOrNull() ?: continue
            if (value in 1..20) return Found(value, display(rows[i]))
        }
        return null
    }

    // ---------- Beträge ----------

    private val AMOUNT = Regex("""(\d{1,3}(?:\.\d{3})+|\d+),(\d{2})""")

    /** Alle Beträge einer Zeile in Cent, in Lesereihenfolge. Ein direkt anliegendes Minus macht sie negativ. */
    private fun amountsIn(row: String): List<Long> = AMOUNT.findAll(row).mapNotNull { m ->
        val before = row.getOrNull(m.range.first - 1)
        val after = row.getOrNull(m.range.last + 1)
        if (before != null && (before.isDigit() || before == '.')) return@mapNotNull null
        if (after != null && after.isDigit()) return@mapNotNull null
        val euros = m.groupValues[1].replace(".", "").toLongOrNull() ?: return@mapNotNull null
        val cents = euros * 100 + m.groupValues[2].toLong()
        if (before == '-' || before == '−' || after == '-' || after == '−') -cents else cents
    }.toList()

    private data class Candidate(val cents: Long, val evidence: String, val score: Int)

    private fun findTotal(rows: List<String>, norm: List<String>): Found<Long>? {
        val keys = listOf(
            "gesamtkosten", "summe der kosten", "gesamtsumme", "summe betriebskosten", "betriebskosten gesamt",
            "gesamtbetrag", "kosten gesamt", "summe nebenkosten", "nebenkosten gesamt"
        )
        val tenant = listOf("ihr anteil", "ihre", "ihrer", "anteil", "wohnung", "mieter")
        val house = listOf("haus", "gebaeude", "objekt", "liegenschaft")
        val candidates = rows.indices.mapNotNull { i ->
            val n = norm[i]
            if (keys.none { it in n } || listOf("vorauszahl", "guthaben", "nachzahl").any { it in n }) return@mapNotNull null
            val cents = amountsIn(rows[i]).lastOrNull { it > 0 } ?: return@mapNotNull null
            val isTenant = tenant.any { it in n }
            var score = 0
            if (isTenant) score += 2
            if (!isTenant && house.any { it in n }) score -= 2          // Hausgesamtkosten statt Mieteranteil
            if ("heiz" in n || "warmwasser" in n) score -= 1            // vermutlich Teilsumme
            Candidate(cents, display(rows[i]), score)
        }
        if (candidates.isEmpty()) return null
        val best = candidates.maxOf { it.score }
        val top = candidates.filter { it.score == best }
        val pick = top.last()                                           // Endsummen stehen meist weiter unten
        return Found(pick.cents, pick.evidence, uncertain = best <= 0 || top.map { it.cents }.distinct().size > 1)
    }

    private fun advanceCandidates(rows: List<String>, norm: List<String>): List<Pair<Long, String>> {
        val keys = listOf("vorauszahl", "abschlag", "geleistete zahlung")
        return rows.indices
            .filter { i -> keys.any { it in norm[i] } }
            .mapNotNull { i -> amountsIn(rows[i]).lastOrNull { it > 0 }?.let { it to display(rows[i]) } }
            .distinctBy { it.first }
    }

    private fun pickAdvance(candidates: List<Pair<Long, String>>, total: Long?, result: Long?): Found<Long>? {
        if (candidates.isEmpty()) return null
        val expected = if (total != null && result != null) total - result else null
        if (expected != null) {
            candidates.firstOrNull { abs(it.first - expected) <= 1 }?.let { return Found(it.first, it.second) }
            val sum = candidates.sumOf { it.first }
            if (candidates.size > 1 && abs(sum - expected) <= 1) {
                return Found(sum, candidates.joinToString(" / ") { it.second }, uncertain = true)
            }
        }
        val best = candidates.maxByOrNull { it.first }!!           // Jahressumme statt Monatsrate
        return Found(best.first, best.second, uncertain = candidates.size > 1 || expected != null)
    }

    private data class ResultHit(val cents: Long, val evidence: String, val uncertain: Boolean)

    private fun findResult(rows: List<String>, norm: List<String>): Found<Long>? {
        val credit = listOf("guthaben", "erstattung", "rueckzahlung", "zu ihren gunsten")
        val debit = listOf("nachzahlung", "nachforderung", "zu ihren lasten", "zu zahlen", "nachzuzahlen")
        val neutral = listOf("saldo", "ergebnis", "endbetrag")
        var best: ResultHit? = null
        for (i in rows.indices) {
            val n = norm[i]
            val isCredit = credit.any { it in n }
            val isDebit = debit.any { it in n }
            if (!isCredit && !isDebit && neutral.none { it in n }) continue
            val amount = amountsIn(rows[i]).lastOrNull { it != 0L } ?: continue
            val magnitude = abs(amount)
            val hit = when {
                isCredit && !isDebit -> ResultHit(-magnitude, display(rows[i]), false)
                isDebit && !isCredit -> ResultHit(magnitude, display(rows[i]), false)
                else -> ResultHit(amount, display(rows[i]), true)      // Vorzeichen nur aus dem Betrag
            }
            if (best == null || (best.uncertain && !hit.uncertain)) best = hit
        }
        return best?.let { Found(it.cents, it.evidence, it.uncertain) }
    }

    // ---------- Kostenpositionen ----------

    private val SKIP = listOf(
        "gesamt", "summe", "zwischensumme", "vorauszahl", "abschlag", "guthaben", "nachzahl",
        "saldo", "ergebnis", "uebertrag", "zahlbetrag"
    )

    private val CATEGORY_KEYS: List<Pair<CostCategory, List<String>>> = listOf(
        CostCategory.HEATING to listOf("heiz", "fernwaerme", "brennstoff"),
        CostCategory.HOT_WATER to listOf("warmwasser"),
        CostCategory.WATER_SEWAGE to listOf("abwasser", "kaltwasser", "wasser", "entwaesserung"),
        CostCategory.WASTE_DISPOSAL to listOf("muell", "abfall"),
        CostCategory.BUILDING_CLEANING to listOf("reinigung", "treppenhaus"),
        CostCategory.GARDENING to listOf("garten", "gruenpflege", "aussenanlage"),
        CostCategory.CARETAKER to listOf("hauswart", "hausmeister"),
        CostCategory.ELEVATOR to listOf("aufzug", "fahrstuhl"),
        CostCategory.GENERAL_ELECTRICITY to listOf("allgemeinstrom", "hausstrom", "strom allgemein", "beleuchtung"),
        CostCategory.BUILDING_INSURANCE to listOf("versicherung"),
        CostCategory.PROPERTY_TAX to listOf("grundsteuer"),
        CostCategory.CHIMNEY_SWEEP to listOf("schornstein", "kaminkehr"),
        CostCategory.MAINTENANCE to listOf("wartung"),
        CostCategory.REPAIRS to listOf("reparatur", "instandhalt", "instandsetz"),
        CostCategory.ADMINISTRATION to listOf("verwaltung")
    )

    private fun findItems(rows: List<String>, norm: List<String>): List<ParsedItem> =
        rows.indices.mapNotNull { i ->
            val n = norm[i]
            if (SKIP.any { it in n }) return@mapNotNull null
            val category = CATEGORY_KEYS.firstOrNull { (_, keys) -> keys.any { it in n } }?.first
                ?: return@mapNotNull null
            val cents = amountsIn(rows[i]).lastOrNull() ?: return@mapNotNull null    // rechte Spalte = Mieteranteil
            if (cents <= 0) return@mapNotNull null
            ParsedItem(category, labelOf(rows[i]), cents, distributionKeyOf(n), display(rows[i]))
        }.distinctBy { Triple(it.category, it.label, it.amountCents) }

    private fun distributionKeyOf(n: String): String? = when {
        "wohnflaeche" in n || "m2" in n || "qm" in n -> "Wohnfläche"
        "verbrauch" in n -> "Verbrauch"
        "person" in n -> "Personen"
        "einheit" in n -> "Einheiten"
        "miteigentum" in n -> "Miteigentumsanteile"
        else -> null
    }

    private fun labelOf(row: String): String {
        val segment = row.split('\t').firstOrNull { seg -> seg.count { it.isLetter() } >= 3 } ?: return ""
        return segment.replace(AMOUNT, "").replace("€", "").replace("EUR", "")
            .trim().trimStart { !it.isLetter() }.trimEnd(':', '.', ' ')
    }

    // ---------- Hilfen ----------

    private fun normalize(s: String) = s.lowercase()
        .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss")

    private fun display(row: String) = row.replace('\t', ' ').replace(Regex("""\s{2,}"""), "  ").trim()
}