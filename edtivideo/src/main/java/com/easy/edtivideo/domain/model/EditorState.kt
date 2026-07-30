package com.easy.edtivideo.domain.model

import android.net.Uri

/**
 * Represents the business state of an editing session.
 *
 * This is the Single Source of Truth emitted by [EditorSession].
 * It is a pure domain model — no Android Framework dependency beyond [Uri]
 * which is required to identify the video resource.
 *
 * Future states (Cropping, Rotating, Exporting, etc.) should be added here
 * as new subclasses without modifying existing ones (Open/Closed principle).
 */
sealed class EditorState {

    /**
     * Initial state when the session is created but the video has not been loaded yet.
     */
    data object Loading : EditorState()

    /**
     * Video has been loaded successfully and is ready for editing.
     *
     * @param videoUri        URI pointing to the source video.
     * @param videoDurationMs Total duration of the video in milliseconds.
     */
    data class Idle(
        val videoUri: Uri,
        val videoDurationMs: Long,
    ) : EditorState()

    /**
     * An error occurred during a session operation.
     *
     * @param message Human-readable description of the error.
     * @param cause   Optional underlying throwable for debugging.
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null,
    ) : EditorState()

    // ---------------------------------------------------------------------------
    // Future states — add here for Phase 2+
    // ---------------------------------------------------------------------------
    // data class Cropping(val videoUri: Uri, val cropInfo: CropInfo?) : EditorState()
    // data class Rotating(val videoUri: Uri, val degrees: Int) : EditorState()
    // data class Exporting(val progress: Float) : EditorState()
    // data object ExportDone : EditorState()
}
