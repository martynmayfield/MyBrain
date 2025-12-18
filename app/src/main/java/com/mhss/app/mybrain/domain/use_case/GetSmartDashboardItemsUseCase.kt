package com.mhss.app.mybrain.domain.use_case
import com.mhss.app.domain.use_case.GetAllEventsUseCase
import com.mhss.app.domain.use_case.GetAllTasksUseCase
import com.mhss.app.mybrain.presentation.main.FocusItem
import com.mhss.app.mybrain.presentation.main.FocusType
import com.mhss.app.mybrain.presentation.main.FlowItem
import com.mhss.app.mybrain.presentation.main.SmartDashboardData
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.preferences.domain.model.OrderType
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.annotation.Single
import org.koin.core.annotation.Named
import com.mhss.app.domain.repository.AiApi
import com.mhss.app.network.NetworkResult

@Single
class GetSmartDashboardItemsUseCase(
    private val getAllTasks: GetAllTasksUseCase,
    private val getAllEvents: GetAllEventsUseCase,
    @Named("ollamaApi") private val ollama: AiApi,
) {
    suspend operator fun invoke(): SmartDashboardData {
        val tasks = getAllTasks(Order.DateModified(OrderType.ASC)).first()
        val eventsMap = getAllEvents(emptyList()) { it.start.toString() }
        val events = eventsMap.values.flatten()

        val openTasks = tasks.filter { !it.isCompleted }
        val upcomingEvents = events.filter { it.end >= System.currentTimeMillis() }

        val itemsList = buildString {
            openTasks.forEach { append("- "+it.title+dueSuffix(it.dueDate)+"\n") }
            upcomingEvents.forEach { append("- "+it.title+dueSuffix(it.start)+"\n") }
        }

        val prompt = """
            Rank these items by immediate importance for today/tomorrow. Consider due dates, recurrence, patterns, inferred urgency, and context. Return only JSON with top 3: [{"title": "...", "reason": "...", "type": "task|event"}]
            Items:
            $itemsList
        """.trimIndent()

        val focusItems = when (val res = ollama.sendPrompt("http://127.0.0.1:11434", prompt, "", "")) {
            is NetworkResult.Success -> parseFocus(openTasks, upcomingEvents, res.data).take(3).ifEmpty {
                heuristicFocus(openTasks, upcomingEvents)
            }
            else -> heuristicFocus(openTasks, upcomingEvents)
        }

        val flow = (upcomingEvents.map {
            FlowItem(time = it.start, title = it.title, type = FocusType.EVENT, event = it, hint = timeHint(it.start))
        } + openTasks.map {
            FlowItem(time = it.dueDate, title = it.title, type = FocusType.TASK, task = it)
        }).sortedBy { it.time }

        val nudge = generateOptionalNudge(openTasks, upcomingEvents)

        return SmartDashboardData(
            topFocus = focusItems.take(3),
            flow = flow,
            nudge = nudge
        )
    }

    private fun parseFocus(tasks: List<com.mhss.app.domain.model.Task>, events: List<com.mhss.app.domain.model.CalendarEvent>, jsonText: String): List<FocusItem> {
        return try {
            val root = Json.parseToJsonElement(jsonText)
            val arr = if (root is kotlinx.serialization.json.JsonArray) root else root.jsonArray
            arr.mapNotNull { el ->
                val obj = el.jsonObject
                val title = obj["title"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val reason = obj["reason"]?.jsonPrimitive?.content ?: ""
                val typeStr = obj["type"]?.jsonPrimitive?.content ?: "task"
                val type = if (typeStr.lowercase() == "event") FocusType.EVENT else FocusType.TASK
                val task = tasks.find { it.title.equals(title, true) }
                val event = events.find { it.title.equals(title, true) }
                FocusItem(title = title, reason = reason, type = type, task = task, event = event)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun heuristicFocus(tasks: List<com.mhss.app.domain.model.Task>, events: List<com.mhss.app.domain.model.CalendarEvent>): List<FocusItem> {
        val now = System.currentTimeMillis()
        val urgentTasks = tasks
            .filter { it.dueDate > 0 }
            .sortedBy { it.dueDate }
            .take(2)
            .map { FocusItem(it.title, reason = "Due soon", type = FocusType.TASK, task = it) }
        val nextEvent = events
            .filter { it.start >= now }
            .sortedBy { it.start }
            .firstOrNull()
            ?.let { FocusItem(it.title, reason = "Upcoming event", type = FocusType.EVENT, event = it) }
        return (listOfNotNull(nextEvent) + urgentTasks).take(3)
    }

    private fun dueSuffix(time: Long): String {
        if (time <= 0L) return ""
        return " (" + java.time.Instant.ofEpochMilli(time).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().toString() + ")"
    }

    private fun timeHint(start: Long): String? {
        val delta = start - System.currentTimeMillis()
        if (delta in 1..(2 * 60 * 60 * 1000)) {
            val hours = delta / (60 * 60 * 1000)
            return "Starts in ${hours}h"
        }
        return null
    }

    private fun generateOptionalNudge(tasks: List<com.mhss.app.domain.model.Task>, events: List<com.mhss.app.domain.model.CalendarEvent>): String? {
        // Simple local heuristic nudge; can be replaced with Ollama call
        val hasFreeAfternoon = events.none { 
            val hour = java.time.Instant.ofEpochMilli(it.start).atZone(java.time.ZoneId.systemDefault()).hour
            hour in 13..17
        }
        val hasImportantTask = tasks.any { it.priority.value >= 2 && !it.isCompleted }
        return if (hasFreeAfternoon && hasImportantTask) "Free ~90 min this afternoon for deep work?" else null
    }
}
