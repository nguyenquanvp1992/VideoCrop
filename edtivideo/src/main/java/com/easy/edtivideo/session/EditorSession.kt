package com.easy.edtivideo.session

import android.net.Uri
import com.easy.edtivideo.domain.model.EditorState
import com.easy.edtivideo.domain.processor.VideoProcessor
import com.easy.edtivideo.domain.processor.VideoProcessorException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditorSession(val videoUri: Uri, private val videoProcessor: VideoProcessor) {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Default)
    private val _state = MutableStateFlow<EditorState>(EditorState.Loading)

    val state: StateFlow<EditorState> = _state.asStateFlow()

    fun loadVideo() {
        if (_state.value is EditorState.Idle) return

        scope.launch {
            _state.value = EditorState.Loading
            runCatching {
                videoProcessor.loadVideoInfo(videoUri)
            }.onSuccess { info ->
                _state.value = EditorState.Idle(
                    videoUri = info.uri,
                    videoDurationMs = info.durationMs,
                )
            }.onFailure { cause ->
                _state.value = EditorState.Error(
                    message = cause.message ?: "Failed to load video",
                    cause = cause as? VideoProcessorException ?: cause,
                )
            }
        }
    }

    fun release() {
        videoProcessor.release()
        scope.cancel()
    }

    // -------------------------------------------------------------------------
    // Future editing commands — add here for Phase 2+
    // -------------------------------------------------------------------------

    // fun applyCrop(cropInfo: CropInfo) { TODO("Phase 2") }
    // fun applyRotate(degrees: Int) { TODO("Phase 2") }
    // fun addText(textConfig: TextConfig) { TODO("Phase 2") }
    // fun trimVideo(startMs: Long, endMs: Long) { TODO("Phase 2") }
    // fun exportVideo(outputPath: String) { TODO("Phase 2") }
}