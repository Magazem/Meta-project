package com.metalens.app.conversation

import android.util.Log
import kotlin.concurrent.thread

/**
 * Simple mock Realtime client used for debug builds so the UI and audio flows can be tested
 * without an actual OpenAI Realtime backend or an account with access to preview models.
 */
class MockOpenAIRealtimeClient(
    apiKey: String,
    model: String = OpenAIRealtimeClient.DEFAULT_MODEL,
    voice: String = OpenAIRealtimeClient.DEFAULT_VOICE,
    instructions: String? = null,
    onConnected: () -> Unit,
    onDisconnected: (reason: String) -> Unit,
    onError: (message: String) -> Unit,
    onAudioDelta: (pcm16: ByteArray) -> Unit,
    onAssistantTextDelta: (delta: String) -> Unit,
    onUserUtteranceStarted: (itemId: String) -> Unit,
    onUserUtteranceStopped: (itemId: String, durationMs: Long) -> Unit,
    onUserTranscript: (itemId: String, transcript: String) -> Unit,
    onAssistantResponseStarted: (responseId: String?) -> Unit,
    onAssistantResponseDone: () -> Unit,
    onInputSpeechState: (isSpeeching: Boolean) -> Unit,
) : OpenAIRealtimeClient(
    apiKey,
    model,
    voice,
    instructions,
    onConnected,
    onDisconnected,
    onError,
    onAudioDelta,
    onAssistantTextDelta,
    onUserUtteranceStarted,
    onUserUtteranceStopped,
    onUserTranscript,
    onAssistantResponseStarted,
    onAssistantResponseDone,
    onInputSpeechState,
) {
    private val TAG = "MockOpenAIRealtimeClient"

    override fun connect() {
        Log.d(TAG, "Mock connect: simulating connected")
        try {
            onConnected()
        } catch (_: Throwable) {
            // ignore
        }
    }

    override fun close() {
        Log.d(TAG, "Mock close")
        try {
            onDisconnected("mock")
        } catch (_: Throwable) {
            // ignore
        }
    }

    override fun sendPcm16Audio(pcm16: ByteArray) {
        // no-op for mock
    }

    override fun sendUserText(text: String) {
        // Simulate a quick textual assistant response on a background thread.
        thread {
            try {
                Thread.sleep(200)
                onAssistantTextDelta("(mock) Echo: $text")
                onAssistantResponseDone()
            } catch (_: Throwable) {
                // ignore
            }
        }
    }

    override fun cancelActiveResponse() {
        // no-op for mock
    }
}
