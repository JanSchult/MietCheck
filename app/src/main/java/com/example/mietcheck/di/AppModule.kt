package com.example.mietcheck.di

import com.example.shared.data.repositorys.InMemoryReminderRepository
import com.example.shared.data.repositorys.InMemoryUtilityBillRepository
import com.example.shared.domain.repository.ReminderRepository
import com.example.shared.domain.repository.UtilityBillRepository
import com.example.shared.domain.usecase.DeleteReminderUseCase
import com.example.shared.domain.usecase.GetUtilityBillUseCase
import com.example.shared.domain.usecase.MarkReminderCompletedUseCase
import com.example.shared.domain.usecase.ObserveRemindersUseCase
import com.example.shared.domain.usecase.SaveReminderUseCase
import com.example.shared.viewmodels.ReminderListViewModel
import com.example.shared.viewmodels.UtilityBillOverviewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single<ReminderRepository> { InMemoryReminderRepository() }
    single<UtilityBillRepository> { InMemoryUtilityBillRepository() }
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
}

val presentationModule = module {
    viewModel { ReminderListViewModel(get(), get(), get()) }
    viewModel { UtilityBillOverviewViewModel(get(),get()) }
}

val appModules = listOf(dataModule, useCaseModule, presentationModule)