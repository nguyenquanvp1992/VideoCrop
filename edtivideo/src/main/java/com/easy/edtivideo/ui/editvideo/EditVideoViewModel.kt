package com.easy.edtivideo.ui.editvideo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easy.edtivideo.domain.model.EditorState
import com.easy.edtivideo.session.EditorSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Adapter between [EditorSession] and [EditVideoFragment].
 *
 * Architectural rules enforced here:
 *  - Receives [EditorSession] via constructor — NOT via Hilt field injection.
 *    This keeps the ViewModel testable (pass a mock session in tests).
 *  - Contains NO business logic. All commands are forwarded to [EditorSession].
 *  - Exposes a single [uiState] flow that the Fragment observes — one observer,
 *    one state object, zero race conditions.
 *  - [viewModelScope] is used for Flow collection; it is automatically cancelled
 *    when the Fragment is destroyed, preventing leaks.
 *
 * @param session The active [EditorSession] for this editing flow.
 */
class EditVideoViewModel(
    private val session: EditorSession,
) : ViewModel() {

    /**
     * The single UI state observed by [EditVideoFragment].
     *
     * - [SharingStarted.WhileSubscribed] with a 5-second timeout means the
     *   upstream collection is kept alive for 5 s after the last subscriber
     *   disappears (e.g. during screen rotation), avoiding redundant reloads.
     * - [EditVideoUiState.INITIAL] is the value shown before the first emission.
     */
    val uiState: StateFlow<EditVideoUiState> = session.state
        .map { domainState -> domainState.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EditVideoUiState.INITIAL,
        )

    // -------------------------------------------------------------------------
    // Command forwarding — no business logic, just delegation to EditorSession
    // -------------------------------------------------------------------------

    /**
     * Initiates video loading. Safe to call multiple times; [EditorSession]
     * guards against redundant loads internally.
     */
    fun loadVideo() {
        session.loadVideo()
    }

    // Future commands — add here for Phase 2+
    // fun onCropClicked() { session.applyCrop(...) }
    // fun onRotateClicked() { session.applyRotate(90) }
    // fun onExportClicked() { session.exportVideo(outputPath) }

    // -------------------------------------------------------------------------
    // Private mapping — EditorState (domain) → EditVideoUiState (UI)
    // -------------------------------------------------------------------------

    private fun EditorState.toUiState(): EditVideoUiState = when (this) {
        is EditorState.Loading -> EditVideoUiState(isLoading = true)
        is EditorState.Idle -> EditVideoUiState(
            isLoading = false,
            videoUri = videoUri,
            videoDurationMs = videoDurationMs,
        )
        is EditorState.Error -> EditVideoUiState(
            isLoading = false,
            errorMessage = message,
        )
    }
}
