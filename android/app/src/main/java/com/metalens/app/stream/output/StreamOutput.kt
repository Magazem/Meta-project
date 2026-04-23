package com.metalens.app.stream.output

import android.graphics.Bitmap

/**
 * Abstract output sink for streaming video frames (and later audio).
 * Implementations may encode + push to RTMP, SRT, WebRTC, or write to disk for testing.
 */
interface StreamOutput {
    /** Start publishing to the given target (for RTMP this is the ingest URL including key). */
    fun start(targetUrl: String)

    /** Stop publishing and release resources. */
    fun stop()

    /** Send a single decoded video frame to the output. Caller provides a post-rotation, display-ready Bitmap. */
    fun sendFrame(bitmap: Bitmap)

    /** Whether the output is currently publishing. */
    val isRunning: Boolean
}
