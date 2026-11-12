package com.example.shared.presentation.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shared.domain.lock.PinMode
import com.example.shared.viewmodels.OnboardingViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = koinViewModel()) {
    var stage by rememberSaveable { mutableStateOf(0) }
    var showPrivacy by rememberSaveable { mutableStateOf(false) }

    Scaffold { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                showPrivacy -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PrivacyContent()
                        OutlinedButton(
                            onClick = { showPrivacy = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                        ) {
                            Text("Zurück")
                        }
                    }
                }
                stage == 0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Willkommen bei MietCheck", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "MietCheck hilft dir, Nebenkostenabrechnungen, Dokumente und Mängel zu ordnen " +
                                    "und rechnerisch zu prüfen.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text("Dein Datenschutz", style = MaterialTheme.typography.titleLarge)
                        Text("• Deine Daten bleiben auf diesem Gerät und sind verschlüsselt gespeichert.", style = MaterialTheme.typography.bodyLarge)
                        Text("• Keine Registrierung, keine Werbung, keine Weitergabe an Dritte.", style = MaterialTheme.typography.bodyLarge)
                        Text("• MietCheck ersetzt keine Rechtsberatung.", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Mit einer PIN schützt du deine Dokumente auch dann, wenn jemand dein entsperrtes " +
                                    "Handy benutzt. Du kannst die App-Sperre jederzeit in den Einstellungen ändern.",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // Button zum Öffnen der Datenschutzerklärung
                        TextButton(
                            onClick = { showPrivacy = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Datenschutzerklärung lesen")
                        }

                        Button(
                            onClick = { stage = 1 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                        ) {
                            Text("PIN festlegen (empfohlen)", style = MaterialTheme.typography.titleMedium)
                        }

                        OutlinedButton(
                            onClick = viewModel::finish,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                        ) {
                            Text("Ohne App-Sperre fortfahren")
                        }
                    }
                }
                else -> {
                    PinSetupScreen(
                        mode = PinMode.SETUP,
                        offerBiometric = true,
                        onDone = viewModel::finish
                    )
                }
            }
        }
    }
}