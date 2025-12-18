package com.mhss.app.data

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.room.withTransaction
import com.mhss.app.database.MyBrainDatabase
import com.mhss.app.database.entity.BookmarkEntity
import com.mhss.app.database.entity.DiaryEntryEntity
import com.mhss.app.database.entity.NoteEntity
import com.mhss.app.database.entity.NoteFolderEntity
import com.mhss.app.database.entity.TaskEntity
import com.mhss.app.database.entity.withoutIds
import com.mhss.app.domain.repository.BackupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class BackupRepositoryImpl(
    private val context: Context,
    private val database: MyBrainDatabase,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : BackupRepository {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun exportDatabase(
        directoryUri: String,
        exportNotes: Boolean,
        exportTasks: Boolean,
        exportDiary: Boolean,
        exportBookmarks: Boolean,
        encrypted: Boolean, // To be added in a future version
        password: String // To be added in a future version
    ): Boolean {
        return withContext(ioDispatcher) {
            try {
                val fileName = "MyBrain_Backup_${System.currentTimeMillis()}.json"
                val pickedDir = DocumentFile.fromTreeUri(context, directoryUri.toUri())
                val destination = pickedDir!!.createFile("application/json", fileName)

                val notes: List<NoteEntity> = if (exportNotes) database.noteDao().getAllNotes().first() else emptyList()
                val tasks: List<TaskEntity> = if (exportTasks) database.taskDao().getAllTasks().first() else emptyList()
                val diary: List<DiaryEntryEntity> = if (exportDiary) database.diaryDao().getAllEntries().first() else emptyList()
                val bookmarks: List<BookmarkEntity> = if (exportBookmarks) database.bookmarkDao().getAllBookmarks().first() else emptyList()

                val backupData = BackupData(
                    notes = notes,
                    tasks = tasks,
                    diary = diary,
                    bookmarks = bookmarks
                )

                val outputStream =
                    destination?.let { context.contentResolver.openOutputStream(it.uri) }
                        ?: return@withContext false

                outputStream.use {
                    Json.encodeToStream(backupData, it)
                }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun importDatabase(
        fileUri: String,
        encrypted: Boolean, // To be added in a future version
        password: String // To be added in a future version
    ): Boolean {
        return withContext(ioDispatcher) {
            try {
                val json = Json {
                    ignoreUnknownKeys = true
                }
                val backupData = context.contentResolver.openInputStream(fileUri.toUri())?.use {
                    json.decodeFromStream<BackupData>(it)
                } ?: return@withContext false

                database.withTransaction {
                    database.noteDao().insertNotes(backupData.notes.withoutIds())
                    database.taskDao().insertTasks(backupData.tasks.withoutIds())
                    database.diaryDao().insertEntries(backupData.diary.withoutIds())
                    database.bookmarkDao().insertBookmarks(backupData.bookmarks.withoutIds())
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    @Serializable
    private data class BackupData(
        @SerialName("notes") val notes: List<NoteEntity> = emptyList(),
        @SerialName("tasks") val tasks: List<TaskEntity> = emptyList(),
        @SerialName("diary") val diary: List<DiaryEntryEntity> = emptyList(),
        @SerialName("bookmarks") val bookmarks: List<BookmarkEntity> = emptyList()
    )
}