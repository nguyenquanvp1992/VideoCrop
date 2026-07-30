package com.easy.edtivideo.ui.editvideo

import android.net.Uri

/**
 * Represents the UI state observed by [EditVideoFragment].
 *
 * This is deliberately decoupled from [com.easy.edtivideo.domain.model.EditorState].
 * The ViewModel is responsible for mapping domain state → UI state, so the Fragment
 * never needs to know about business logic internals.
 *
 * Design decisions:
 * - Using a flat data class (not sealed) keeps the Fragment's render logic simple.
 * - New UI fields for Crop, Rotate, Text, etc. are added here in Phase 2+
 *   without changing the Fragment's observe pattern.
 */
data class EditVideoUiState(
    /** True while the session is initialising / loading the video. */
    val isLoading: Boolean = true,

    /** URI of the video once it has been loaded. Null when [isLoading] is true. */
    val videoUri: Uri? = null,

    /** Total duration of the video in milliseconds. 0 when not yet loaded. */
    val videoDurationMs: Long = 0L,

    /** Non-null when an error has occurred. The message is safe to display in the UI. */
    val errorMessage: String? = null,
) {
    // ---------------------------------------------------------------------------
    // Convenience helpers — keeps Fragment render logic free of boolean arithmetic
    // ---------------------------------------------------------------------------

    val hasError: Boolean get() = errorMessage != null

    val isReady: Boolean get() = !isLoading && videoUri != null && !hasError

    // ---------------------------------------------------------------------------
    // Future UI fields — add here for Phase 2+
    // ---------------------------------------------------------------------------
    // val isCropping: Boolean = false,
    // val cropInfo: CropInfo? = null,
    // val rotationDegrees: Int = 0,
    // val isExporting: Boolean = false,
    // val exportProgress: Float = 0f,

    companion object {
        /** Default state shown while the session is starting. */
        val INITIAL = EditVideoUiState(isLoading = true)
    }
}
