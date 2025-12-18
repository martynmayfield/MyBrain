package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


public val com_mhss_app_domain_di_TasksDomainModule : Module get() = module {
	single() { _ -> com.mhss.app.domain.use_case.UpdateTaskCompletedUseCase(tasksRepository=get(),deleteAlarm=get(),widgetUpdater=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.UpdateTaskUseCase(tasksRepository=get(),addAlarm=get(),deleteAlarm=get(),widgetUpdater=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.AddTaskUseCase(tasksRepository=get(),addAlarm=get(),updateTask=get(),widgetUpdater=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.GetTaskByIdUseCase(tasksRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.SearchTasksUseCase(tasksRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.DeleteTaskUseCase(taskRepository=get(),deleteAlarm=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.GetAllTasksUseCase(tasksRepository=get(),defaultDispatcher=get(qualifier=org.koin.core.qualifier.StringQualifier("defaultDispatcher"))) } 
}
public val com.mhss.app.domain.di.TasksDomainModule.module : org.koin.core.module.Module get() = com_mhss_app_domain_di_TasksDomainModule
