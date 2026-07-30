package com.easy.edtivideo.session

import android.net.Uri
import com.easy.edtivideo.domain.processor.VideoProcessor
import javax.inject.Inject
import javax.inject.Provider

/**
 * Factory that creates one [EditorSession] per video.
 *
 * Why a Factory instead of a Singleton session?
 * - The user can open multiple videos in a single app lifetime.
 * - Each video requires independent state, its own coroutine scope,
 *   and its own [VideoProcessor] instance.
 * - Singleton session would require explicit reset logic and risks
 *   stale state across videos.
 *
 * Why [Provider] instead of direct [VideoProcessor] injection?
 * - [Provider] defers instantiation until [create] is called, so a new
 *   [VideoProcessor] instance is obtained for each session.
 * - This satisfies Hilt's requirement: [EditorSessionFactory] itself is
 *   @Singleton (created once), but each call to [create] retrieves a fresh
 *   [VideoProcessor] from the provider.
 *
 * Java interoperability:
 * - This class is designed so ParentFragment (Java) can inject and call it
 *   without any Kotlin-specific syntax:
 *
 *   ```java
 *   @Inject EditorSessionFactory factory;
 *
 *   void onVideoSelected(Uri uri) {
 *       EditorSession session = factory.create(uri);
 *       sessionHolder.setSession(session);
 *       session.loadVideo();
 *   }
 *   ```
 */
class EditorSessionFactory @Inject constructor(
    private val videoProcessorProvider: Provider<VideoProcessor>,
) {

    /**
     * Creates a new [EditorSession] for the given video URI.
     *
     * Each call returns an independent session with its own coroutine scope
     * and [VideoProcessor] instance. The caller is responsible for calling
     * [EditorSession.release] when editing is finished.
     *
     * @param videoUri URI of the video to be edited (content:// or file://).
     * @return         A fresh [EditorSession] in [EditorState.Loading] state.
     */
    fun create(videoUri: Uri): EditorSession {
        return EditorSession(
            videoUri = videoUri,
            videoProcessor = videoProcessorProvider.get(),
        )
    }
}
