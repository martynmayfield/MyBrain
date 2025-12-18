package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


public val com_mhss_app_alarm_di_AlarmModule : Module get() = module {
	single() { _ -> com.mhss.app.alarm.use_case.AddAlarmUseCase(alarmRepository=get(),alarmScheduler=get()) } 
	single() { _ -> com.mhss.app.alarm.use_case.DeleteAlarmUseCase(alarmRepository=get(),alarmScheduler=get()) } 
	single() { _ -> com.mhss.app.alarm.use_case.GetAllAlarmsUseCase(alarmRepository=get()) } 
}
public val com.mhss.app.alarm.di.AlarmModule.module : org.koin.core.module.Module get() = com_mhss_app_alarm_di_AlarmModule
