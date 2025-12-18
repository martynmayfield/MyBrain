# MyBrain Software Functions Outline

MyBrain is an all-in-one productivity Android application built with Kotlin, Jetpack Compose, and Clean Architecture (MVI pattern). It features local data storage (Room DB), dependency injection (Koin), and AI integration. Below is a detailed domain-based outline of its functions, derived from the codebase analysis.

## I. Task Management Domain
Manages user tasks with priorities, sub-tasks, due dates, and recurring options.

### Models
- **Task**: Represents a task with title, description, completion status, priority (LOW/MEDIUM/HIGH), creation/update dates, sub-tasks list, due date, recurring flag, frequency (EVERY_MINUTES/HOURLY/DAILY/WEEKLY/MONTHLY/ANNUAL), frequency amount, and ID.
- **SubTask**: Represents a sub-task with title, completion status, and ID (using UUID for serialization).
- **Priority**: Enum for task priority levels (LOW=0, MEDIUM=1, HIGH=2).
- **TaskFrequency**: Enum for recurring task frequencies (EVERY_MINUTES=0 to ANNUAL=5).

### Repository
- **TaskRepository**: Interface for task data operations.
  - `getAllTasks()`: Flow of all tasks.
  - `getTaskById(id: Int)`: Suspend function to get a single task.
  - `searchTasks(title: String)`: Flow of tasks matching title.
  - `insertTask(task: Task)`: Suspend function to add a task, returns ID.
  - `updateTask(task: Task)`: Suspend function to update a task.
  - `completeTask(id: Int, completed: Boolean)`: Suspend function to mark task complete/incomplete.
  - `deleteTask(task: Task)`: Suspend function to delete a task.

### Use Cases
- **AddTaskUseCase**: Adds a new task.
- **DeleteTaskUseCase**: Deletes a task.
- **GetAllTasksUseCase**: Retrieves all tasks.
- **GetTaskByIdUseCase**: Retrieves a task by ID.
- **SearchTasksUseCase**: Searches tasks by title.
- **UpdateTaskUseCase**: Updates a task.
- **UpdateTaskCompletedUseCase**: Updates task completion status.

## II. Notes Management Domain
Handles AI-powered note-taking with markdown support, auto-titling, semantic search, and intelligent structuring. No manual folders or pinning—AI handles organization.

### Models
- **Note**: Represents a note with title (auto-generated), content, creation/update dates, and ID. (Removed: pinned status, folder ID)

### Repository
- **NoteRepository**: Interface for note data operations.
  - `getAllNotes()`: Flow of all notes (renamed from getAllFolderlessNotes).
  - `getNote(id: Int)`: Suspend function to get a note.
  - `searchNotes(query: String)`: Suspend function to search notes (enhanced with semantic search via local embeddings).
  - `addNote(note: Note)`: Suspend function to add a note, returns ID.
  - `updateNote(note: Note)`: Suspend function to update a note.
  - `deleteNote(note: Note)`: Suspend function to delete a note.
  - (Removed: All folder-related methods like insertNoteFolder, getAllNoteFolders, etc.)

### Use Cases
- **AddNoteUseCase**: Adds a note with AI auto-titling.
- **DeleteNoteUseCase**: Deletes a note.
- **GetAllNotesUseCase**: Gets all notes (renamed from GetAllFolderlessNotesUseCase).
- **GetNoteUseCase**: Gets a note by ID.
- **SearchNotesUseCase**: Searches notes with semantic matching.
- **UpdateNoteUseCase**: Updates a note.
- (Removed: All folder-related use cases like AddNoteFolderUseCase, etc.)

### AI Features
- **Auto-Titling**: Generates intelligent titles from content using local Ollama.
- **Semantic Search**: Finds notes by meaning using local embeddings (nomic-embed-text).
- **Voice Input**: Transcribes voice to text via local Whisper, then summarizes with Ollama.
- **Auto-Daily Notes**: Creates/appends to dated notes automatically.
- **Bidirectional Linking**: Parses [[Note Title]] for links and backlinks.
- **Ask My Notes**: AI chat scoped to notes database (local RAG).
- **Smart Highlights**: Extracts highlights from shared web content.

## III. Diary Management Domain
Manages daily diary entries with mood tracking and analytics.

### Models
- **DiaryEntry**: Represents an entry with title, content, creation/update dates, mood, and ID.
- **Mood**: Enum for mood levels (TERRIBLE=1 to AWESOME=5).

### Repository
- **DiaryRepository**: Interface for diary data operations.
  - `getAllEntries()`: Flow of all entries.
  - `getEntry(id: Int)`: Suspend function to get an entry.
  - `searchEntries(title: String)`: Suspend function to search entries.
  - `addEntry(diary: DiaryEntry)`: Suspend function to add an entry, returns ID.
  - `updateEntry(diary: DiaryEntry)`: Suspend function to update an entry.
  - `deleteEntry(diary: DiaryEntry)`: Suspend function to delete an entry.

### Use Cases
- **AddDiaryEntryUseCase**: Adds a diary entry.
- **DeleteDiaryEntryUseCase**: Deletes a diary entry.
- **GetAllEntriesUseCase**: Gets all entries.
- **GetDiaryEntryUseCase**: Gets an entry by ID.
- **GetDiaryForChartUseCase**: Retrieves data for mood charts/graphs.
- **SearchEntriesUseCase**: Searches diary entries.
- **UpdateDiaryEntryUseCase**: Updates a diary entry.

## IV. Bookmarks Management Domain
Handles saving and managing web bookmarks via share menu.

### Models
- **Bookmark**: Represents a bookmark with URL, title, description, creation/update dates, and ID.

### Repository
- **BookmarkRepository**: Interface for bookmark data operations.
  - `getAllBookmarks()`: Flow of all bookmarks.
  - `getBookmark(id: Int)`: Suspend function to get a bookmark.
  - `searchBookmarks(query: String)`: Suspend function to search bookmarks.
  - `addBookmark(bookmark: Bookmark)`: Suspend function to add a bookmark, returns ID.
  - `updateBookmark(bookmark: Bookmark)`: Suspend function to update a bookmark.
  - `deleteBookmark(bookmark: Bookmark)`: Suspend function to delete a bookmark.

### Use Cases
- Similar to other domains: Add, Delete, Get All, Get by ID, Search, Update.

## V. Calendar Management Domain
Integrates with device calendar for viewing and managing events.

### Models
- **CalendarEvent**: Represents an event with ID, title, description, start/end times, location, all-day flag, color, calendar ID, recurring flag, and frequency.
- **CalendarEventFrequency**: Enum for recurrence (NEVER, DAILY, WEEKLY, MONTHLY, YEARLY).
- **Calendar**: Represents a calendar with ID, name, account, color, and inclusion flag.

### Repository
- **CalendarRepository**: Interface for calendar data operations.
  - `getEvents()`: Suspend function to get all events.
  - `getCalendars()`: Suspend function to get all calendars.
  - `addEvent(event: CalendarEvent)`: Suspend function to add an event.
  - `deleteEvent(event: CalendarEvent)`: Suspend function to delete an event.
  - `updateEvent(event: CalendarEvent)`: Suspend function to update an event.
  - `createCalendar()`: Suspend function to create a new calendar.

### Use Cases
- Standard CRUD use cases for events and calendars.

## VI. AI Assistant Domain
Provides AI-powered chat and planning with attachments.

### Models
- **AiMessage**: Represents a message with content, type (USER/MODEL), timestamp, attachments, and attachments text.
- **AiMessageAttachment**: Sealed interface for attachments: Note, Task, or CalendarEvents.
- **AiMessageType**: Enum for message types (USER, MODEL).

### Repository/API
- **AiApi**: Interface for AI API interactions.
  - `sendPrompt(baseUrl: String, prompt: String, model: String, key: String)`: Sends a prompt, returns NetworkResult<String>.
  - `sendMessage(baseUrl: String, messages: List<AiMessage>, model: String, key: String)`: Sends messages with attachments, returns NetworkResult<AiMessage>.

### Use Cases
- **SendAiPromptUseCase**: Sends AI prompts, supporting OpenAI and Gemini providers with API keys and models.

## VII. Settings/Preferences Domain
Manages app settings and user preferences.

### Models
- **AiProvider**: Enum for AI providers (None=0, Gemini=1, OpenAI=2).
- **Order**: Sealed class for sorting orders (e.g., Alphabetical with ASC/DESC).
- **PrefsKey**: Sealed class for typed preference keys (Int, Boolean, String, StringSet).

### Repository
- **PreferenceRepository**: Interface for preferences data operations.
  - `savePreference(key: PrefsKey<T>, value: T)`: Suspend function to save a preference.
  - `getPreference(key: PrefsKey<T>, defaultValue: T)`: Flow of a preference value.

### Use Cases
- Various use cases for reading/writing specific preferences (e.g., theme, AI settings).

## VIII. Core/Shared Domains
Provides infrastructure for the app.

### Alarm Domain
- Manages task reminders and notifications using AlarmManager.

### Database Domain
- **MyBrainDatabase**: Room database with entities for all models (Tasks, Notes, Diary, Bookmarks, etc.).
- Handles migrations and DAOs.

### DI Domain
- Koin modules for dependency injection across all features.

### Network Domain
- **NetworkResult**: Sealed class for API results (Success, Error, etc.).
- Handles HTTP requests with Ktor.

### Notification Domain
- Manages push notifications and reminders.

### UI Domain
- Shared Compose UI components and themes.

### Util Domain
- Utility functions (e.g., date formatting, validation).

### Widget Domain
- Home screen widget for calendar events using Jetpack Glance.

## IX. App-Level Functions
- **Dashboard**: Aggregates tasks, calendar events, and overviews.
- **Authentication**: Biometric login.
- **Backup/Restore**: Data export/import.
- **Themes**: Dark/light mode.
- **Localization**: Multi-language support via Crowdin.

This outline covers the primary functions based on domain analysis. The app emphasizes privacy (local storage), productivity features, and AI integration, all following Clean Architecture principles.