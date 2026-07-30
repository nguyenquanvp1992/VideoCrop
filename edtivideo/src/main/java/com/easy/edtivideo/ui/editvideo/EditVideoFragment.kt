package com.easy.edtivideo.ui.editvideo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.easy.edtivideo.databinding.FragmentEditVideoBinding
import com.easy.edtivideo.di.EditVideoEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

/**
 * The main UI component of the EditVideo module.
 *
 * Architectural rules enforced here:
 *  - NO business logic — all logic lives in [EditVideoViewModel] or [EditorSession].
 *  - NO direct reference to [EditorSession] — the Fragment only knows [EditVideoViewModel].
 *  - Obtains [EditorSession] via Hilt Entry Point → [EditVideoViewModelFactory],
 *    so the Fragment is NOT aware of how the session is created or stored.
 *  - Uses [repeatOnLifecycle] for safe Flow collection that stops on STOPPED
 *    and resumes on STARTED, preventing updates to a non-visible UI.
 *
 * How to embed in ParentFragment (Java):
 * ```java
 * // 1. Create session and store in holder BEFORE navigating
 * EditorSession session = factory.create(videoUri);
 * sessionHolder.setSession(session);
 * session.loadVideo();
 *
 * // 2. Navigate / add fragment
 * getSupportFragmentManager()
 *     .beginTransaction()
 *     .replace(R.id.container, EditVideoFragment.newInstance())
 *     .commit();
 *
 * // 3. Release on dismiss
 * session.release();
 * sessionHolder.clearSession();
 * ```
 *
 * Future UI extensions (Phase 2+):
 *  - Add crop overlay view → observe [EditVideoUiState.isCropping]
 *  - Add bottom toolbar buttons → call viewModel.onCropClicked(), etc.
 */
class EditVideoFragment : Fragment() {

    companion object {
        fun newInstance(): EditVideoFragment = EditVideoFragment()
    }

    private var _binding: FragmentEditVideoBinding? = null
    private val binding get() = requireNotNull(_binding) { "Binding accessed after onDestroyView" }

    private lateinit var viewModel: EditVideoViewModel

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setupViewModel(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUiState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // -------------------------------------------------------------------------
    // ViewModel setup via Hilt Entry Point
    // -------------------------------------------------------------------------

    /**
     * Resolves [EditorSession] from the Activity-scoped [SessionHolder] via
     * [EditVideoEntryPoint], then wires up [EditVideoViewModel] through the
     * custom factory.
     *
     * Called in [onAttach] so the ViewModel is ready before [onCreateView].
     *
     * Why [EntryPointAccessors.fromActivity] and not field injection?
     * - [EditorSession] is a runtime object (created per video), not compile-time.
     * - Hilt cannot inject runtime objects directly into Fragment fields.
     * - The Entry Point pattern lets us pull the Activity-scoped [SessionHolder]
     *   from the Hilt graph and retrieve the current session from it.
     */
    private fun setupViewModel(context: Context) {
        val entryPoint = EntryPointAccessors.fromActivity(
            requireActivity(),
            EditVideoEntryPoint::class.java,
        )
        val session = entryPoint.sessionHolder().requireSession()
        val factory = EditVideoViewModelFactory(session)
        viewModel = ViewModelProvider(this, factory)[EditVideoViewModel::class.java]
    }

    // -------------------------------------------------------------------------
    // State observation
    // -------------------------------------------------------------------------

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            // repeatOnLifecycle ensures collection is active only when the
            // Fragment is at least STARTED — no updates to off-screen UI.
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Rendering — pure UI, no logic
    // -------------------------------------------------------------------------

    private fun render(state: EditVideoUiState) {
        with(binding) {
            // Loading indicator
            progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            // Preview container — shown only when video is ready
            layoutPreview.visibility = if (state.isReady) View.VISIBLE else View.GONE

            // Error view
            tvError.visibility = if (state.hasError) View.VISIBLE else View.GONE
            tvError.text = state.errorMessage.orEmpty()
        }

        // Future Phase 2+ rendering:
        // if (state.isCropping) showCropOverlay(state.cropInfo)
        // if (state.isExporting) showExportProgress(state.exportProgress)
    }
}
