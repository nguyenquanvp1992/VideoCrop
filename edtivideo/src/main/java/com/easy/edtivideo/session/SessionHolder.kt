package com.easy.edtivideo.session

import javax.inject.Inject

/**
 * Activity-scoped bridge that carries the active [EditorSession] from
 * ParentFragment (Java) into [EditVideoFragment] (Kotlin) without using
 * SharedViewModel or passing it through Fragment arguments.
 *
 * Lifecycle contract:
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  ParentFragment                                                       │
 * │      session = factory.create(uri)        ← create                   │
 * │      sessionHolder.setSession(session)    ← store                    │
 * │      session.loadVideo()                  ← trigger load             │
 * │                                                                       │
 * │  EditVideoFragment (via Hilt Entry Point)                             │
 * │      session = sessionHolder.requireSession()  ← retrieve            │
 * │      ViewModel(session)                        ← wire up             │
 * │                                                                       │
 * │  ParentFragment (on dismiss / back)                                   │
 * │      session.release()                    ← cleanup                  │
 * │      sessionHolder.clearSession()         ← prevent stale reference  │
 * └──────────────────────────────────────────────────────────────────────┘
 *
 * Why @ActivityScoped?
 * - Lives as long as the Activity → survives Fragment transactions and
 *   configuration changes without leaking the session.
 * - Destroyed with the Activity → no manual cleanup needed for the holder itself.
 * - The session inside still needs explicit [EditorSession.release] by the
 *   caller to cancel the coroutine scope and free media resources.
 *
 * Why NOT a singleton?
 * - A singleton would survive across Activity instances in multi-activity apps,
 *   leading to stale sessions and potential memory leaks.
 */
class SessionHolder @Inject constructor() {

    @Volatile
    private var currentSession: EditorSession? = null

    /**
     * Stores the active [EditorSession].
     * Called by ParentFragment after [EditorSessionFactory.create].
     */
    fun setSession(session: EditorSession) {
        currentSession = session
    }

    /**
     * Returns the current [EditorSession], or `null` if none has been set.
     */
    fun getSession(): EditorSession? = currentSession

    /**
     * Returns the current [EditorSession].
     *
     * @throws IllegalStateException if no session has been set. This signals a
     *         programming error — EditVideoFragment should never be shown before
     *         ParentFragment calls [setSession].
     */
    fun requireSession(): EditorSession =
        currentSession
            ?: error(
                "No active EditorSession found. " +
                    "ParentFragment must call SessionHolder.setSession() " +
                    "before navigating to EditVideoFragment.",
            )

    /**
     * Clears the stored session reference.
     * Called by ParentFragment after [EditorSession.release] to prevent
     * the holder from keeping a reference to a released session.
     */
    fun clearSession() {
        currentSession = null
    }
}
