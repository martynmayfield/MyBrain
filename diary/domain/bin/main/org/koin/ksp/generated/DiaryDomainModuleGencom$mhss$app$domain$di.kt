package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


public val com_mhss_app_domain_di_DiaryDomainModule : Module get() = module {
	single() { _ -> com.mhss.app.domain.use_case.GetAllEntriesUseCase(diaryRepository=get(),defaultDispatcher=get(qualifier=org.koin.core.qualifier.StringQualifier("defaultDispatcher"))) } 
	single() { _ -> com.mhss.app.domain.use_case.GetDiaryEntryUseCase(diaryRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.UpdateDiaryEntryUseCase(diaryRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.GetDiaryForChartUseCase(diaryRepository=get(),defaultDispatcher=get(qualifier=org.koin.core.qualifier.StringQualifier("defaultDispatcher"))) } 
	single() { _ -> com.mhss.app.domain.use_case.SearchEntriesUseCase(repository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.DeleteDiaryEntryUseCase(diaryRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.AddDiaryEntryUseCase(diaryRepository=get()) } 
}
public val com.mhss.app.domain.di.DiaryDomainModule.module : org.koin.core.module.Module get() = com_mhss_app_domain_di_DiaryDomainModule
