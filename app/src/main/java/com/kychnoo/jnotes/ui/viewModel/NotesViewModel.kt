package com.kychnoo.jnotes.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kychnoo.jnotes.R
import com.kychnoo.jnotes.data.local.entity.NoteEntity
import com.kychnoo.jnotes.data.model.note.PreviewNote
import com.kychnoo.jnotes.data.model.note.toPreviewNote
import com.kychnoo.jnotes.data.model.ui.UiState
import com.kychnoo.jnotes.data.repository.NoteRepository
import com.kychnoo.jnotes.provider.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState<List<PreviewNote>>> = MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState<List<PreviewNote>>> = _uiState.asStateFlow()

    private val retryTrigger: MutableSharedFlow<Unit> = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        observeNotes()
    }

    fun retry() {
        _uiState.update { UiState.Loading }
        retryTrigger.tryEmit(Unit)
    }

    private fun observeNotes() {
        noteRepository.getAllNotesWithBlocks()
            .map { notesWithBlocks ->
                notesWithBlocks.map { noteWithBlock ->
                    noteWithBlock.toPreviewNote()
                }
            }
            .onEach { notes ->
                _uiState.update { UiState.Success(notes) }
            }.retryWhen { cause, _ ->
                if (cause is CancellationException) throw cause
                _uiState.update {
                    UiState.Error(
                        cause.localizedMessage ?: resourceProvider.getString(R.string.loading_error)
                    )
                }
                retryTrigger.first()
                true
            }.launchIn(viewModelScope)
    }
}