package com.mhss.app.presentation.di

import com.mhss.app.presentation.NoteDetailsViewModel
import com.mhss.app.presentation.NotesViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val notePresentationModule: Module = module {
    viewModelOf(::NotesViewModel)
    viewModel { (id: Int) ->
        NoteDetailsViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(named("applicationScope")),
            id
        )
    }
}