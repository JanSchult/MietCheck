package com.example.shared.di

import com.example.shared.data.repositorys.InMemoryApartmentRepository
import com.example.shared.data.repositorys.InMemoryDocumentRepository
import com.example.shared.data.repositorys.InMemoryReminderRepository
import com.example.shared.data.repositorys.InMemoryUtilityBillRepository
import com.example.shared.domain.repository.ApartmentRepository
import com.example.shared.domain.repository.DocumentRepository
import com.example.shared.domain.repository.ReminderRepository
import com.example.shared.domain.repository.UtilityBillRepository
import com.example.shared.domain.usecase.BuildLetterContextUseCase
import com.example.shared.domain.usecase.BuildLetterDraftUseCase
import com.example.shared.domain.usecase.DeleteApartmentUseCase
import com.example.shared.domain.usecase.DeleteReminderUseCase
import com.example.shared.domain.usecase.GetApartmentUseCase
import com.example.shared.domain.usecase.GetOpenChecksUseCase
import com.example.shared.domain.usecase.GetUtilityBillUseCase
import com.example.shared.domain.usecase.MarkReminderCompletedUseCase
import com.example.shared.domain.usecase.ObserveApartmentsUseCase
import com.example.shared.domain.usecase.ObserveRecentDocumentsUseCase
import com.example.shared.domain.usecase.ObserveRemindersUseCase
import com.example.shared.domain.usecase.SaveApartmentUseCase
import com.example.shared.domain.usecase.SaveReminderUseCase
import com.example.shared.viewmodels.ApartmentFormViewModel
import com.example.shared.viewmodels.DashboardViewModel
import com.example.shared.viewmodels.LetterEditorViewModel
import com.example.shared.viewmodels.ReminderListViewModel
import com.example.shared.viewmodels.UtilityBillOverviewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single<ReminderRepository> { InMemoryReminderRepository() }
    single<UtilityBillRepository> { InMemoryUtilityBillRepository() }
    single<ApartmentRepository> { InMemoryApartmentRepository() }
    single<DocumentRepository> { InMemoryDocumentRepository() }

    // TODO: weitere Repositories hier registrieren (ApartmentRepository,
    // DocumentRepository, UtilityBillRepository, DefectRepository, ...)
    // sobald ihre SQLDelight-Queries stehen.
}

val useCaseModule = module {
    factory { ObserveRemindersUseCase(get()) }
    factory { SaveReminderUseCase(get()) }
    factory { MarkReminderCompletedUseCase(get()) }
    factory { DeleteReminderUseCase(get()) }
    factory { GetUtilityBillUseCase(get()) }
    factory { BuildLetterDraftUseCase() }
    factory { ObserveApartmentsUseCase(get()) }
    factory { GetApartmentUseCase(get()) }
    factory { SaveApartmentUseCase(get()) }
    factory { DeleteApartmentUseCase(get()) }
    factory { BuildLetterContextUseCase(get(), get()) }
    factory { ObserveRecentDocumentsUseCase(get()) }
    factory { GetOpenChecksUseCase(get(), get()) }

}

val presentationModule = module {
    viewModel { ReminderListViewModel(get(), get(), get()) }
    viewModel { UtilityBillOverviewViewModel(get(), get()) }
    viewModel { LetterEditorViewModel(get(), get()) }
    viewModel { ApartmentFormViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get(), get()) }
}

val appModules = listOf(dataModule, useCaseModule, presentationModule)