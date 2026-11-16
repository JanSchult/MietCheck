package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.Apartment
import com.example.shared.domain.model.BillFormField
import com.example.shared.domain.model.CostItemInput
import com.example.shared.domain.model.DocumentType
import com.example.shared.domain.model.ImportedFile
import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.UtilityCostItem
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.CostItemId
import com.example.shared.domain.model.ids.UtilityBillId
import com.example.shared.domain.usecase.GetApartmentUseCase
import com.example.shared.domain.usecase.SaveUtilityBillUseCase
import com.example.shared.domain.usecase.centsToInput
import com.example.shared.domain.usecase.parseEuroToCents
import com.example.shared.domain.usecase.parseSqm
import com.example.shared.viewmodels.effects.UtilityBillFormEffect
import com.example.shared.viewmodels.intent.UtilityBillFormIntent
import com.example.shared.presentation.labels.displayName
import com.example.shared.domain.model.ItemField
import com.example.shared.domain.model.OcrField
import com.example.shared.domain.model.OcrReview
import com.example.shared.domain.usecase.ImportDocumentUseCase
import com.example.shared.domain.usecase.ImportResult
import com.example.shared.domain.usecase.ReadBillDocumentUseCase
import com.example.shared.util.formatSqm
import com.example.shared.util.parseDate
import com.example.shared.viewmodels.state.UtilityBillFormState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class UtilityBillFormViewModel(
    private val getApartment: GetApartmentUseCase,
    private val saveBill: SaveUtilityBillUseCase,
    private val readBill: ReadBillDocumentUseCase,
    private val importDocument: ImportDocumentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UtilityBillFormState())
    val state: StateFlow<UtilityBillFormState> = _state.asStateFlow()

    private val _effects = Channel<UtilityBillFormEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var apartment: Apartment? = null
    private var nextItemKey = 0L
    private var pendingDocument: ImportedFile? = null
    init { _state.update { it.copy(ocrAvailable = readBill.isAvailable) } }

    fun onIntent(intent: UtilityBillFormIntent) {
        when (intent) {
            is UtilityBillFormIntent.Init -> init(intent.apartmentId)
            is UtilityBillFormIntent.FieldChanged -> fieldChanged(intent.field, intent.value)
            UtilityBillFormIntent.AddItem -> addItem()
            is UtilityBillFormIntent.RemoveItem -> _state.update { s ->
                s.copy(items = s.items.filterNot { it.key == intent.key }, itemErrors = s.itemErrors - intent.key)
            }
            is UtilityBillFormIntent.ItemCategoryChanged ->
                updateItem(intent.key) { it.copy(category = intent.category) }
            is UtilityBillFormIntent.ItemTextChanged -> updateItem(intent.key) {
                when (intent.field) {
                    ItemField.LABEL -> it.copy(label = intent.value)
                    ItemField.AMOUNT -> it.copy(amount = intent.value)
                    ItemField.DISTRIBUTION_KEY -> it.copy(distributionKey = intent.value)
                }
            }
            UtilityBillFormIntent.ApplyAdvanceSuggestion -> applyAdvanceSuggestion()
            UtilityBillFormIntent.Save -> save()
            is UtilityBillFormIntent.OcrFileSelected -> readDocument(intent.file)
            is UtilityBillFormIntent.OcrToggle -> _state.update { s ->
                val review = s.review ?: return@update s
                val next = if (intent.field in review.selected) review.selected - intent.field else review.selected + intent.field
                s.copy(review = review.copy(selected = next))
            }
            UtilityBillFormIntent.OcrApply -> applyReview()
            UtilityBillFormIntent.OcrCancel -> _state.update { it.copy(review = null) }
            UtilityBillFormIntent.RemoveSourceDocument -> {
                pendingDocument = null
                _state.update { it.copy(sourceDocumentName = null) }
            }
        }
    }

    private fun init(apartmentIdRaw: String) {
        if (apartment?.id?.value == apartmentIdRaw) return   // Screen-Neuaufbau: nichts überschreiben
        viewModelScope.launch {
            val a = getApartment(ApartmentId(apartmentIdRaw))
            if (a == null) {
                _effects.send(UtilityBillFormEffect.ShowError("Wohnung nicht gefunden."))
                return@launch
            }
            apartment = a
            _state.update {
                it.copy(
                    apartmentId = apartmentIdRaw,
                    livingSpace = formatSqm(a.livingSpaceSqm),
                    occupants = a.numberOfOccupants.toString()
                )
            }
        }
    }
    private fun readDocument(file: ImportedFile) {
        _state.update { it.copy(isReading = true) }
        viewModelScope.launch {
            runCatching { readBill(file) }
                .onSuccess { outcome ->
                    val p = outcome.parsed
                    if (p.isEmpty) {
                        _effects.send(UtilityBillFormEffect.ShowError(
                            "Es konnten keine Werte erkannt werden. Du kannst die Angaben von Hand eintragen."
                        ))
                    } else {
                        // Unsichere Werte sind nicht vorausgewählt: Du entscheidest bewusst
                        val selected = buildSet {
                            if (p.period != null && !p.period.uncertain) add(OcrField.PERIOD)
                            if (p.issueDate != null && !p.issueDate.uncertain) add(OcrField.ISSUE_DATE)
                            if (p.livingSpaceSqm != null && !p.livingSpaceSqm.uncertain) add(OcrField.LIVING_SPACE)
                            if (p.occupants != null && !p.occupants.uncertain) add(OcrField.OCCUPANTS)
                            if (p.advancePaymentsCents != null && !p.advancePaymentsCents.uncertain) add(OcrField.ADVANCE)
                            if (p.totalCostsCents != null && !p.totalCostsCents.uncertain) add(OcrField.TOTAL)
                            if (p.items.isNotEmpty()) add(OcrField.ITEMS)
                        }
                        _state.update { it.copy(review = OcrReview(outcome, file, selected)) }
                    }
                }
                .onFailure { e ->
                    _effects.send(UtilityBillFormEffect.ShowError(e.message ?: "Das Dokument konnte nicht gelesen werden."))
                }
            _state.update { it.copy(isReading = false) }
        }
    }

    private fun applyReview() {
        val review = _state.value.review ?: return
        val p = review.outcome.parsed
        fun picked(field: OcrField) = field in review.selected

        // Schlüssel außerhalb von update{} vergeben (der Block kann erneut laufen)
        val newItems = if (picked(OcrField.ITEMS)) p.items.map {
            CostItemInput(
                key = nextItemKey++,
                category = it.category,
                label = it.label,
                amount = centsToInput(it.amountCents),
                distributionKey = it.distributionKey.orEmpty()
            )
        } else null

        pendingDocument = review.file
        _state.update { s ->
            s.copy(
                periodStart = p.period?.takeIf { picked(OcrField.PERIOD) }?.value?.first?.toString() ?: s.periodStart,
                periodEnd = p.period?.takeIf { picked(OcrField.PERIOD) }?.value?.second?.toString() ?: s.periodEnd,
                issueDate = p.issueDate?.takeIf { picked(OcrField.ISSUE_DATE) }?.value?.toString() ?: s.issueDate,
                livingSpace = p.livingSpaceSqm?.takeIf { picked(OcrField.LIVING_SPACE) }?.value?.let(::formatSqm) ?: s.livingSpace,
                occupants = p.occupants?.takeIf { picked(OcrField.OCCUPANTS) }?.value?.toString() ?: s.occupants,
                advancePayments = p.advancePaymentsCents?.takeIf { picked(OcrField.ADVANCE) }?.value?.let(::centsToInput) ?: s.advancePayments,
                totalCosts = p.totalCostsCents?.takeIf { picked(OcrField.TOTAL) }?.value?.let(::centsToInput) ?: s.totalCosts,
                items = newItems ?: s.items,
                review = null,
                sourceDocumentName = review.file.suggestedName,
                ocrResultCents = p.resultCents?.value,
                fieldErrors = emptyMap(),
                itemErrors = emptyMap()
            )
        }
    }
    private fun fieldChanged(field: BillFormField, value: String) = _state.update {
        val errors = it.fieldErrors - field
        when (field) {
            BillFormField.PERIOD_START -> it.copy(periodStart = value, fieldErrors = errors)
            BillFormField.PERIOD_END -> it.copy(periodEnd = value, fieldErrors = errors)
            BillFormField.ISSUE_DATE -> it.copy(issueDate = value, fieldErrors = errors)
            BillFormField.LIVING_SPACE -> it.copy(livingSpace = value, fieldErrors = errors)
            BillFormField.OCCUPANTS -> it.copy(occupants = value, fieldErrors = errors)
            BillFormField.ADVANCE -> it.copy(advancePayments = value, fieldErrors = errors)
            BillFormField.TOTAL -> it.copy(totalCosts = value, fieldErrors = errors)
            BillFormField.ITEMS -> it
        }
    }

    private fun addItem() {
        val key = nextItemKey++   // außerhalb von update{}, das bei Retry mehrfach laufen kann
        _state.update {
            it.copy(items = it.items + CostItemInput(key = key), fieldErrors = it.fieldErrors - BillFormField.ITEMS)
        }
    }

    private fun updateItem(key: Long, transform: (CostItemInput) -> CostItemInput) = _state.update { s ->
        s.copy(
            items = s.items.map { if (it.key == key) transform(it) else it },
            itemErrors = s.itemErrors - key
        )
    }

    /** Vorschlag: (Nebenkosten- + Heizkosten-Vorauszahlung) × Monate. Setzt unveränderte Raten voraus. */
    private fun applyAdvanceSuggestion() {
        val a = apartment ?: return
        val s = _state.value
        val start = parseDate(s.periodStart)
        val end = parseDate(s.periodEnd)
        if (start == null || end == null || end < start) {
            viewModelScope.launch {
                _effects.send(UtilityBillFormEffect.ShowError("Bitte zuerst einen gültigen Abrechnungszeitraum eingeben."))
            }
            return
        }
        val months = start.monthsUntil(end.plus(1, DateTimeUnit.DAY))
        val cents = (a.monthlyUtilityAdvanceCents + a.monthlyHeatingAdvanceCents) * months
        _state.update {
            it.copy(advancePayments = centsToInput(cents), fieldErrors = it.fieldErrors - BillFormField.ADVANCE)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun save() {
        val s = _state.value
        val a = apartment
        if (a == null) {
            viewModelScope.launch { _effects.send(UtilityBillFormEffect.ShowError("Keine Wohnung ausgewählt.")) }
            return
        }

        val errors = mutableMapOf<BillFormField, String>()
        val itemErrors = mutableMapOf<Long, String>()
        val dateHint = "Bitte das Datum als JJJJ-MM-TT eingeben."

        val start = parseDate(s.periodStart)
        if (start == null) errors[BillFormField.PERIOD_START] = dateHint
        val end = parseDate(s.periodEnd)
        if (end == null) errors[BillFormField.PERIOD_END] = dateHint
        if (start != null && end != null && end < start) {
            errors[BillFormField.PERIOD_END] = "Das Ende liegt vor dem Beginn."
        }

        var issue: LocalDate? = null
        if (s.issueDate.isNotBlank()) {
            issue = parseDate(s.issueDate)
            if (issue == null) errors[BillFormField.ISSUE_DATE] = dateHint
        }

        var sqm: Double? = null
        if (s.livingSpace.isNotBlank()) {
            sqm = parseSqm(s.livingSpace)
            if (sqm == null) errors[BillFormField.LIVING_SPACE] = "Bitte eine Zahl größer 0 eingeben."
        }

        var occupants: Int? = null
        if (s.occupants.isNotBlank()) {
            occupants = s.occupants.trim().toIntOrNull()?.takeIf { it in 1..20 }
            if (occupants == null) errors[BillFormField.OCCUPANTS] = "Bitte eine Zahl zwischen 1 und 20 eingeben."
        }

        val advance = parseEuroToCents(s.advancePayments)
        if (advance == null) errors[BillFormField.ADVANCE] = "Bitte einen Betrag eingeben, z. B. 2.160,00."
        val total = parseEuroToCents(s.totalCosts)
        if (total == null) errors[BillFormField.TOTAL] = "Bitte die Gesamtkosten laut Abrechnung eingeben."

        if (s.items.isEmpty()) errors[BillFormField.ITEMS] = "Bitte mindestens eine Kostenposition erfassen."

        val items = s.items.mapNotNull { input ->
            val cents = parseEuroToCents(input.amount)
            if (cents == null) {
                itemErrors[input.key] = "Bitte einen Betrag eingeben, z. B. 320,00."
                null
            } else {
                UtilityCostItem(
                    id = CostItemId.new(),
                    category = input.category,
                    label = input.label.trim().ifBlank { input.category.displayName() },
                    amountCents = cents,
                    distributionKey = input.distributionKey.trim().ifBlank { null },
                    note = null
                )
            }
        }

        if (errors.isNotEmpty() || itemErrors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors, itemErrors = itemErrors) }
            return
        }

        val now = Clock.System.now()
        val bill = UtilityBill(
            id = UtilityBillId.new(),
            apartmentId = a.id,
            documentId = null,
            billingPeriodStart = start!!,
            billingPeriodEnd = end!!,
            issueDate = issue,
            livingSpaceSqmAtBilling = sqm,
            occupantsAtBilling = occupants,
            advancePaymentsMadeCents = advance!!,
            totalCostsCents = total!!,
            distributionKeyNoted = items.any { it.distributionKey != null },
            items = items,
            createdAt = now,
            updatedAt = now
        )

        _state.update { it.copy(fieldErrors = emptyMap(), itemErrors = emptyMap(), isSaving = true) }
        viewModelScope.launch {
            runCatching { val documentId = pendingDocument?.let { file ->
                when (val imported = importDocument(a.id, DocumentType.UTILITY_BILL, file)) {
                    is ImportResult.Saved -> imported.id
                    is ImportResult.Duplicate -> imported.id
                }
            }
                saveBill(bill.copy(documentId = documentId))
            }
                .onSuccess {
                    pendingDocument = null
                    _state.update { it.copy(isSaving = false) }
                    _effects.send(UtilityBillFormEffect.Saved(bill.id.value))
                }
                .onFailure { e ->
                    _state.update { it.copy(isSaving = false) }
                    _effects.send(UtilityBillFormEffect.ShowError(e.message ?: "Speichern fehlgeschlagen"))
                }
        }
    }
}

