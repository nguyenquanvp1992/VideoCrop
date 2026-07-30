package com.easy.edtivideo.domain.processor

import android.net.Uri

/**
 * Contract for all video processing operations.
 *
 * This interface is the boundary between the domain layer and the media
 * infrastructure layer. [EditorSession] depends on this abstraction, never
 * on a concrete implementation (Dependency Inversion Principle).
 *
 * The concrete implementation (Media3VideoProcessor) will live in the `data`
 * layer and will be injected via Hilt. This keeps [EditorSession] testable
 * without Android dependencies.
 *
 * All functions are suspend functions so callers can use structured concurrency
 * with coroutines without blocking the calling thread.
 */
interface VideoProcessor {

    /**
     * Reads metadata from the given video URI and returns a [VideoInfo] summary.
     *
     * @param uri URI pointing to the source video (content:// or file://).
     * @return    [VideoInfo] containing duration and other metadata.
     * @throws    [VideoProcessorException] if the file cannot be read.
     */
    suspend fun loadVideoInfo(uri: Uri): VideoInfo

    /**
     * Releases any resources held by this processor (e.g. MediaMetadataRetriever,
     * ExoPlayer instances). Must be called when the owning [EditorSession] is released.
     */
    fun release()

    // ---------------------------------------------------------------------------
    // Future operations — add here for Phase 2+
    // ---------------------------------------------------------------------------
    // suspend fun crop(uri: Uri, cropInfo: CropInfo, outputPath: String): Uri
    // suspend fun rotate(uri: Uri, degrees: Int, outputPath: String): Uri
    // suspend fun export(uri: Uri, config: ExportConfig, outputPath: String): Uri
}

/**
 * Holds the metadata of a loaded video.
 *
 * @param uri         Original URI of the video.
 * @param durationMs  Total duration in milliseconds.
 * @param widthPx     Width in pixels of the video stream.
 * @param heightPx    Height in pixels of the video stream.
 */
data class VideoInfo(
    val uri: Uri,
    val durationMs: Long,
    val widthPx: Int = 0,
    val heightPx: Int = 0,
)

/**
 * Thrown by [VideoProcessor] implementations when a processing operation fails.
 */
class VideoProcessorException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
