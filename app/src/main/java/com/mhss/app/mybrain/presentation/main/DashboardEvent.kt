package com.mhss.app.mybrain.presentation.main

import com.mhss.app.domain.model.Task


sealed class DashboardEvent {
    data class ReadPermissionChanged(val hasPermission: Boolean) : DashboardEvent()
    data class CompleteTask(val task: Task, val isCompleted: Boolean) : DashboardEvent()
    data class UndoComplete(val task: Task) : DashboardEvent()
    data object InitAll : DashboardEvent()
    data object RefreshSmart : DashboardEvent()
    data class SnoozeFocus(val title: String) : DashboardEvent()
    data class PostponeFocus(val title: String) : DashboardEvent()
    data object DismissNudge : DashboardEvent()
}