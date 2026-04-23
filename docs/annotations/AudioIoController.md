# Annotation: android/app/src/main/java/com/metalens/app/conversation/AudioIoController.kt

Purpose
- Manages device audio I/O: microphone capture, audio playback, and Bluetooth SCO handling for call-like audio routing.

Key points
- Uses `AudioRecord` to capture microphone audio and exposes `startMicCapture(...)` to stream PCM frames to the conversation layer.
- Uses `AudioTrack` for assistant playback; supports writing raw PCM16 mono data returned from the Realtime API.
- May enable Bluetooth SCO for wearable/headset audio routing; must request/handle permissions and audio focus correctly.
- Responsible for resampling and mixing if device sample rate differs from the required 24 kHz for the Realtime API.

Notes / Next steps
- Verify proper handling of audio focus and interruptions (incoming calls, other apps).
- Consider offloading heavy resampling to a background thread or native code for lower latency.

Change log
- **2026-04-23:** Added runtime `RECORD_AUDIO` permission check in `createAndStartAudioRecord()` to address Android Lint `MissingPermission` and to fail-fast with a clear SecurityException when the app hasn't requested audio permission at runtime. See [android/app/src/main/java/com/metalens/app/conversation/AudioIoController.kt](android/app/src/main/java/com/metalens/app/conversation/AudioIoController.kt#L1-L300).

Recommendation
- Ensure the app requests `RECORD_AUDIO` at runtime (for example, add `RECORD_AUDIO` to the permission flow in `MainActivity.kt` or the Wearables permission contract) before calling `startMicCapture()`. See [android/app/src/main/java/com/metalens/app/MainActivity.kt](android/app/src/main/java/com/metalens/app/MainActivity.kt#L1-L120).
