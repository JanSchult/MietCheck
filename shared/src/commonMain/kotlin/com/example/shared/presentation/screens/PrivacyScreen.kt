package com.example.shared.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ENTWURF: vor der Veröffentlichung rechtlich prüfen lassen und die Platzhalter in [...] füllen.
// Der Text muss zum tatsächlichen Stand der App passen (Android/iOS getrennt prüfen).
private val PRIVACY_SECTIONS = listOf(
    "Verantwortlich" to
            "[Name, Anschrift und E-Mail-Adresse des Anbieters einfügen]",

    "Kurzfassung" to
            "MietCheck speichert deine Daten ausschließlich auf deinem Gerät. Es gibt keine Registrierung, " +
            "kein Konto, keine Werbung und keine Analyse- oder Tracking-Dienste. Deine Angaben und Dokumente " +
            "werden von der App nicht ins Internet gesendet und nicht an KI-Dienste übermittelt.",

    "Welche Daten die App verarbeitet" to
            "Nur Angaben, die du selbst eingibst oder importierst: Wohnung (Adresse, Fläche, Bewohner, Mietbeginn, " +
            "Vorauszahlungen), Kontaktdaten des Vermieters, Nebenkostenabrechnungen, Mängel samt Verlauf, Fristen " +
            "sowie importierte oder gescannte Dokumente.",

    "Wie die Daten geschützt werden" to
            "Die Datenbank ist verschlüsselt, Dokumente werden einzeln mit AES-256 verschlüsselt abgelegt. " +
            "Die Schlüssel liegen im geschützten Speicher des Geräts (Android Keystore). Deine PIN wird nur als " +
            "Hash gespeichert. Bildschirmfotos und die Vorschau im App-Wechsler sind standardmäßig blockiert. " +
            "Die App-Daten sind von der Cloud-Datensicherung des Geräts ausgenommen.",

    "Weitergabe" to
            "Die App gibt von sich aus keine Daten weiter. Wenn du ein Dokument, ein Schreiben oder einen Export " +
            "teilst, entscheidest du über den Empfänger. Zum Teilen liegt die Datei kurz unverschlüsselt im " +
            "Zwischenspeicher der App und wird spätestens beim nächsten Teilen oder Export entfernt. Was die " +
            "empfangende App mit der Datei macht, kann MietCheck nicht kontrollieren. Ein Export ist nicht verschlüsselt.",

    "Dokumentenscanner" to
            "Die Kamera-Funktion nutzt den Dokumentenscanner von Google (ML Kit, Google Play-Dienste). Laut Google " +
            "werden die Bilder auf dem Gerät verarbeitet. Google kann dabei technische Nutzungsdaten nach den " +
            "eigenen Datenschutzbestimmungen verarbeiten. [Aktuelle Angaben von Google prüfen und hier ergänzen]",

    "Berechtigungen" to
            "Die App benötigt keine besonderen Berechtigungen. Dateien wählst du selbst aus, Scans laufen über " +
            "den Google-Scanner.",

    "Deine Kontrolle" to
            "In den Einstellungen kannst du alle Daten exportieren und alle Daten unwiderruflich löschen. " +
            "Beim Deinstallieren der App werden ebenfalls alle Daten entfernt. Wenn du deine PIN vergisst, " +
            "ist keine Wiederherstellung möglich, weil keine Daten außerhalb deines Geräts liegen.",

    "Kein Rechtsrat" to
            "MietCheck führt eine technische und rechnerische Plausibilitätsprüfung durch. Die Ergebnisse sind " +
            "keine Rechtsberatung und ersetzen keine Prüfung durch einen Mieterverein, eine Verbraucherzentrale " +
            "oder eine qualifizierte Rechtsanwältin beziehungsweise einen qualifizierten Rechtsanwalt.",

    "Stand" to "[Datum einfügen]"
)

@Composable
fun PrivacyContent(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Datenschutzerklärung", style = MaterialTheme.typography.headlineSmall)
        PRIVACY_SECTIONS.forEach { (title, body) ->
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun PrivacyScreen() {
    PrivacyContent(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp))
}