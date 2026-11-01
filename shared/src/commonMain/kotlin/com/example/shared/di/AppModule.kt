package com.example.shared.di

import app.cash.sqldelight.db.SqlDriver
import com.example.shared.data.local.MietCheckDatabase
import com.example.shared.data.repositorys.SqlDelightApartmentRepository
import com.example.shared.data.repositorys.SqlDelightDocumentRepository
import com.example.shared.data.repositorys.SqlDelightReminderRepository
import com.example.shared.data.repositorys.SqlDelightSelectedApartmentRepository
import com.example.shared.data.repositorys.SqlDelightUtilityBillRepository
import com.example.shared.domain.repository.ApartmentRepository
import com.example.shared.domain.repository.DocumentRepository
import com.example.shared.domain.repository.ReminderRepository
import com.example.shared.domain.repository.SelectedApartmentRepository
import com.example.shared.domain.repository.UtilityBillRepository
import com.example.shared.domain.usecase.BuildBillOverviewUseCase
import com.example.shared.domain.usecase.BuildLetterContextUseCase
import com.example.shared.domain.usecase.BuildLetterDraftUseCase
import com.example.shared.domain.usecase.DeleteApartmentUseCase
import com.example.shared.domain.usecase.DeleteReminderUseCase
import com.example.shared.domain.usecase.GetApartmentUseCase
import com.example.shared.domain.usecase.GetOpenChecksUseCase
import com.example.shared.domain.usecase.GetUtilityBillUseCase
import com.example.shared.domain.usecase.MarkReminderCompletedUseCase
import com.example.shared.domain.usecase.ObserveApartmentsUseCase
import com.example.shared.domain.usecase.ObserveBillsUseCase
import com.example.shared.domain.usecase.ObserveRecentDocumentsUseCase
import com.example.shared.domain.usecase.ObserveRemindersUseCase
import com.example.shared.domain.usecase.SaveApartmentUseCase
import com.example.shared.domain.usecase.SaveReminderUseCase
import com.example.shared.domain.usecase.SaveUtilityBillUseCase
import com.example.shared.viewmodels.ApartmentFormViewModel
import com.example.shared.viewmodels.ChecksViewModel
import com.example.shared.viewmodels.DashboardViewModel
import com.example.shared.viewmodels.LetterEditorViewModel
import com.example.shared.viewmodels.ReminderFormViewModel
import com.example.shared.viewmodels.ReminderListViewModel
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
    factory { SaveUtilityBillUseCase(get()) }
    factory { BuildBillOverviewUseCase(get(), get()) }
    factory { ObserveBillsUseCase(get()) }


}

val presentationModule = module {
    viewModel { ReminderListViewModel(get(), get(), get()) }
    viewModel { UtilityBillOverviewViewModel(get(), get()) }
    viewModel { LetterEditorViewModel(get(), get()) }
    viewModel { ApartmentFormViewModel(get(),get()) }
    viewModel { DashboardViewModel(get(), get(), get(), get(),get()) }
    viewModel { ReminderListViewModel(get(), get(), get()) }
    viewModel { ReminderFormViewModel(get()) }
    viewModel { ChecksViewModel(get(), get(), get()) }
}

val appModules = listOf(dataModule, useCaseModule, presentationModule,platformModule)