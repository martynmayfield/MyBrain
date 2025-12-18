package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


internal val com_mhss_app_data_di_AiDataModule : Module get() = module {
	single(qualifier=org.koin.core.qualifier.StringQualifier("geminiApi")) { _ -> com.mhss.app.data.GeminiApi(client=get(),ioDispatcher=get(qualifier=org.koin.core.qualifier.StringQualifier("ioDispatcher"))) } bind(com.mhss.app.domain.repository.AiApi::class)
	single(qualifier=org.koin.core.qualifier.StringQualifier("ollamaApi")) { _ -> com.mhss.app.data.OllamaApi(client=get(),ioDispatcher=get(qualifier=org.koin.core.qualifier.StringQualifier("ioDispatcher"))) } bind(com.mhss.app.domain.repository.AiApi::class)
	single(qualifier=org.koin.core.qualifier.StringQualifier("openaiApi")) { _ -> com.mhss.app.data.OpenaiApi(client=get(),ioDispatcher=get(qualifier=org.koin.core.qualifier.StringQualifier("ioDispatcher"))) } bind(com.mhss.app.domain.repository.AiApi::class)
}
internal val com.mhss.app.data.di.AiDataModule.module : org.koin.core.module.Module get() = com_mhss_app_data_di_AiDataModule
