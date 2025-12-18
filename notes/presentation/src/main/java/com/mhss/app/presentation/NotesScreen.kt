@file:OptIn(ExperimentalLayoutApi::class)

package com.mhss.app.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mhss.app.ui.R
import com.mhss.app.domain.model.*
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.preferences.domain.model.OrderType
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.components.notes.NoteCard
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.titleRes
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotesScreen(
    navController: NavHostController,
    viewModel: NotesViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState = viewModel.notesUiState
    var orderSettingsVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                context.getString(it)
            )
            viewModel.onEvent(NoteEvent.ErrorDisplayed)
        }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            MyBrainAppBar(
                stringResource(R.string.notes)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        Screen.NoteDetailsScreen()
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_note),
                    tint = Color.White
                )
            }
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
                if (uiState.notes.isEmpty())
                    NoNotesMessage()
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { orderSettingsVisible = !orderSettingsVisible }) {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            painter = painterResource(R.drawable.ic_settings_sliders),
                            contentDescription = stringResource(R.string.order_by)
                        )
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.NoteSearchScreen)
                    }) {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = stringResource(R.string.search)
                        )
                    }
                }
                AnimatedVisibility(visible = orderSettingsVisible) {
                    NotesSettingsSection(
                        uiState.notesOrder,
                        uiState.noteView,
                        onOrderChange = {
                            viewModel.onEvent(NoteEvent.UpdateOrder(it))
                        },
                        onViewChange = {
                            viewModel.onEvent(NoteEvent.UpdateView(it))
                        }
                    )
                }
                if (uiState.noteView == ItemView.LIST) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            top = 12.dp,
                            bottom = 24.dp,
                            start = 12.dp,
                            end = 12.dp
                        )
                    ) {
                        items(uiState.notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = {
                                    navController.navigate(
                                        Screen.NoteDetailsScreen(
                                            noteId = note.id,
                                        )
                                    )
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        items(uiState.notes) { note ->
                            key(note.id) {
                                NoteCard(
                                    note = note,
                                    onClick = {
                                        navController.navigate(
                                            Screen.NoteDetailsScreen(
                                                noteId = note.id
                                            )
                                        )
                                    },
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                        }
                }
            }
        }
    }
}@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotesSettingsSection(
    order: Order,
    view: ItemView,
    onOrderChange: (Order) -> Unit,
    onViewChange: (ItemView) -> Unit
) {
    val orders = remember {
        listOf(
            Order.DateModified(),
            Order.DateCreated(),
            Order.Alphabetical()
        )
    }
    val orderTypes = remember {
        listOf(
            OrderType.ASC,
            OrderType.DESC
        )
    }
    val noteViews = remember {
        listOf(
            ItemView.LIST,
            ItemView.GRID
        )
    }
    Column(
        Modifier.background(color = MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = stringResource(R.string.order_by),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
        FlowRow(
            modifier = Modifier.padding(end = 8.dp)
        ) {
            orders.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = order::class == it::class,
                        onClick = {
                            if (order != it)
                                onOrderChange(
                                    it.copyOrder(orderType = order.orderType)
                                )
                        }
                    )
                    Text(
                        text = stringResource(it.titleRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        HorizontalDivider()
        FlowRow {
            orderTypes.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = order.orderType == it,
                        onClick = {
                            if (order != it)
                                onOrderChange(
                                    order.copyOrder(it)
                                )
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(it.titleRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        HorizontalDivider()
        Text(
            text = stringResource(R.string.view_as),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        )
        FlowRow {
            noteViews.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = view.title == it.title,
                        onClick = {
                            if (view.title != it.title)
                                onViewChange(
                                    it
                                )
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(it.title),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun NoNotesMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_notes_message),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            modifier = Modifier.size(125.dp),
            painter = painterResource(id = R.drawable.notes_img),
            contentDescription = stringResource(R.string.no_notes_message),
            alpha = 0.7f
        )
    }
}
