package com.metalens.app.stream

import android.graphics.Bitmap
import com.meta.wearable.dat.camera.types.StreamSessionState

/** UI state for streaming screen. Includes publisher selection and publish target. */
data class StreamUiState(
    val streamSessionState: StreamSessionState = StreamSessionState.STOPPED,
    val videoFrame: Bitmap? = null,
    val frameCount: Long = 0,
    val recentError: String? = null,

    // Publisher selection and publishing state
    val selectedPublisher: PublisherBackend = PublisherBackend.PLACEHOLDER,
    val publishTargetUrl: String = "",
    val isPublishing: Boolean = false,
)

