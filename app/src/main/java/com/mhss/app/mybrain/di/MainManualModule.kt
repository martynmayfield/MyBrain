package com.mhss.app.mybrain.di

import com.mhss.app.mybrain.presentation.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainPresentationModuleManual = module {
    viewModel {
        MainViewModel(
            get(), // GetPreferenceUseCase
            get(), // SavePreferenceUseCase
            get(), // GetAllTasksUseCase
            get(), // GetAllEntriesUseCase
            get(), // UpdateTaskCompletedUseCase
            get(), // GetAllEventsUseCase
            get(), // GetSmartDashboardItemsUseCase
            get()  // UpdateTaskUseCase
        )
    }
}
