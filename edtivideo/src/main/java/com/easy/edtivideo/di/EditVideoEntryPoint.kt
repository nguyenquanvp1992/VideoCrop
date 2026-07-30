package com.easy.edtivideo.di

import com.easy.edtivideo.session.SessionHolder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

/**
 * Hilt Entry Point that grants [EditVideoFragment] access to the [SessionHolder]
 * without requiring the Fragment to be annotated with @AndroidEntryPoint.
 *
 * Why @InstallIn(ActivityComponent::class)?
 * - [EntryPointAccessors.fromActivity] requires the Entry Point to be installed
 *   in [ActivityComponent] so Hilt can locate it from the Activity's component.
 * - [SessionHolder] is @Singleton so it is accessible from any Hilt component,
 *   including [ActivityComponent].
 *
 * Why NOT @AndroidEntryPoint on EditVideoFragment directly?
 * - The module is a library that is embedded into a host Activity.
 * - Using @AndroidEntryPoint on the Fragment would require the host Activity
 *   to also be @AndroidEntryPoint, creating an unwanted coupling.
 * - The Entry Point pattern keeps the Fragment injection self-contained.
 *
 * Usage inside [EditVideoFragment]:
 * ```kotlin
 * val entryPoint = EntryPointAccessors.fromActivity(
 *     requireActivity(),
 *     EditVideoEntryPoint::class.java,
 * )
 * val session = entryPoint.sessionHolder().requireSession()
 * ```
 */
@EntryPoint
@InstallIn(ActivityComponent::class)
interface EditVideoEntryPoint {

    /**
     * Exposes the singleton [SessionHolder] so [EditVideoFragment] can
     * retrieve the current [com.easy.edtivideo.session.EditorSession].
     */
    fun sessionHolder(): SessionHolder
}
