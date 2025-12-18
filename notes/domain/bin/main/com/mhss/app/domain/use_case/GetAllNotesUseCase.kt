package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.Note
import com.mhss.app.domain.repository.NoteRepository
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.preferences.domain.model.OrderType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class GetAllNotesUseCase(
    private val notesRepository: NoteRepository,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(order: Order) : Flow<List<Note>> {
        return notesRepository.getAllNotes().map { list ->
            when (order.orderType) {
                is OrderType.ASC -> {
                    when (order) {
                        is Order.Alphabetical -> list.sortedBy { it.title }
                        is Order.DateCreated -> list.sortedBy { it.createdDate }
                        is Order.DateModified -> list.sortedBy { it.updatedDate }
                        else -> list.sortedBy { it.updatedDate }
                    }
                }
                is OrderType.DESC -> {
                    when (order) {
                        is Order.Alphabetical -> list.sortedBy { it.title }.reversed()
                        is Order.DateCreated -> list.sortedBy { it.createdDate }.reversed()
                        is Order.DateModified -> list.sortedBy { it.updatedDate }.reversed()
                        else -> list.sortedBy { it.updatedDate }.reversed()
                    }
                }
            }
        }.flowOn(defaultDispatcher)
    }

}