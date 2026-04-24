package com.metalens.app.stream.output

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.util.Log
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class H264FilePublisher(private val context: Context) : StreamOutput {
    companion object {
        private const val TAG = "H264FilePublisher"
        private const val MIME_TYPE = "video/avc"
        private const val FRAME_RATE = 15
        private const val IFRAME_INTERVAL = 2
        private const val BIT_RATE = 2_000_000
        private const val TIMEOUT_US = 10_000L
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var codec: MediaCodec? = null
    private var muxer: MediaMuxer? = null
    private var outputTrack = -1
    private var muxerStarted = false
    private var frameIndex = 0L
    private var colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
    private var outputFile: File? = null
    private var running = AtomicBoolean(false)

    override val isRunning: Boolean
        get() = running.get()

    override fun start(targetUrl: String) {
        if (running.get()) return
        outputFile = resolveOutputFile(targetUrl)
        running.set(true)
        Log.i(TAG, "Local H.264 recording will write to: ${outputFile?.absolutePath}")
    }

    override fun stop() {
        if (!running.get()) return
        scope.launch {
            drainEncoder(endOfStream = true)
            releaseEncoder()
            running.set(false)
        }
    }

    override fun sendFrame(bitmap: Bitmap) {
        if (!running.get()) return
        scope.launch {
            try {
                if (codec == null) {
                    val outputFile = outputFile ?: resolveOutputFile("metalens_stream.mp4")
                    if (!prepareEncoder(outputFile, bitmap.width, bitmap.height)) {
                        Log.e(TAG, "Failed to prepare encoder after first frame")
                        return@launch
                    }
                }
                if (bitmap.width != codec?.outputFormat?.getInteger(MediaFormat.KEY_WIDTH) ||
                    bitmap.height != codec?.outputFormat?.getInteger(MediaFormat.KEY_HEIGHT)
                ) {
                    Log.w(TAG, "Dropped frame because size changed: " +
                        "expected ${codec?.outputFormat?.getInteger(MediaFormat.KEY_WIDTH)}x${codec?.outputFormat?.getInteger(MediaFormat.KEY_HEIGHT)} " +
                        "but got ${bitmap.width}x${bitmap.height}")
                    return@launch
                }

                val inputBufferIndex = codec?.dequeueInputBuffer(TIMEOUT_US) ?: -1
                if (inputBufferIndex >= 0) {
                    val inputBuffer = getInputBuffer(inputBufferIndex)
                    if (inputBuffer != null) {
                        inputBuffer.clear()
                        val yuv = convertBitmapToYuv(bitmap)
                        inputBuffer.put(yuv)
                        val presentationTimeUs = computePresentationTimeUs(frameIndex++)
                        codec?.queueInputBuffer(inputBufferIndex, 0, yuv.size, presentationTimeUs, 0)
                    }
                }
                drainEncoder(endOfStream = false)
            } catch (t: Throwable) {
                Log.e(TAG, "Error encoding frame", t)
            }
        }
    }

    private fun resolveOutputFile(targetUrl: String): File {
        val file = run {
            val candidate = File(targetUrl)
            if (candidate.isAbsolute) candidate else File(context.cacheDir, targetUrl)
        }
        if (file.parentFile?.exists() != true) {
            file.parentFile?.mkdirs()
        }
        if (!file.name.endsWith(".mp4")) {
            return File(file.parentFile, file.name + ".mp4")
        }
        return file
    }

    private fun prepareEncoder(outputFile: File, width: Int, height: Int): Boolean {
        return try {
            if (codec != null) {
                releaseEncoder()
            }

            colorFormat = selectColorFormat()
            val format = MediaFormat.createVideoFormat(MIME_TYPE, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
                setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
                setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL)
            }

            codec = MediaCodec.createEncoderByType(MIME_TYPE).apply {
                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                start()
            }

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            outputTrack = -1
            muxerStarted = false
            frameIndex = 0
            true
        } catch (t: Throwable) {
            Log.e(TAG, "prepareEncoder failed", t)
            releaseEncoder()
            false
        }
    }

    private fun selectColorFormat(): Int {
        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val codecInfo = codecList.codecInfos.firstOrNull { info ->
            info.isEncoder && info.supportedTypes.any { it.equals(MIME_TYPE, ignoreCase = true) }
        } ?: return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar

        val caps = codecInfo.getCapabilitiesForType(MIME_TYPE)
        return when {
            caps.colorFormats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible) ->
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
            caps.colorFormats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) ->
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
            caps.colorFormats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) ->
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar
            else -> caps.colorFormats.firstOrNull() ?: MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
        }
    }

    private fun computePresentationTimeUs(frameIndex: Long): Long {
        return frameIndex * 1_000_000L / FRAME_RATE
    }

    private fun drainEncoder(endOfStream: Boolean) {
        codec?.let { codec ->
            if (endOfStream) {
                val inputBufferIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                if (inputBufferIndex >= 0) {
                    codec.queueInputBuffer(inputBufferIndex, 0, 0, computePresentationTimeUs(frameIndex), MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                }
            }

            val bufferInfo = MediaCodec.BufferInfo()
            while (true) {
                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (!endOfStream) break
                    continue
                }
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val newFormat = codec.outputFormat
                    outputTrack = muxer?.addTrack(newFormat) ?: -1
                    muxer?.start()
                    muxerStarted = true
                    continue
                }
                if (outputBufferIndex < 0) {
                    continue
                }

                val encodedData = getOutputBuffer(outputBufferIndex) ?: continue
                if (bufferInfo.size != 0 && muxerStarted && outputTrack >= 0) {
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    muxer?.writeSampleData(outputTrack, encodedData, bufferInfo)
                }
                codec.releaseOutputBuffer(outputBufferIndex, false)

                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    break
                }
            }
        }
    }

    private fun releaseEncoder() {
        try {
            codec?.stop()
        } catch (_: Throwable) {
        }
        try {
            codec?.release()
        } catch (_: Throwable) {
        }
        codec = null

        try {
            muxer?.stop()
        } catch (_: Throwable) {
        }
        try {
            muxer?.release()
        } catch (_: Throwable) {
        }
        muxer = null
        outputTrack = -1
        muxerStarted = false
    }

    private fun getInputBuffer(index: Int): ByteBuffer? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            codec?.getInputBuffer(index)
        } else {
            codec?.inputBuffers?.get(index)
        }
    }

    private fun getOutputBuffer(index: Int): ByteBuffer? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            codec?.getOutputBuffer(index)
        } else {
            codec?.outputBuffers?.get(index)
        }
    }

    private fun convertBitmapToYuv(bitmap: Bitmap): ByteArray {
        return when (colorFormat) {
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar ->
                convertBitmapToI420(bitmap)
            else ->
                convertBitmapToNV12(bitmap)
        }
    }

    private fun convertBitmapToNV12(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val frameSize = width * height
        val yuv = ByteArray(frameSize * 3 / 2)
        val pixels = IntArray(frameSize)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var yIndex = 0
        var uvIndex = frameSize

        for (j in 0 until height) {
            for (i in 0 until width) {
                val color = pixels[j * width + i]
                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF
                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yuv[yIndex++] = clamp(y)
            }
        }

        for (j in 0 until height step 2) {
            for (i in 0 until width step 2) {
                var sumU = 0
                var sumV = 0
                for (subJ in 0..1) {
                    for (subI in 0..1) {
                        val color = pixels[(j + subJ) * width + (i + subI)]
                        val r = (color shr 16) and 0xFF
                        val g = (color shr 8) and 0xFF
                        val b = color and 0xFF
                        sumU += ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                        sumV += ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    }
                }
                yuv[uvIndex++] = clamp(sumU / 4)
                yuv[uvIndex++] = clamp(sumV / 4)
            }
        }

        return yuv
    }

    private fun convertBitmapToI420(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val frameSize = width * height
        val yuv = ByteArray(frameSize * 3 / 2)
        val pixels = IntArray(frameSize)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var yIndex = 0
        var uIndex = frameSize
        var vIndex = frameSize + frameSize / 4

        for (j in 0 until height) {
            for (i in 0 until width) {
                val color = pixels[j * width + i]
                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF
                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yuv[yIndex++] = clamp(y)
            }
        }

        for (j in 0 until height step 2) {
            for (i in 0 until width step 2) {
                var sumU = 0
                var sumV = 0
                for (subJ in 0..1) {
                    for (subI in 0..1) {
                        val color = pixels[(j + subJ) * width + (i + subI)]
                        val r = (color shr 16) and 0xFF
                        val g = (color shr 8) and 0xFF
                        val b = color and 0xFF
                        sumU += ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                        sumV += ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    }
                }
                yuv[uIndex++] = clamp(sumU / 4)
                yuv[vIndex++] = clamp(sumV / 4)
            }
        }

        return yuv
    }

    private fun clamp(value: Int): Byte {
        return when {
            value < 0 -> 0
            value > 255 -> 255.toByte()
            else -> value.toByte()
        }
    }
}
