package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


public val com_mhss_app_domain_di_SettingsDomainModule : Module get() = module {
	single() { _ -> com.mhss.app.domain.use_case.ImportAllDataUseCase(repository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.ExportAllDataUseCase(repository=get()) } 
}
public val com.mhss.app.domain.di.SettingsDomainModule.module : org.koin.core.module.Module get() = com_mhss_app_domain_di_SettingsDomainModule
