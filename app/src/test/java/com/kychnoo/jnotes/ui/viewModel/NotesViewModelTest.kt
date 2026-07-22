package com.kychnoo.jnotes.ui.viewModel

import app.cash.turbine.test
import com.kychnoo.jnotes.R
import com.kychnoo.jnotes.data.local.entity.NoteEntity
import com.kychnoo.jnotes.data.local.relation.NoteWithBlocks
import com.kychnoo.jnotes.data.model.ui.UiState
import com.kychnoo.jnotes.data.repository.NoteRepository
import com.kychnoo.jnotes.provider.ResourceProvider
import com.kychnoo.jnotes.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val noteRepository = mockk<NoteRepository>()
    private val resourceProvider = mockk<ResourceProvider>()
    private lateinit var viewModel: NotesViewModel

    @Before
    fun setup() {
        every { resourceProvider.getString(any()) } returns "Error"
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        every { noteRepository.getAllNotesWithBlocks() } returns flow { }
        
        viewModel = NotesViewModel(noteRepository, resourceProvider)
        
        assertEquals(UiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `Success state should be emitted when repository returns notes`() = runTest {
        val noteEntity = NoteEntity(
            id = "1",
            title = "Test Note",
            createdAt = 0,
            updatedAt = 100,
            isPinned = false,
            isEncrypted = false
        )
        val notes = listOf(NoteWithBlocks(noteEntity, emptyList()))
        
        every { noteRepository.getAllNotesWithBlocks() } returns flowOf(notes)
        
        viewModel = NotesViewModel(noteRepository, resourceProvider)

        viewModel.uiState.test {
            // Initial state from StateFlow
            assertEquals(UiState.Loading, awaitItem())
            
            // Success state
            val state = awaitItem()
            assertTrue("Expected Success but was $state", state is UiState.Success)
            assertEquals("Test Note", (state as UiState.Success).data[0].title)
        }
    }

    @Test
    fun `Error state should be emitted when repository fails`() = runTest {
        val errorMessage = "Network Error"
        every { noteRepository.getAllNotesWithBlocks() } returns flow {
            throw Exception(errorMessage)
        }
        
        viewModel = NotesViewModel(noteRepository, resourceProvider)

        viewModel.uiState.test {
            assertEquals(UiState.Loading, awaitItem())
            
            val state = awaitItem()
            assertTrue("Expected Error but was $state", state is UiState.Error)
            assertEquals(errorMessage, (state as UiState.Error).message)
        }
    }

    @Test
    fun `retry should trigger re-observation of notes`() = runTest {
        var collectionCount = 0
        val testFlow = flow {
            collectionCount++
            if (collectionCount == 1) {
                throw Exception("First fail")
            } else {
                emit(emptyList<NoteWithBlocks>())
            }
        }
        
        every { noteRepository.getAllNotesWithBlocks() } returns testFlow

        viewModel = NotesViewModel(noteRepository, resourceProvider)

        viewModel.uiState.test {
            assertEquals(UiState.Loading, awaitItem())
            
            val firstState = awaitItem()
            assertTrue("Expected Error but was $firstState", firstState is UiState.Error)

            // Act
            viewModel.retry()

            assertEquals(UiState.Loading, awaitItem())
            
            val finalState = awaitItem()
            assertTrue("Expected Success but was $finalState", finalState is UiState.Success)
            
            assertEquals(2, collectionCount)
        }
    }
}
