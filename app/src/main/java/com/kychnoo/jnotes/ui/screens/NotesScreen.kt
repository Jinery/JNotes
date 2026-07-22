package com.kychnoo.jnotes.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kychnoo.jnotes.R
import com.kychnoo.jnotes.data.model.note.PreviewNote
import com.kychnoo.jnotes.data.model.ui.UiState
import com.kychnoo.jnotes.ui.viewModel.NotesViewModel
import com.kychnoo.jnotes.ui.widgets.CircularLoader
import com.kychnoo.jnotes.ui.widgets.NotePreviewCard
import com.kychnoo.jnotes.ui.widgets.error.ErrorMessage
import kotlinx.serialization.Serializable

@Serializable
object NotesScreenRoute

@Composable
fun NotesScreen(
    innerPadding: PaddingValues,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NotesScreenContent(
        uiState = uiState,
        onNoteClick = onNoteClick,
        onRetry = { viewModel.retry() },
        innerPadding = innerPadding,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        modifier = modifier
    )
}

@Composable
private fun NotesScreenContent(
    uiState: UiState<List<PreviewNote>>,
    onNoteClick: (String) -> Unit,
    onRetry: () -> Unit,
    innerPadding: PaddingValues,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        UiState.Loading -> CircularLoader(modifier = Modifier.fillMaxSize())
        is UiState.Error -> ErrorMessage(uiState.message, onRetry = onRetry)
        is UiState.Success<List<PreviewNote>> -> NotesGrid(
            notes = uiState.data,
            onNoteClick = onNoteClick,
            innerPadding = innerPadding,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            modifier = modifier
        )
    }
}

@Composable
private fun NotesGrid(
    notes: List<PreviewNote>,
    onNoteClick: (String) -> Unit,
    innerPadding: PaddingValues,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    if (notes.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_notes),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(230.dp),
            modifier = modifier.fillMaxSize().padding(
                PaddingValues(
                    top = 16.dp + innerPadding.calculateTopPadding(),
                    bottom = 16.dp + innerPadding.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp
                )
            )
        ) {
            items(
                items = notes,
                key = { note -> note.id }
            ) { note ->
                NotePreviewCard(
                    previewNote = note,
                    onClick = { onNoteClick(note.id) },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }
        }
    }
}