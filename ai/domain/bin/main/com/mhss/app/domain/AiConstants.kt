package com.mhss.app.domain

object AiConstants {
    const val OPENAI_BASE_URL = "https://api.openai.com/v1"
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
    const val GROK_BASE_URL = "https://api.x.ai/v1"
    const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/v1"

    const val OPENAI_DEFAULT_MODEL = "gpt-4o"
    const val GEMINI_DEFAULT_MODEL = "gemini-1.5-pro"
    const val GROK_DEFAULT_MODEL = "grok-beta"
    const val DEEPSEEK_DEFAULT_MODEL = "deepseek-chat"

    const val GEMINI_KEY_INFO_URL = "https://ai.google.dev/gemini-api/docs/api-key"
    const val OPENAI_KEY_INFO_URL = "https://platform.openai.com/api-keys"
    const val GROK_KEY_INFO_URL = "https://console.x.ai/"
    const val DEEPSEEK_KEY_INFO_URL = "https://platform.deepseek.com/api-keys"

    const val GEMINI_MODELS_INFO_URL = "https://ai.google.dev/gemini-api/docs/models/gemini"
    const val OPENAI_MODELS_INFO_URL = "https://platform.openai.com/docs/models"
    const val GROK_MODELS_INFO_URL = "https://docs.x.ai/docs#models"
    const val DEEPSEEK_MODELS_INFO_URL = "https://platform.deepseek.com/api-docs"
}

val systemMessage = """
    You are a personal AI assistant.
    You help users with their requests and provide detailed explanations if needed.
    Users might attach notes, tasks, or calendar events. Use this attached data as a context for your response.
""".trimIndent()


val String.summarizeNotePrompt: String
    get() = """
        Summarize this note in bullet points.
        Respond with the summary only and don't say anything else.
        Use Markdown for formatting.
        Respond using the same language as the original note language.
        Note content:
        $this
        Summary:
    """.trimIndent()

val String.autoFormatNotePrompt: String
    get() = """
        Format this note in a more readable way.
        Include headings, bullet points, and other formatting.
        Respond with the formatted note only and don't say anything else.
        Use Markdown for formatting.
        Respond using the same language as the original note language.
        Note content:
        $this
        Formatted note:
    """.trimIndent()

val String.correctSpellingNotePrompt: String
    get() = """
        Correct the spelling and grammar errors in this note.
        Respond with the corrected note only and don't say anything else.
        Respond using the same language as the original note language.
        Note content:
        $this
        Corrected note:
    """.trimIndent()

val String.autoGenerateTitlePrompt: String
    get() = """
        Generate a concise, descriptive title for this note based on its content.
        The title should be 3-8 words long and capture the main topic or idea.
        Respond with the title only and don't say anything else.
        Respond using the same language as the original note language.
        Note content:
        $this
        Title:
    """.trimIndent()