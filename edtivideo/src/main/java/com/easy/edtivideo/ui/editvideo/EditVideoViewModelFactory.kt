package com.easy.edtivideo.ui.editvideo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easy.edtivideo.session.EditorSession

/**
 * Custom [ViewModelProvider.Factory] for [EditVideoViewModel].
 *
 * Why a custom factory instead of Hilt's @HiltViewModel?
 * - [EditVideoViewModel] takes [EditorSession] as a constructor parameter.
 * - [EditorSession] is created at runtime (per video), not at compile time,
 *   so it cannot be bound in a Hilt module directly.
 * - This factory receives the session at construction time and passes it to
 *   the ViewModel, bridging runtime data with Hilt's compile-time graph.
 *
 * Usage inside [EditVideoFragment]:
 * ```kotlin
 * val factory = EditVideoViewModelFactory(session)
 * viewModel = ViewModelProvider(this, factory)[EditVideoViewModel::class.java]
 * ```
 *
 * @param session The active [EditorSession] to inject into [EditVideoViewModel].
 */
class EditVideoViewModelFactory(
    private val session: EditorSession,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(EditVideoViewModel::class.java)) {
            "EditVideoViewModelFactory can only create EditVideoViewModel. " +
                "Requested: ${modelClass.name}"
        }
        return EditVideoViewModel(session) as T
    }
}
