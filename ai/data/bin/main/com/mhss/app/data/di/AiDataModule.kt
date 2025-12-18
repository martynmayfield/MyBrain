package com.mhss.app.data.di

import com.mhss.app.data.OllamaApi
import com.mhss.app.data.OpenaiApi
import com.mhss.app.domain.di.AiDomainModule
import com.mhss.app.domain.repository.AiApi
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.mhss.app.data")
internal class AiDataModule

val aiDataModule = module {
    includes(AiDataModule().module, AiDomainModule().module)
    single<AiApi>(named("ollamaApi")) { OllamaApi(get(), get()) }
    single<AiApi>(named("openaiApi")) { OpenaiApi(get(), get()) }
}