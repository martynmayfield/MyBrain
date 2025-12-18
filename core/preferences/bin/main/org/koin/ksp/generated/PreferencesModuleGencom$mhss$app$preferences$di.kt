package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


public val com_mhss_app_preferences_di_PreferencesModule : Module get() = module {
	single() { _ -> com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase(preferenceRepository=get()) } 
	single() { _ -> com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase(preferenceRepository=get()) } 
	single() { _ -> com.mhss.app.preferences.data.repository.PreferenceRepositoryImpl(preferences=get(),ioDispatcher=get(qualifier=org.koin.core.qualifier.StringQualifier("ioDispatcher"))) } bind(com.mhss.app.preferences.domain.repository.PreferenceRepository::class)
}
public val com.mhss.app.preferences.di.PreferencesModule.module : org.koin.core.module.Module get() = com_mhss_app_preferences_di_PreferencesModule
