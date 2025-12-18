package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


public val com_mhss_app_domain_di_CalendarDomainModule : Module get() = module {
	single() { _ -> com.mhss.app.domain.use_case.UpdateCalendarEventUseCase(calendarRepository=get(),widgetUpdater=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.GetAllEventsUseCase(calendarRepository=get(),defaultDispatcher=get(qualifier=org.koin.core.qualifier.StringQualifier("defaultDispatcher"))) } 
	single() { _ -> com.mhss.app.domain.use_case.AddCalendarEventUseCase(calendarEventRepository=get(),widgetUpdater=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.DeleteCalendarEventUseCase(calendarRepository=get(),widgetUpdater=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.GetAllCalendarsUseCase(calendarRepository=get(),defaultDispatcher=get(qualifier=org.koin.core.qualifier.StringQualifier("defaultDispatcher"))) } 
}
public val com.mhss.app.domain.di.CalendarDomainModule.module : org.koin.core.module.Module get() = com_mhss_app_domain_di_CalendarDomainModule
