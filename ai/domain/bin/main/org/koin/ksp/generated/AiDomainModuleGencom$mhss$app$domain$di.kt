package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


public val com_mhss_app_domain_di_AiDomainModule : Module get() = module {
	single() { _ -> com.mhss.app.domain.use_case.SendAiMessageUseCase(openai=get(qualifier=org.koin.core.qualifier.StringQualifier("openaiApi")),gemini=get(qualifier=org.koin.core.qualifier.StringQualifier("geminiApi"))) } 
	single() { _ -> com.mhss.app.domain.use_case.SendAiPromptUseCase(ollama=get(qualifier=org.koin.core.qualifier.StringQualifier("ollamaApi")),openai=get(qualifier=org.koin.core.qualifier.StringQualifier("openaiApi")),getPreference=get()) } 
}
public val com.mhss.app.domain.di.AiDomainModule.module : org.koin.core.module.Module get() = com_mhss_app_domain_di_AiDomainModule
