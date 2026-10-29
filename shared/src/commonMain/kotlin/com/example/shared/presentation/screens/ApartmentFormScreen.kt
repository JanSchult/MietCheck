package com.example.shared.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.ApartmentField
import com.example.shared.domain.model.ApartmentFormField
import com.example.shared.presentation.apartment.ApartmentFormEffect
import com.example.shared.presentation.apartment.ApartmentFormIntent
import com.example.shared.presentation.componentes.FormField
import com.example.shared.presentation.componentes.SectionTitle
import com.example.shared.viewmodels.ApartmentFormViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ApartmentFormScreen(
    onSaved: () -> Unit,
    onShowError: (String) -> Unit,
    viewModel: ApartmentFormViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ApartmentFormEffect.Saved -> onSaved()
                is ApartmentFormEffect.ShowError -> onShowError(effect.message)
            }
        }
    }

    fun change(field: ApartmentFormField): (String) -> Unit = { value ->
        viewModel.onIntent(ApartmentFormIntent.FieldChanged(field, value))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Wohnung anlegen", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Deine Angaben bleiben auf diesem Gerät.",
            style = MaterialTheme.typography.bodyLarge
        )

        FormField("Bezeichnung (z. B. Wohnung Hannover)", state.label,
            state.fieldErrors[ApartmentField.LABEL], onChange = change(ApartmentFormField.LABEL))

        SectionTitle("Adresse")
        FormField("Straße", state.street, state.fieldErrors[ApartmentField.STREET],
            onChange = change(ApartmentFormField.STREET))
        FormField("Hausnummer", state.houseNumber, state.fieldErrors[ApartmentField.HOUSE_NUMBER],
            onChange = change(ApartmentFormField.HOUSE_NUMBER))
        FormField("PLZ", state.postalCode, state.fieldErrors[ApartmentField.POSTAL_CODE],
            KeyboardType.Number, change(ApartmentFormField.POSTAL_CODE))
        FormField("Ort", state.city, state.fieldErrors[ApartmentField.CITY],
            onChange = change(ApartmentFormField.CITY))

        SectionTitle("Mietverhältnis")
        FormField("Wohnfläche in m²", state.livingSpace, state.fieldErrors[ApartmentField.LIVING_SPACE],
            KeyboardType.Decimal, change(ApartmentFormField.LIVING_SPACE))
        FormField("Anzahl Bewohner", state.occupants, state.fieldErrors[ApartmentField.OCCUPANTS],
            KeyboardType.Number, change(ApartmentFormField.OCCUPANTS))
        FormField("Mietbeginn (JJJJ-MM-TT)", state.tenancyStart,
            state.fieldErrors[ApartmentField.TENANCY_START], KeyboardType.Number,
            change(ApartmentFormField.TENANCY_START))
        FormField("Nebenkosten-Vorauszahlung pro Monat in €", state.utilityAdvance,
            state.fieldErrors[ApartmentField.UTILITY_ADVANCE], KeyboardType.Decimal,
            change(ApartmentFormField.UTILITY_ADVANCE))
        FormField("Heizkosten-Vorauszahlung pro Monat in € (optional)", state.heatingAdvance,
            state.fieldErrors[ApartmentField.HEATING_ADVANCE], KeyboardType.Decimal,
            change(ApartmentFormField.HEATING_ADVANCE))

        SectionTitle("Vermieter oder Hausverwaltung (optional)")
        FormField("Name", state.landlordName, null, onChange = change(ApartmentFormField.LANDLORD_NAME))
        FormField("E-Mail", state.landlordEmail, null, KeyboardType.Email,
            change(ApartmentFormField.LANDLORD_EMAIL))
        FormField("Telefon", state.landlordPhone, null, KeyboardType.Phone,
            change(ApartmentFormField.LANDLORD_PHONE))

        Button(
            onClick = { viewModel.onIntent(ApartmentFormIntent.Save) },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) {
            Text(
                if (state.isSaving) "Wird gespeichert …" else "Speichern",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
