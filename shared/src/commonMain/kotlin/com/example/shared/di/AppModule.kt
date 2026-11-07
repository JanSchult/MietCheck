package com.example.shared.di

import app.cash.sqldelight.db.SqlDriver
import com.example.shared.data.local.MietCheckDatabase
import com.example.shared.data.repositorys.SqlDelightApartmentRepository
import com.example.shared.data.repositorys.SqlDelightAppLockRepository
import com.example.shared.data.repositorys.SqlDelightDefectRepository
import com.example.shared.data.repositorys.SqlDelightDocumentRepository
import com.example.shared.data.repositorys.SqlDelightReminderRepository
import com.example.shared.data.repositorys.SqlDelightSelectedApartmentRepository
import com.example.shared.data.repositorys.SqlDelightUtilityBillRepository
import com.example.shared.domain.lock.AppLockManager
import com.example.shared.domain.lock.AppLockRepository
import com.example.shared.domain.repository.ApartmentRepository
import com.example.shared.domain.repository.DefectRepository
import com.example.shared.domain.repository.DocumentRepository
import com.example.shared.domain.repository.ReminderRepository
import com.example.shared.domain.repository.SelectedApartmentRepository
import com.example.shared.domain.repository.UtilityBillRepository
import com.example.shared.domain.usecase.AddCommunicationUseCase
import com.example.shared.domain.usecase.BuildBillOverviewUseCase
import com.example.shared.domain.usecase.BuildLetterContextUseCase
import com.example.shared.domain.usecase.BuildLetterDraftUseCase
import com.example.shared.domain.usecase.DeleteApartmentUseCase
import com.example.shared.domain.usecase.DeleteDefectUseCase
import com.example.shared.domain.usecase.DeleteDocumentUseCase
import com.example.shared.domain.usecase.DeleteReminderUseCase
import com.example.shared.domain.usecase.GetApartmentUseCase
import com.example.shared.domain.usecase.GetDocumentUseCase
import com.example.shared.domain.usecase.GetOpenChecksUseCase
import com.example.shared.domain.usecase.GetUtilityBillUseCase
import com.example.shared.domain.usecase.ImportDocumentUseCase
import com.example.shared.domain.usecase.MarkReminderCompletedUseCase
import com.example.shared.domain.usecase.ObserveApartmentsUseCase
import com.example.shared.domain.usecase.ObserveBillsUseCase
import com.example.shared.domain.usecase.ObserveDefectUseCase
import com.example.shared.domain.usecase.ObserveDefectsUseCase
import com.example.shared.domain.usecase.ObserveDocumentsUseCase
import com.example.shared.domain.usecase.ObserveRecentDocumentsUseCase
import com.example.shared.domain.usecase.ObserveRemindersUseCase
import com.example.shared.domain.usecase.RunValidationUseCase
import com.example.shared.domain.usecase.SaveApartmentUseCase
import com.example.shared.domain.usecase.SaveDefectUseCase
import com.example.shared.domain.usecase.SaveReminderUseCase
import com.example.shared.domain.usecase.SaveUtilityBillUseCase
import com.example.shared.domain.usecase.ShareDocumentUseCase
import com.example.shared.domain.usecase.UpdateDefectStatusUseCase
import com.example.shared.domain.usecase.UpdateDocumentUseCase
import com.example.shared.validation.AdvancePaymentMultiplicationRule
import com.example.shared.validation.BillingPeriodLengthRule
import com.example.shared.validation.DuplicateCostItemsRule
import com.example.shared.validation.IssueDateAfterPeriodEndRule
import com.example.shared.validation.LivingSpacePresentRule
import com.example.shared.validation.ValidationEngine
import com.example.shared.viewmodels.ApartmentFormViewModel
import com.example.shared.viewmodels.ChecksViewModel
import com.example.shared.viewmodels.DashboardViewModel
import com.example.shared.viewmodels.DefectDetailViewModel
import com.example.shared.viewmodels.DefectFormViewModel
import com.example.shared.viewmodels.DefectsViewModel
import com.example.shared.viewmodels.DocumentDetailViewModel
import com.example.shared.viewmodels.DocumentsViewModel
import com.example.shared.viewmodels.LetterEditorViewModel
import com.example.shared.viewmodels.LockViewModel
import com.example.shared.viewmodels.OnboardingViewModel
import com.example.shared.viewmodels.PinSetupViewModel
import com.example.shared.viewmodels.ReminderFormViewModel
import com.example.shared.viewmodels.ReminderListViewModel
import com.example.shared.viewmodels.SettingsViewModel
import com.example.shared.viewmodels.UtilityBillFormViewModel
import com.example.shared.viewmodels.UtilityBillOverviewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single { MietCheckDatabase(get<SqlDriver>()) }
    single<ApartmentRepository> { SqlDelightApartmentRepository(get()) }
    single<UtilityBillRepository> { SqlDelightUtilityBillRepository(get()) }
    single<ReminderRepository> { SqlDelightReminderRepository(get()) }
    single<DocumentRepository> { SqlDelightDocumentRepository(get()) }
    single<SelectedApartmentRepository> { SqlDelightSelectedApartmentRepository(get()) }
    single<DefectRepository> { SqlDelightDefectRepository(get()) }
    single<AppLockRepository> { SqlDelightAppLockRepository(get()) }
    single { AppLockManager(get(), get()) }

}

val validationModule = module {
    single {
        ValidationEngine(
            listOf(
                BillingPeriodLengthRule,
                IssueDateAfterPeriodEndRule,
                AdvancePaymentMultiplicationRule,
                LivingSpacePresentRule,
                DuplicateCostItemsRule,
            )
        )
    }
    factory { RunValidationUseCase(get(), get()) }
}

val useCaseModule = module {
    // Wohnung
    factory { ObserveApartmentsUseCase(get()) }
    factory { GetApartmentUseCase(get()) }
    factory { SaveApartmentUseCase(get()) }
    factory { DeleteApartmentUseCase(get()) }
    // Abrechnung
    factory { GetUtilityBillUseCase(get()) }
    factory { SaveUtilityBillUseCase(get()) }
    factory { ObserveBillsUseCase(get()) }
    factory { BuildBillOverviewUseCase(get(), get()) }
    factory { GetOpenChecksUseCase(get(), get()) }
    // Fristen
    factory { ObserveRemindersUseCase(get()) }
    factory { SaveReminderUseCase(get()) }
    factory { MarkReminderCompletedUseCase(get()) }
    factory { DeleteReminderUseCase(get()) }
    // Dokumente
    factory { ObserveRecentDocumentsUseCase(get()) }
    // Schreiben
    factory { BuildLetterContextUseCase(get(), get()) }
    factory { BuildLetterDraftUseCase() }
    // Mängel
    factory { ObserveDefectsUseCase(get()) }
    factory { ObserveDefectUseCase(get()) }
    factory { SaveDefectUseCase(get()) }
    factory { UpdateDefectStatusUseCase(get()) }
    factory { AddCommunicationUseCase(get()) }
    factory { DeleteDefectUseCase(get()) }
    factory { ObserveDocumentsUseCase(get()) }
    factory { GetDocumentUseCase(get()) }
    factory { ImportDocumentUseCase(get(), get()) }
    factory { UpdateDocumentUseCase(get()) }
    factory { DeleteDocumentUseCase(get(), get()) }
    factory { ShareDocumentUseCase(get(), get(), get()) }

}

val presentationModule = module {
    viewModel { DashboardViewModel(get(), get(), get(), get(), get()) }
    viewModel { ApartmentFormViewModel(get(), get()) }
    viewModel { UtilityBillFormViewModel(get(), get()) }
    viewModel { UtilityBillOverviewViewModel(get(), get()) }
    viewModel { ChecksViewModel(get(), get(), get()) }
    viewModel { LetterEditorViewModel(get(), get()) }
    viewModel { ReminderListViewModel(get(), get(), get()) }
    viewModel { ReminderFormViewModel(get()) }
    viewModel { DefectsViewModel(get(), get()) }
    viewModel { DefectFormViewModel(get(), get()) }
    viewModel { DefectDetailViewModel(get(), get(), get(), get()) }
    viewModel { DocumentsViewModel(get(), get(), get()) }
    viewModel { DocumentDetailViewModel(get(), get(), get(), get(), get()) }
    viewModel { LockViewModel(get(), get(), get()) }
    viewModel { PinSetupViewModel(get(), get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
}

val appModules = listOf(
    dataModule,
    useCaseModule,
    validationModule,
    presentationModule,
    platformModule
)