package com.easy.edtivideo.infrastructure

import android.net.Uri
import com.easy.edtivideo.domain.processor.VideoInfo
import com.easy.edtivideo.domain.processor.VideoProcessor
import com.easy.edtivideo.domain.processor.VideoProcessorException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Concrete implementation of [VideoProcessor] backed by AndroidX Media3.
 *
 * Phase 1 scope:
 *  - Only [loadVideoInfo] is implemented (returns a stub).
 *  - All other operations are left as TODO for Phase 2+.
 *
 * Threading:
 *  - All work is dispatched to [Dispatchers.IO] so callers (on Default dispatcher
 *    inside [EditorSession]) are never blocked.
 *
 * This class lives in the `infrastructure` layer, which is the only layer
 * allowed to depend on Android SDK classes (Media3, MediaMetadataRetriever, etc.).
 * [EditorSession] and all domain classes remain Android-free.
 *
 * @Inject constructor allows Hilt to instantiate this class and satisfy the
 * [VideoProcessor] binding declared in [VideoProcessorModule].
 */
class Media3VideoProcessor @Inject constructor() : VideoProcessor {

    // In Phase 2, inject: @ApplicationContext context: Context
    // to use MediaMetadataRetriever or Media3 APIs.

    override suspend fun loadVideoInfo(uri: Uri): VideoInfo = withContext(Dispatchers.IO) {
        runCatching {
            // TODO Phase 2: use MediaMetadataRetriever or Media3 to read real metadata.
            // Example:
            //   val retriever = MediaMetadataRetriever()
            //   retriever.setDataSource(context, uri)
            //   val duration = retriever.extractMetadata(METADATA_KEY_DURATION)?.toLong() ?: 0L
            //   val width = retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
            //   val height = retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
            //   retriever.release()
            //   VideoInfo(uri = uri, durationMs = duration, widthPx = width, heightPx = height)

            // Phase 1 stub — returns a placeholder VideoInfo so the session
            // can transition to EditorState.Idle and the architecture can be validated.
            VideoInfo(
                uri = uri,
                durationMs = 0L,
                widthPx = 0,
                heightPx = 0,
            )
        }.getOrElse { cause ->
            throw VideoProcessorException(
                message = "Failed to load video info for uri=$uri",
                cause = cause,
            )
        }
    }

    override fun release() {
        // TODO Phase 2: release MediaMetadataRetriever, ExoPlayer, or Transformer instances.
    }
}
