package com.example.shared.domain.usecase


import com.example.shared.domain.model.Address
import com.example.shared.domain.model.LandlordContact
import com.example.shared.domain.model.LetterContext
import com.example.shared.domain.model.ids.UtilityBillId
import com.example.shared.domain.repository.ApartmentRepository
import com.example.shared.domain.repository.UtilityBillRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

class BuildLetterContextUseCase(
    private val billRepository: UtilityBillRepository,
    private val apartmentRepository: ApartmentRepository
) {
    suspend operator fun invoke(billId: UtilityBillId): LetterContext? {
        val bill = billRepository.getById(billId) ?: return null
        val apartment = apartmentRepository.getById(bill.apartmentId)

        val landlordId = apartment?.landlordContactId
        val landlord: LandlordContact? =
            if (landlordId != null) apartmentRepository.getLandlord(landlordId) else null

        val apartmentAddress: String = apartment?.address?.format() ?: "[Adresse der Wohnung]"

        val recipientName: String = if (landlord != null) {
            listOfNotNull(landlord.company, landlord.name).joinToString("\n")
        } else {
            "[Vermieter / Hausverwaltung]"
        }

        val recipientAddress: String =
            landlord?.postalAddress?.format() ?: "[Adresse Vermieter]"

        return LetterContext(
            senderName = "[Dein Name]",
            senderAddress = apartmentAddress,
            recipientName = recipientName,
            recipientAddress = recipientAddress,
            apartmentAddress = apartmentAddress.replace("\n", ", "),
            billingPeriod = "${bill.billingPeriodStart.formatDe()} – ${bill.billingPeriodEnd.formatDe()}",
            concreteQuestion = ""
        )
    }
}

private fun Address.format(): String = "$street $houseNumber\n$postalCode $city"

private fun LocalDate.formatDe(): String =
    "${day.toString().padStart(2, '0')}.${month.number.toString().padStart(2, '0')}.$year"