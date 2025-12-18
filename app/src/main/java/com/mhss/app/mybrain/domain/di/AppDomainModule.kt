package com.mhss.app.mybrain.domain.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.mhss.app.mybrain.domain")
class AppDomainModule

val appDomainModule = module {
    includes(AppDomainModule().module)
}
