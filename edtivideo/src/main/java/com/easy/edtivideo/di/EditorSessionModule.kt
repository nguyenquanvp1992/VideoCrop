package com.easy.edtivideo.di

import com.easy.edtivideo.session.EditorSessionFactory
import com.easy.edtivideo.session.SessionHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import com.easy.edtivideo.domain.processor.VideoProcessor

/**
 * Hilt module that wires together the session-management layer.
 *
 * Binding decisions:
 *
 *  1. [EditorSessionFactory] — @Singleton
 *     The factory itself is stateless (it only holds a [Provider]).
 *     Creating it once and reusing it across the app is correct and efficient.
 *
 *  2. [SessionHolder] — @Singleton
 *     A single holder per application is sufficient because only one editing
 *     session is active at a time. The holder is effectively a lightweight,
 *     thread-safe reference cell.
 *     Note: If the app ever needs multi-window / split-screen editing, this
 *     should be promoted to @ActivityScoped.
 *
 * What is NOT a singleton:
 *  - [com.easy.edtivideo.session.EditorSession] — created at runtime by the
 *    factory, one per video, never registered with Hilt.
 *  - [VideoProcessor] — vended via Provider so each session gets its own
 *    instance (see [VideoProcessorModule]).
 */
@Module
@InstallIn(SingletonComponent::class)
object EditorSessionModule {

    /**
     * Provides the singleton [EditorSessionFactory].
     *
     * [Provider<VideoProcessor>] is injected automatically by Hilt.
     * Each call to [Provider.get] returns a fresh [VideoProcessor] instance,
     * which is exactly what [EditorSessionFactory.create] needs.
     */
    @Provides
    @Singleton
    fun provideEditorSessionFactory(
        videoProcessorProvider: Provider<VideoProcessor>,
    ): EditorSessionFactory = EditorSessionFactory(videoProcessorProvider)

    /**
     * Provides the singleton [SessionHolder].
     *
     * This is the shared bridge between ParentFragment (Java) and
     * [EditVideoFragment] (Kotlin). ParentFragment writes the session here;
     * the Fragment reads it via [EditVideoEntryPoint].
     */
    @Provides
    @Singleton
    fun provideSessionHolder(): SessionHolder = SessionHolder()
}
