package com.mhss.app.domain.use_case

import com.mhss.app.network.NetworkResult
import com.mhss.app.domain.AiConstants
import com.mhss.app.domain.repository.AiApi
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.PrefsConstants
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.io.IOException

@Single
class SendAiPromptUseCase(
    @Named("ollamaApi") private val ollama: AiApi,
    @Named("openaiApi") private val openai: AiApi,
    private val getPreference: GetPreferenceUseCase
) {
    suspend operator fun invoke(
        prompt: String
    ): NetworkResult<String> {
        return try {
            val providerId = getPreference(intPreferencesKey(PrefsConstants.AI_PROVIDER_KEY), AiProvider.None.id).first()
            val provider = AiProvider.entries.find { it.id == providerId } ?: AiProvider.None

            when (provider) {
                AiProvider.OpenAI -> {
                    val useCustomUrl = getPreference(booleanPreferencesKey(PrefsConstants.OPENAI_USE_URL_KEY), false).first()
                    val baseUrl = if (useCustomUrl) {
                        getPreference(stringPreferencesKey(PrefsConstants.OPENAI_URL_KEY), "").first()
                    } else {
                        AiConstants.OPENAI_BASE_URL
                    }
                    val model = getPreference(stringPreferencesKey(PrefsConstants.OPENAI_MODEL_KEY), AiConstants.OPENAI_DEFAULT_MODEL).first()
                    val key = getPreference(stringPreferencesKey(PrefsConstants.OPENAI_KEY), "").first()
                    openai.sendPrompt(baseUrl, prompt, model, key)
                }
                AiProvider.Gemini -> {
                    val baseUrl = AiConstants.GEMINI_BASE_URL
                    val model = getPreference(stringPreferencesKey(PrefsConstants.GEMINI_MODEL_KEY), AiConstants.GEMINI_DEFAULT_MODEL).first()
                    val key = getPreference(stringPreferencesKey(PrefsConstants.GEMINI_KEY), "").first()
                    openai.sendPrompt(baseUrl, prompt, model, key) // Assuming OpenaiApi handles Gemini, but probably need GeminiApi
                }
                AiProvider.Grok -> {
                    val baseUrl = AiConstants.GROK_BASE_URL
                    val model = getPreference(stringPreferencesKey(PrefsConstants.GROK_MODEL_KEY), AiConstants.GROK_DEFAULT_MODEL).first()
                    val key = getPreference(stringPreferencesKey(PrefsConstants.GROK_KEY), "").first()
                    openai.sendPrompt(baseUrl, prompt, model, key)
                }
                AiProvider.DeepSeek -> {
                    val baseUrl = AiConstants.DEEPSEEK_BASE_URL
                    val model = getPreference(stringPreferencesKey(PrefsConstants.DEEPSEEK_MODEL_KEY), AiConstants.DEEPSEEK_DEFAULT_MODEL).first()
                    val key = getPreference(stringPreferencesKey(PrefsConstants.DEEPSEEK_KEY), "").first()
                    openai.sendPrompt(baseUrl, prompt, model, key)
                }
                AiProvider.None -> {
                    ollama.sendPrompt("http://127.0.0.1:11434", prompt, "", "")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            NetworkResult.InternetError
        } catch (e: Exception) {
            e.printStackTrace()
            NetworkResult.OtherError()
        }
    }
}