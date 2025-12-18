package com.mhss.app.mybrain.presentation.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.use_case.GetAllEntriesUseCase
import com.mhss.app.domain.use_case.GetAllEventsUseCase
import com.mhss.app.domain.use_case.GetAllTasksUseCase
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.use_case.UpdateTaskCompletedUseCase
import com.mhss.app.domain.use_case.UpdateTaskUseCase
import com.mhss.app.preferences.domain.model.*
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import com.mhss.app.ui.FontSizeSettings
import com.mhss.app.ui.StartUpScreenSettings
import com.mhss.app.ui.ThemeSettings
import com.mhss.app.ui.theme.Rubik
import com.mhss.app.ui.toInt
import com.mhss.app.ui.toIntList
import com.mhss.app.util.date.formatDateForMapping
import com.mhss.app.util.date.inTheLastWeek
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlinx.coroutines.FlowPreview

@KoinViewModel
@OptIn(FlowPreview::class)
class MainViewModel(
    private val getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val getAllTasks: GetAllTasksUseCase,
    private val getAllEntriesUseCase: GetAllEntriesUseCase,
    private val completeTask: UpdateTaskCompletedUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getSmartDashboardItems: com.mhss.app.mybrain.domain.use_case.GetSmartDashboardItemsUseCase
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
    private set

    private var refreshTasksJob : Job? = null
    private var smartRefreshJob : Job? = null
    private val smartRefreshSignals = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val suppressedFocusTitles = mutableSetOf<String>()
    sealed interface UiEffect {
        data class Snackbar(val message: String, val actionLabel: String? = null, val actionEvent: DashboardEvent? = null) : UiEffect
    }
    private val _snack = MutableSharedFlow<UiEffect>(extraBufferCapacity = 1)
    val snack: SharedFlow<UiEffect> = _snack.asSharedFlow()

    val lockApp = getPreference(booleanPreferencesKey(PrefsConstants.LOCK_APP_KEY), false)
    val themeMode = getPreference(intPreferencesKey(PrefsConstants.SETTINGS_THEME_KEY), ThemeSettings.AUTO.value)
    val defaultStartUpScreen = getPreference(intPreferencesKey(PrefsConstants.DEFAULT_START_UP_SCREEN_KEY), StartUpScreenSettings.SPACES.value)
    val font = getPreference(intPreferencesKey(PrefsConstants.APP_FONT_KEY), Rubik.toInt())
    val fontSize = getPreference(intPreferencesKey(PrefsConstants.FONT_SIZE_KEY), FontSizeSettings.NORMAL.value)
    val blockScreenshots = getPreference(booleanPreferencesKey(PrefsConstants.BLOCK_SCREENSHOTS_KEY), false)
    val useMaterialYou = getPreference(booleanPreferencesKey(PrefsConstants.SETTINGS_MATERIAL_YOU), false)

    fun onDashboardEvent(event: DashboardEvent) {
        when(event) {
            is DashboardEvent.ReadPermissionChanged -> {
                if (event.hasPermission)
                    getCalendarEvents()
            }
            is DashboardEvent.CompleteTask -> viewModelScope.launch {
                completeTask(event.task.id, event.isCompleted)
                if (event.isCompleted) {
                    _snack.emit(UiEffect.Snackbar(
                        message = "Marked '${event.task.title}' complete",
                        actionLabel = "Undo",
                        actionEvent = DashboardEvent.UndoComplete(event.task)
                    ))
                } else {
                    _snack.emit(UiEffect.Snackbar(message = "Marked '${event.task.title}' incomplete"))
                }
            }
            is DashboardEvent.UndoComplete -> viewModelScope.launch {
                completeTask(event.task.id, false)
                _snack.emit(UiEffect.Snackbar(message = "Undid completion for '${event.task.title}'"))
            }
            DashboardEvent.InitAll -> {
                collectDashboardData()
                startSmartRefreshCollector()
                triggerSmartRefresh()
            }
            DashboardEvent.RefreshSmart -> refreshSmart()
            is DashboardEvent.SnoozeFocus -> {
                suppressedFocusTitles.add(event.title)
                uiState = uiState.copy(smartTopFocus = uiState.smartTopFocus.filter { it.title !in suppressedFocusTitles })
                viewModelScope.launch { _snack.emit(UiEffect.Snackbar("Snoozed '${event.title}' for now")) }
                triggerSmartRefresh()
            }
            is DashboardEvent.PostponeFocus -> {
                val item = uiState.smartTopFocus.find { it.title == event.title }
                val task = item?.task
                if (task != null && task.dueDate > 0L) {
                    val updated = task.copy(dueDate = task.dueDate + 24 * 60 * 60 * 1000)
                    viewModelScope.launch {
                        val ok = updateTask(updated, true)
                        if (ok) {
                            _snack.emit(UiEffect.Snackbar("Postponed '${event.title}' by 1 day"))
                            triggerSmartRefresh()
                        }
                    }
                    uiState = uiState.copy(smartTopFocus = uiState.smartTopFocus.map { if (it.title == event.title) it.copy(task = updated) else it })
                } else {
                    viewModelScope.launch { _snack.emit(UiEffect.Snackbar("Can't postpone '${event.title}' (no due date)")) }
                }
                suppressedFocusTitles.add(event.title)
                uiState = uiState.copy(smartTopFocus = uiState.smartTopFocus.filter { it.title !in suppressedFocusTitles })
                triggerSmartRefresh()
            }
            DashboardEvent.DismissNudge -> { uiState = uiState.copy(smartNudge = null) }
        }
    }

    data class UiState(
        val dashBoardTasks: List<Task> = emptyList(),
        val dashBoardEvents: Map<String, List<CalendarEvent>> = emptyMap(),
        val summaryTasks: List<Task> = emptyList(),
        val dashBoardEntries: List<DiaryEntry> = emptyList(),
        val smartTopFocus: List<FocusItem> = emptyList(),
        val smartFlow: List<FlowItem> = emptyList(),
        val smartNudge: String? = null,
        val loadingSmart: Boolean = false
    )

    private fun getCalendarEvents() = viewModelScope.launch {
        val excluded = getPreference(
            stringSetPreferencesKey(PrefsConstants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).first()
        val events = getAllEventsUseCase(excluded.toIntList()) {
            it.start.formatDateForMapping()
        }
        uiState = uiState.copy(
            dashBoardEvents = events
        )
    }

    private fun collectDashboardData() = viewModelScope.launch {
        combine(
            getPreference(
                intPreferencesKey(PrefsConstants.TASKS_ORDER_KEY),
                Order.DateModified(OrderType.ASC).toInt()
            ),
            getPreference(
                booleanPreferencesKey(PrefsConstants.SHOW_COMPLETED_TASKS_KEY),
                false
            ),
            getAllEntriesUseCase(Order.DateCreated(OrderType.ASC))
        ) { order, showCompleted, entries ->
            uiState = uiState.copy(
                dashBoardEntries = entries,
            )
            refreshTasks(order.toOrder(), showCompleted)
        }.collect()
    }

    private fun refreshSmart() = viewModelScope.launch {
        uiState = uiState.copy(loadingSmart = true)
        val data = getSmartDashboardItems()
        val filteredTop = data.topFocus.filter { it.title !in suppressedFocusTitles }
        uiState = uiState.copy(
            smartTopFocus = filteredTop,
            smartFlow = data.flow,
            smartNudge = data.nudge,
            loadingSmart = false
        )
    }

    private fun startSmartRefreshCollector() {
        smartRefreshJob?.cancel()
        smartRefreshJob = viewModelScope.launch {
            smartRefreshSignals
                .debounce(300)
                .collect {
                    refreshSmart()
                }
        }
    }

    private fun triggerSmartRefresh() {
        smartRefreshSignals.tryEmit(Unit)
    }

    private fun refreshTasks(order: Order, showCompleted: Boolean) {
        refreshTasksJob?.cancel()
        refreshTasksJob = getAllTasks(order).onEach { tasks ->
                uiState = uiState.copy(
                    dashBoardTasks = if (showCompleted) tasks else tasks.filter { !it.isCompleted },
                    summaryTasks = tasks.filter { it.createdDate.inTheLastWeek() }
                )
            }.launchIn(viewModelScope)
    }

    fun disableAppLock() = viewModelScope.launch {
        savePreference(booleanPreferencesKey(PrefsConstants.LOCK_APP_KEY), false)
    }

}