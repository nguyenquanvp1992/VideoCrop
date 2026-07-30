package com.easy.edtivideo.di

import com.easy.edtivideo.domain.processor.VideoProcessor
import com.easy.edtivideo.infrastructure.Media3VideoProcessor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module that binds the [VideoProcessor] interface to its concrete
 * implementation [Media3VideoProcessor].
 *
 * Why @Binds instead of @Provides?
 * - @Binds is more efficient: Hilt generates a direct delegate rather than
 *   a wrapper method, reducing generated code size.
 * - Used when the implementation is a concrete class that Hilt can instantiate
 *   via its own @Inject constructor.
 *
 * Why NOT @Singleton on the VideoProcessor binding?
 * - [VideoProcessor] instances are vended through [javax.inject.Provider] in
 *   [EditorSessionFactory], which creates a new instance per session.
 * - Making it a Singleton here would defeat that design — all sessions would
 *   share one processor and risk state interference.
 * - The lifecycle of each [VideoProcessor] is managed by its owning [EditorSession].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class VideoProcessorModule {

    @Binds
    abstract fun bindVideoProcessor(
        impl: Media3VideoProcessor,
    ): VideoProcessor
}
