package com.example.shared.presentation.componentes

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
 fun LegalNotice() {
    Text(
        "MietCheck führt eine technische und rechnerische Plausibilitätsprüfung durch. " +
                "Die Ergebnisse sind keine Rechtsberatung und ersetzen keine Prüfung durch einen " +
                "Mieterverein, eine Verbraucherzentrale oder eine qualifizierte Rechtsanwältin " +
                "beziehungsweise einen qualifizierten Rechtsanwalt.",
        style = MaterialTheme.typography.bodySmall
    )
}