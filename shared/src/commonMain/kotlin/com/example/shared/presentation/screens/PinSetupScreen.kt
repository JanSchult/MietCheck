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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.lock.PIN_LENGTH
import com.example.shared.domain.lock.PinMode
import com.example.shared.domain.lock.PinStep
import com.example.shared.presentation.componentes.PinDots
import com.example.shared.presentation.componentes.PinPad
import com.example.shared.presentation.lock.PinSetupEffect
import com.example.shared.presentation.lock.PinSetupIntent
import com.example.shared.viewmodels.PinSetupViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PinSetupScreen(
    mode: PinMode,
    offerBiometric: Boolean,
    onDone: () -> Unit,
    viewModel: PinSetupViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(mode, offerBiometric) { viewModel.onIntent(PinSetupIntent.Init(mode, offerBiometric)) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect -> if (effect == PinSetupEffect.Done) onDone() }
    }

    val title = when (state.step) {
        PinStep.VERIFY_CURRENT -> "Aktuelle PIN eingeben"
        PinStep.ENTER_NEW -> "Neue PIN festlegen"
        PinStep.CONFIRM_NEW -> "PIN wiederholen"
        PinStep.OFFER_BIOMETRIC -> "Zusätzlich mit Fingerabdruck oder Gesicht entsperren?"
    }
    val hint = when (state.step) {
        PinStep.VERIFY_CURRENT ->
            if (state.mode == PinMode.REMOVE) "Zum Ausschalten der App-Sperre." else "Zum Ändern deiner PIN."
        PinStep.ENTER_NEW ->
            "Wähle $PIN_LENGTH Ziffern. Es gibt keine Wiederherstellung, merke sie dir gut."
        PinStep.CONFIRM_NEW -> "Gib dieselbe PIN noch einmal ein."
        PinStep.OFFER_BIOMETRIC -> "Die PIN bleibt immer als Alternative nutzbar."
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(hint, style = MaterialTheme.typography.bodyLarge)

        if (state.step == PinStep.OFFER_BIOMETRIC) {
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge) }
            Button(
                onClick = { viewModel.onIntent(PinSetupIntent.BiometricChoice(true)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text("Ja, aktivieren") }
            OutlinedButton(
                onClick = { viewModel.onIntent(PinSetupIntent.BiometricChoice(false)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) { Text("Nein, nur PIN") }
        } else {
            PinDots(state.digits)
            when {
                state.lockoutSeconds > 0 -> Text(
                    "Zu viele Versuche. Bitte warte noch ${formatLockout(state.lockoutSeconds)}.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                state.error != null -> Text(
                    state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            PinPad(
                enabled = !state.isBusy && state.lockoutSeconds == 0L,
                onDigit = { viewModel.onIntent(PinSetupIntent.Digit(it)) },
                onBackspace = { viewModel.onIntent(PinSetupIntent.Backspace) }
            )
        }
    }
}