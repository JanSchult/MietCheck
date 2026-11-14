package com.example.shared.presentation.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.TenantField
import com.example.shared.presentation.componentes.FormField
import com.example.shared.viewmodels.TenantProfileViewModel
import com.example.shared.viewmodels.effects.TenantProfileEffect
import com.example.shared.viewmodels.intent.TenantProfileIntent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TenantProfileScreen(
    onSaved: () -> Unit,
    onShowError: (String) -> Unit,
    viewModel: TenantProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                TenantProfileEffect.Saved -> onSaved()
                is TenantProfileEffect.ShowError -> onShowError(effect.message)
            }
        }
    }

    if (state.isLoading) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) { CircularProgressIndicator() }
        return
    }

    fun change(field: TenantField): (String) -> Unit =
        { viewModel.onIntent(TenantProfileIntent.FieldChanged(field, it)) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Diese Angaben erscheinen als Absender in deinen Briefen und Mängelanzeigen. " +
                    "Sie bleiben auf diesem Gerät.",
            style = MaterialTheme.typography.bodyLarge
        )
        FormField("Vor- und Nachname", state.name, state.errors[TenantField.NAME], onChange = change(TenantField.NAME))
        FormField(
            "E-Mail (optional)", state.email, state.errors[TenantField.EMAIL],
            KeyboardType.Email, change(TenantField.EMAIL)
        )
        FormField(
            "Telefon (optional)", state.phone, state.errors[TenantField.PHONE],
            KeyboardType.Phone, change(TenantField.PHONE)
        )
        Button(
            onClick = { viewModel.onIntent(TenantProfileIntent.Save) },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) {
            Text(if (state.isSaving) "Wird gespeichert …" else "Speichern", style = MaterialTheme.typography.titleMedium)
        }
    }
}