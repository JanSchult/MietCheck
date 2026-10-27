package com.example.shared.domain.usecase

import com.example.shared.domain.model.LetterContext
import com.example.shared.domain.model.LetterDraft
import com.example.shared.domain.model.LetterTemplateType

/**
 * Füllt die Platzhalter. Datum und Unterschrift bleiben bewusst als
 * Platzhalter stehen – der Nutzer setzt sie beim Bearbeiten selbst.
 * Es wird nichts automatisch versendet.
 */
class BuildLetterDraftUseCase {

    operator fun invoke(type: LetterTemplateType, context: LetterContext): LetterDraft {
        val template = LetterTemplates.get(type)
        val values = mapOf(
            "senderName" to context.senderName,
            "senderAddress" to context.senderAddress,
            "recipientName" to context.recipientName,
            "recipientAddress" to context.recipientAddress,
            "apartmentAddress" to context.apartmentAddress,
            "billingPeriod" to context.billingPeriod,
            "concreteQuestion" to context.concreteQuestion.ifBlank { "[Hier deine Frage einfügen]" }
        )
        return LetterDraft(
            templateType = type,
            subject = fill(template.subject, values),
            body = fill(template.body, values)
        )
    }

    private fun fill(text: String, values: Map<String, String>): String =
        values.entries.fold(text) { acc, (key, value) ->
            acc.replace("{{$key}}", value)
        }
    // {{date}} und {{signature}} bleiben absichtlich unersetzt.
}