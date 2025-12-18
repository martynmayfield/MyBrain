package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.AiConstants
import com.mhss.app.domain.autoGenerateTitlePrompt
import com.mhss.app.domain.model.Note
import com.mhss.app.network.NetworkResult
import com.mhss.app.domain.use_case.AddNoteUseCase
import com.mhss.app.domain.use_case.DeleteNoteUseCase
import com.mhss.app.domain.use_case.GetNoteUseCase
import com.mhss.app.domain.use_case.SendAiPromptUseCase
import com.mhss.app.domain.use_case.UpdateNoteUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.util.date.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named

class NoteDetailsViewModel(
    private val getNote: GetNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val addNote: AddNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val sendAiPrompt: SendAiPromptUseCase,
    private val getPreference: GetPreferenceUseCase,
    @Named("applicationScope") private val applicationScope: CoroutineScope,
    id: Int,
) : ViewModel() {

    var noteUiState by mutableStateOf(UiState())
        private set

    var title by mutableStateOf("")
        private set
    var content by mutableStateOf("")
        private set

    private var autoSaveJob: Job? = null
    private val debounceTime = 2000L

    private val _aiEnabled = MutableStateFlow(false)
    val aiEnabled: StateFlow<Boolean> = _aiEnabled
    var aiState by mutableStateOf((AiState()))
        private set
    private var aiActionJob: Job? = null

    // AI is disabled on Android

    init {
        viewModelScope.launch {
            val note: Note? = if (id != -1) getNote(id) else null

            if (note != null) {
                title = note.title
                content = note.content
            }

            noteUiState = noteUiState.copy(
                note = note,
                readingMode = note != null
            )
        }
        viewModelScope.launch {
            getPreference(intPreferencesKey(PrefsConstants.AI_PROVIDER_KEY), AiProvider.None.id).collect { providerId ->
                _aiEnabled.value = AiProvider.entries.find { it.id == providerId } != AiProvider.None
            }
        }
    }

    fun onEvent(event: NoteDetailsEvent) {
        when (event) {
            is NoteDetailsEvent.ScreenOnStop -> applicationScope.launch {
                if (!noteUiState.navigateUp) {
                    autoSaveJob?.cancel()
                    saveNote()
                }
            }

            is NoteDetailsEvent.DeleteNote -> viewModelScope.launch {
                deleteNote(event.note)
                noteUiState = noteUiState.copy(navigateUp = true)
            }

            NoteDetailsEvent.ToggleReadingMode -> noteUiState =
                noteUiState.copy(readingMode = !noteUiState.readingMode)

            is NoteDetailsEvent.UpdateTitle -> {
                title = event.title
                saveNoteWithDebounce()
            }

            is NoteDetailsEvent.UpdateContent -> {
                content = event.content
                saveNoteWithDebounce()
            }

            is AiAction -> aiActionJob = viewModelScope.launch {
                val isTitleGeneration = event is NoteDetailsEvent.AutoGenerateTitle
                aiState = aiState.copy(loading = true, showAiSheet = true, error = null)
                val prompt = if (isTitleGeneration) {
                    event.content.autoGenerateTitlePrompt
                } else {
                    // For other actions, but currently only title is implemented
                    ""
                }
                val result = sendAiPrompt(prompt)
                aiState = when (result) {
                    is NetworkResult.Success -> {
                        if (isTitleGeneration) {
                            title = result.data
                        }
                        aiState.copy(
                            loading = false,
                            result = if (isTitleGeneration) null else result.data,
                            error = null
                        )
                    }
                    is NetworkResult.InvalidKey -> {
                        aiState.copy(
                            loading = false,
                            result = null,
                            error = "Invalid API key"
                        )
                    }
                    is NetworkResult.InternetError -> {
                        aiState.copy(
                            loading = false,
                            result = null,
                            error = "No internet connection"
                        )
                    }
                    is NetworkResult.OtherError -> {
                        aiState.copy(
                            loading = false,
                            result = null,
                            error = result.message ?: "Unknown error"
                        )
                    }
                }
            }

            NoteDetailsEvent.AiResultHandled -> {
                aiActionJob?.cancel()
                aiActionJob = null
                aiState = aiState.copy(showAiSheet = false)
            }

            NoteDetailsEvent.StartVoiceInput -> {
                // TODO: Implement voice input using speech recognition
                // This would require microphone permissions and speech-to-text API
            }
        }
    }

    private fun saveNoteWithDebounce() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(debounceTime)
            saveNote()
        }
    }

    private suspend fun saveNote() {
        if (noteUiState.navigateUp) return

        if (noteUiState.note == null) {
            if (title.isNotBlank() || content.isNotBlank()) {
                var finalTitle = title
                if (finalTitle.isBlank() && content.isNotBlank()) {
                    val prompt = content.autoGenerateTitlePrompt
                    val result = sendAiPrompt(prompt)
                    if (result is NetworkResult.Success) {
                        finalTitle = result.data
                        title = finalTitle // Update the state
                    }
                }
                val note = Note(
                    title = finalTitle,
                    content = content,
                    createdDate = now(),
                    updatedDate = now()
                )
                val id = addNote(note)
                noteUiState = noteUiState.copy(note = note.copy(id = id.toInt()))
            }
        } else {
            val currentNote = noteUiState.note!!
            if (currentNote.title != title ||
                currentNote.content != content
            ) {
                val newNote = currentNote.copy(
                    title = title,
                    content = content,
                    updatedDate = now()
                )
                updateNote(newNote)
                noteUiState = noteUiState.copy(note = newNote)
            }
        }
    }

    data class UiState(
        val note: Note? = null,
        val navigateUp: Boolean = false,
        val readingMode: Boolean = false
    )

    data class AiState(
        val loading: Boolean = false,
        val result: String? = null,
        val error: String? = null,
        val showAiSheet: Boolean = false,
    )
}