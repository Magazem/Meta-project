package com.metalens.app.stream.output

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

/**
 * Placeholder RTMP publisher implementation.
 *
 * NOTE: this is a scaffold. It currently writes low-quality JPEG frames to the app cache
 * so you can verify frames are forwarded. Replace internal implementation with a real
 * encoder + RTMP client (for example `rtmp-rtsp-stream-client-java` or `ffmpeg-kit`) later.
 */
class RtmpPublisher(private val context: Context) : StreamOutput {
    companion object {
        private const val TAG = "RtmpPublisher"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    @Volatile private var running = false
    private var targetUrl: String? = null
    private var frameCounter = 0L

    override val isRunning: Boolean
        get() = running

    override fun start(targetUrl: String) {
        if (running) return
        this.targetUrl = targetUrl
        running = true
        Log.i(TAG, "(placeholder) start publishing -> $targetUrl")
        // TODO: initialize encoder + RTMP client here
    }

    override fun stop() {
        if (!running) return
        running = false
        scope.coroutineContext.cancelChildren()
        Log.i(TAG, "(placeholder) stop publishing")
        // TODO: tear down encoder + RTMP client here
    }

    override fun sendFrame(bitmap: Bitmap) {
        if (!running) return
        val index = frameCounter++
        scope.launch {
            try {
                val cache = File(context.cacheDir, "rtmp_frames")
                if (!cache.exists()) cache.mkdirs()
                val file = File(cache, "frame_$index.jpg")
                FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 40, out) }
                Log.d(TAG, "Wrote placeholder frame: ${file.absolutePath}")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to write placeholder frame", t)
            }
        }
    }
}
