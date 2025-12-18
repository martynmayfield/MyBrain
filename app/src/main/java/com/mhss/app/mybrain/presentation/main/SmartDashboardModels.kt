package com.mhss.app.mybrain.presentation.main

import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.model.Task

enum class FocusType { TASK, EVENT }

data class FocusItem(
    val title: String,
    val reason: String,
    val type: FocusType,
    val task: Task? = null,
    val event: CalendarEvent? = null
)

data class FlowItem(
    val time: Long,
    val title: String,
    val type: FocusType,
    val task: Task? = null,
    val event: CalendarEvent? = null,
    val hint: String? = null
)

data class SmartDashboardData(
    val topFocus: List<FocusItem> = emptyList(),
    val flow: List<FlowItem> = emptyList(),
    val nudge: String? = null
)
