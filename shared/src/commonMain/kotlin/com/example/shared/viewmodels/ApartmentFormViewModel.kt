package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.Address
import com.example.shared.domain.model.Apartment
import com.example.shared.domain.model.ApartmentFormField
import com.example.shared.domain.model.LandlordContact
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.LandlordContactId
import com.example.shared.domain.usecase.SaveApartmentUseCase
import com.example.shared.domain.usecase.parseEuroToCents
import com.example.shared.domain.usecase.parseSqm
import com.example.shared.presentation.apartment.ApartmentFormEffect
import com.example.shared.presentation.apartment.ApartmentFormIntent
import com.example.shared.viewmodels.state.ApartmentFormState
import com.example.shared.domain.model.ApartmentField
import com.example.shared.domain.repository.SelectedApartmentRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ApartmentFormViewModel(
    private val saveApartment: SaveApartmentUseCase,
    private val selectedApartment: SelectedApartmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ApartmentFormState())
    val state: StateFlow<ApartmentFormState> = _state.asStateFlow()

    private val _effects = Channel<ApartmentFormEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: ApartmentFormIntent) {
        when (intent) {
            is ApartmentFormIntent.FieldChanged -> onFieldChanged(intent.field, intent.value)
            ApartmentFormIntent.Save -> save()
        }
    }

    private fun onFieldChanged(field: ApartmentFormField, v: String) = _state.update {
        when (field) {
            ApartmentFormField.LABEL -> it.copy(label = v)
            ApartmentFormField.STREET -> it.copy(street = v)
            ApartmentFormField.HOUSE_NUMBER -> it.copy(houseNumber = v)
            ApartmentFormField.POSTAL_CODE -> it.copy(postalCode = v)
            ApartmentFormField.CITY -> it.copy(city = v)
            ApartmentFormField.LIVING_SPACE -> it.copy(livingSpace = v)
            ApartmentFormField.OCCUPANTS -> it.copy(occupants = v)
            ApartmentFormField.TENANCY_START -> it.copy(tenancyStart = v)
            ApartmentFormField.UTILITY_ADVANCE -> it.copy(utilityAdvance = v)
            ApartmentFormField.HEATING_ADVANCE -> it.copy(heatingAdvance = v)
            ApartmentFormField.LANDLORD_NAME -> it.copy(landlordName = v)
            ApartmentFormField.LANDLORD_EMAIL -> it.copy(landlordEmail = v)
            ApartmentFormField.LANDLORD_PHONE -> it.copy(landlordPhone = v)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun save() {
        val s = _state.value
        val errors = mutableMapOf<ApartmentField, String>()

        if (s.label.isBlank()) errors[ApartmentField.LABEL] = "Bitte eine Bezeichnung eingeben."
        if (s.street.isBlank()) errors[ApartmentField.STREET] = "Straße fehlt."
        if (s.houseNumber.isBlank()) errors[ApartmentField.HOUSE_NUMBER] = "Hausnummer fehlt."
        if (!s.postalCode.matches(Regex("\\d{5}"))) errors[ApartmentField.POSTAL_CODE] = "Bitte eine 5-stellige PLZ eingeben."
        if (s.city.isBlank()) errors[ApartmentField.CITY] = "Ort fehlt."

        val sqm = parseSqm(s.livingSpace)
        if (sqm == null) errors[ApartmentField.LIVING_SPACE] = "Bitte eine Wohnfläche größer 0 eingeben."

        val occupants = s.occupants.toIntOrNull()?.takeIf { it in 1..20 }
        if (occupants == null) errors[ApartmentField.OCCUPANTS] = "Bitte eine Zahl zwischen 1 und 20 eingeben."

        val start = runCatching { LocalDate.parse(s.tenancyStart.trim()) }.getOrNull()
        if (start == null) errors[ApartmentField.TENANCY_START] = "Bitte das Datum als JJJJ-MM-TT eingeben."

        val utility = parseEuroToCents(s.utilityAdvance)
        if (utility == null) errors[ApartmentField.UTILITY_ADVANCE] = "Bitte einen Betrag eingeben, z. B. 180,00."

        // Heizkosten-Vorauszahlung ist optional (leer = 0)
        val heating = if (s.heatingAdvance.isBlank()) 0L else parseEuroToCents(s.heatingAdvance)
        if (heating == null) errors[ApartmentField.HEATING_ADVANCE] = "Bitte einen Betrag eingeben, z. B. 90,00."

        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors) }
            return
        }

        val landlord = s.landlordName.takeIf { it.isNotBlank() }?.let {
            LandlordContact(
                id = LandlordContactId.new(),
                name = it.trim(),
                company = null,
                email = s.landlordEmail.takeIf { e -> e.isNotBlank() }?.trim(),
                phone = s.landlordPhone.takeIf { p -> p.isNotBlank() }?.trim(),
                postalAddress = null
            )
        }

        val now = Clock.System.now()
        val apartment = Apartment(
            id = ApartmentId.new(),
            label = s.label.trim(),
            address = Address(s.street.trim(), s.houseNumber.trim(), s.postalCode.trim(), s.city.trim()),
            livingSpaceSqm = sqm!!,
            numberOfOccupants = occupants!!,
            tenancyStartDate = start!!,
            monthlyUtilityAdvanceCents = utility!!,
            monthlyHeatingAdvanceCents = heating!!,
            landlordContactId = landlord?.id,
            createdAt = now,
            updatedAt = now
        )

        _state.update { it.copy(fieldErrors = emptyMap(), isSaving = true) }
        viewModelScope.launch {
            runCatching { saveApartment(apartment, landlord) }
                .onSuccess {
                    _state.update { it.copy(isSaving = false) }
                    _effects.send(ApartmentFormEffect.Saved(apartment.id.value))
                }
                .onFailure { e ->
                    _state.update { it.copy(isSaving = false) }
                    _effects.send(ApartmentFormEffect.ShowError(e.message ?: "Speichern fehlgeschlagen"))
                }
        }
    }
}