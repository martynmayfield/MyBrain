package com.mhss.app.data

import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageType
import com.mhss.app.network.NetworkResult
import com.mhss.app.domain.repository.AiApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Serializable
data class OllamaGenerateRequest(
    val model: String = "qwen2.5:7b",
    val prompt: String,
    val stream: Boolean = false
)

@Serializable
data class OllamaGenerateResponse(
    val response: String
)

@Serializable
data class OllamaChatRequest(
    val model: String = "qwen2.5:7b",
    val messages: List<OllamaMessage>,
    val stream: Boolean = false
)

@Serializable
data class OllamaMessage(
    val role: String,
    val content: String
)

@Serializable
data class OllamaChatResponse(
    val message: OllamaMessage
)

@Single
@Named("ollamaApi")
class OllamaApi(
    private val client: HttpClient,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : AiApi {
    private val baseUrl = "http://127.0.0.1:11434"

    override suspend fun sendPrompt(baseUrl: String, prompt: String, model: String, key: String)
            : NetworkResult<String> {
        return withContext(ioDispatcher) {
            try {
                val response = client.post(baseUrl) {
                    url {
                        appendPathSegments("api", "generate")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(OllamaGenerateRequest(prompt = prompt))
                }
                val body = response.body<OllamaGenerateResponse>()
                NetworkResult.Success(body.response)
            } catch (e: Exception) {
                NetworkResult.OtherError(e.message ?: "Unknown error")
            }
        }
    }

    override suspend fun sendMessage(baseUrl: String, messages: List<AiMessage>, model: String, key: String)
            : NetworkResult<AiMessage> {
        return withContext(ioDispatcher) {
            try {
                val ollamaMessages = messages.map { msg ->
                    OllamaMessage(
                        role = if (msg.type == AiMessageType.USER) "user" else "assistant",
                        content = msg.content
                    )
                }
                val response = client.post(baseUrl) {
                    url {
                        appendPathSegments("api", "chat")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(OllamaChatRequest(messages = ollamaMessages))
                }
                val body = response.body<OllamaChatResponse>()
                val aiMessage = AiMessage(
                    content = body.message.content,
                    type = AiMessageType.MODEL,
                    time = System.currentTimeMillis()
                )
                NetworkResult.Success(aiMessage)
            } catch (e: Exception) {
                NetworkResult.OtherError(e.message ?: "Unknown error")
            }
        }
    }
}