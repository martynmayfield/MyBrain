package com.mhss.app.mybrain.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mhss.app.ui.R
import com.mhss.app.presentation.CalendarDashboardWidget
import com.mhss.app.presentation.MoodCircularBar
import com.mhss.app.presentation.TasksDashboardWidget
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.navigation.Screen
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: MainViewModel = koinViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val pullState = rememberPullToRefreshState()
    Scaffold(
        topBar = {
            MyBrainAppBar(stringResource(R.string.dashboard))
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AssistantScreen) }) {
                Icon(Icons.Filled.Add, contentDescription = "Brain dump")
            }
        }
    ) {paddingValues ->
        LaunchedEffect(true) { viewModel.onDashboardEvent(DashboardEvent.InitAll) }
        LaunchedEffect(Unit) {
            viewModel.snack.collect { effect ->
                when (effect) {
                    is MainViewModel.UiEffect.Snackbar -> {
                        val res = snackbarHostState.showSnackbar(effect.message, effect.actionLabel)
                        if (res == SnackbarResult.ActionPerformed && effect.actionEvent != null) {
                            viewModel.onDashboardEvent(effect.actionEvent)
                        }
                    }
                }
            }
        }
        PullToRefreshBox(
            isRefreshing = viewModel.uiState.loadingSmart,
            state = pullState,
            onRefresh = { viewModel.onDashboardEvent(DashboardEvent.RefreshSmart) },
            modifier = Modifier.padding(paddingValues)
        ) {
        LazyColumn(contentPadding = PaddingValues(bottom = 65.dp)) {
            item {
                // Top 3 Focus Items
                Column(Modifier.padding(16.dp)) {
                    Text(text = "Top Focus", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    viewModel.uiState.smartTopFocus.take(3).forEach { fi ->
                        Card(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(fi.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(fi.reason, style = MaterialTheme.typography.bodySmall)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { viewModel.onDashboardEvent(DashboardEvent.SnoozeFocus(fi.title)) }) { Text("Snooze") }
                                    TextButton(onClick = { viewModel.onDashboardEvent(DashboardEvent.PostponeFocus(fi.title)) }) { Text("Postpone") }
                                    if (fi.task != null) {
                                        TextButton(onClick = { viewModel.onDashboardEvent(DashboardEvent.CompleteTask(fi.task, true)) }) { Text("Complete") }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            item {
                // Gentle Nudge
                val nudge = viewModel.uiState.smartNudge
                if (nudge != null) {
                    Card(Modifier.padding(horizontal = 16.dp)) {
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(nudge, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { viewModel.onDashboardEvent(DashboardEvent.DismissNudge) }) { Text("Dismiss") }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
            item {
                CalendarDashboardWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f),
                    events = viewModel.uiState.dashBoardEvents,
                    onClick = {
                        navController.navigate(
                            Screen.CalendarScreen
                        )
                    },
                    onPermission = {
                        viewModel.onDashboardEvent(DashboardEvent.ReadPermissionChanged(it))
                    },
                    onAddEventClicked = {
                        navController.navigate(
                            Screen.CalendarEventDetailsScreen()
                        )
                    },
                    onEventClicked = {
                        navController.navigate(
                            Screen.CalendarEventDetailsScreen(
                                Base64.encode(Json.encodeToString(it).toByteArray())
                            )
                        )
                    }
                )
            }
            item {
                TasksDashboardWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f),
                    tasks = viewModel.uiState.dashBoardTasks,
                    onCheck = { task, completed ->
                        viewModel.onDashboardEvent(DashboardEvent.CompleteTask(task, completed))
                    },
                    onTaskClick = {
                        navController.navigate(
                            Screen.TaskDetailScreen(it.id)
                        )
                    },
                    onAddClick = {
                        navController.navigate(
                            Screen.TasksScreen(addTask = true)
                        )
                    },
                    onClick = {
                        navController.navigate(
                            Screen.TasksScreen()
                        )
                    }
                )
            }
            item {
                // Today’s Flow
                Column(Modifier.padding(16.dp)) {
                    Text("Today’s Flow", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    viewModel.uiState.smartFlow.forEach { flow ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(flow.title, style = MaterialTheme.typography.titleMedium)
                                flow.hint?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            item {
                Row {
                    MoodCircularBar(
                        entries = viewModel.uiState.dashBoardEntries,
                        showPercentage = false,
                        modifier = Modifier.weight(1f, fill = true),
                        onClick = {
                            navController.navigate(
                                Screen.DiaryChartScreen
                            )
                        }
                    )
                    TasksSummaryCard(
                        modifier = Modifier.weight(1f, fill = true),
                        tasks = viewModel.uiState.summaryTasks
                    )
                }
            }
            // bottom spacer not needed; provided via contentPadding above
        }
        }
    }
}