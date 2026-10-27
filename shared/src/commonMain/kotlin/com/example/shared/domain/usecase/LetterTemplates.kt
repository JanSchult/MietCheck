package com.example.shared.domain.usecase

import com.example.shared.domain.model.LetterTemplateType

/**
 * Neutrale Vorlagen. Bewusst keine Fristsetzungen, keine Drohungen,
 * keine rechtlichen Wertungen. Platzhalter: {{...}}
 */
object LetterTemplates {

    private const val HEADER = """{{senderName}}
{{senderAddress}}

{{recipientName}}
{{recipientAddress}}

{{date}}

"""

    private const val FOOTER = """

Vielen Dank für Ihre Mühe und freundliche Grüße

{{signature}}"""

    data class Template(val subject: String, val body: String)

    fun get(type: LetterTemplateType): Template = when (type) {
        LetterTemplateType.REQUEST_EXPLANATION -> Template(
            subject = "Rückfrage zur Nebenkostenabrechnung {{billingPeriod}} – {{apartmentAddress}}",
            body = HEADER + """Sehr geehrte Damen und Herren,

zu meiner Nebenkostenabrechnung für den Zeitraum {{billingPeriod}} (Wohnung: {{apartmentAddress}}) habe ich eine Verständnisfrage:

{{concreteQuestion}}

Könnten Sie mir die betreffende Position bitte erläutern?""" + FOOTER
        )

        LetterTemplateType.REQUEST_RECEIPT_ACCESS -> Template(
            subject = "Bitte um Belegeinsicht – Abrechnung {{billingPeriod}}",
            body = HEADER + """Sehr geehrte Damen und Herren,

ich möchte die Nebenkostenabrechnung für {{billingPeriod}} (Wohnung: {{apartmentAddress}}) gern nachvollziehen und bitte Sie um Einsicht in die zugrunde liegenden Belege.

{{concreteQuestion}}

Bitte teilen Sie mir mit, wann und wie ich Einsicht nehmen kann.""" + FOOTER
        )

        LetterTemplateType.NOTE_CALCULATION_DISCREPANCY -> Template(
            subject = "Rechnerische Rückfrage – Abrechnung {{billingPeriod}}",
            body = HEADER + """Sehr geehrte Damen und Herren,

beim Nachrechnen der Abrechnung für {{billingPeriod}} (Wohnung: {{apartmentAddress}}) ist mir eine mögliche rechnerische Unstimmigkeit aufgefallen:

{{concreteQuestion}}

Bitte prüfen Sie die Angaben und teilen Sie mir mit, ob hier ein Versehen vorliegt.""" + FOOTER
        )

        LetterTemplateType.REQUEST_DISTRIBUTION_KEY -> Template(
            subject = "Frage zum Verteilerschlüssel – Abrechnung {{billingPeriod}}",
            body = HEADER + """Sehr geehrte Damen und Herren,

zur Abrechnung für {{billingPeriod}} (Wohnung: {{apartmentAddress}}) habe ich eine Frage zum angewendeten Verteilerschlüssel:

{{concreteQuestion}}

Könnten Sie mir bitte erläutern, nach welchem Schlüssel die Kosten verteilt wurden?""" + FOOTER
        )

        LetterTemplateType.REQUEST_CORRECTION_MISSING_DATA -> Template(
            subject = "Bitte um Ergänzung fehlender Angaben – Abrechnung {{billingPeriod}}",
            body = HEADER + """Sehr geehrte Damen und Herren,

in der Abrechnung für {{billingPeriod}} (Wohnung: {{apartmentAddress}}) konnte ich einzelne Angaben nicht finden:

{{concreteQuestion}}

Könnten Sie die Abrechnung bitte entsprechend ergänzen oder die Angaben nachreichen?""" + FOOTER
        )

        LetterTemplateType.GENERAL_INQUIRY -> Template(
            subject = "Rückfrage zur Nebenkostenabrechnung {{billingPeriod}}",
            body = HEADER + """Sehr geehrte Damen und Herren,

zu meiner Nebenkostenabrechnung für {{billingPeriod}} (Wohnung: {{apartmentAddress}}) habe ich folgende Frage:

{{concreteQuestion}}""" + FOOTER
        )
    }
}