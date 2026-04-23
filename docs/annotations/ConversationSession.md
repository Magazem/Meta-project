# Annotation: android/app/src/main/java/com/metalens/app/conversation/ConversationSession.kt

Purpose
- Orchestrates a live conversation session: routes mic audio to OpenAI Realtime, receives assistant events/audio, and manages turn state.

Key points
- Starts/stops mic capture and web socket connection lifecycle with `OpenAIRealtimeClient`.
- Handles local interruptions (user stops speaking or taps to interrupt) and coordinates replay/playback of assistant audio.
- Maintains conversation state (list of turns, partial transcriptions, detected end-of-turn events) for UI updates.
- Ensures audio format and chunking match the Realtime API (PCM16 mono @ 24 kHz), and may buffer/resample as needed.

Notes / Next steps
- Trace error/reconnect flows to make sure partial audio is not lost on transient disconnects.
- Add small sequence diagram documenting mic -> websocket -> playback flow and where turn_detection messages are emitted.

---

## Session Summary (checkpoint 2026-04-23)

- Goal: get the app building locally, add a streaming-middleman UI, and enable emulator-friendly testing without physical glasses.
- High-level changes in this checkpoint:
	- UI: added a `Stream` screen with a publisher backend dropdown and target URL field.
	- Home: temporarily enabled `Start Streaming` on the Home screen so the Stream UI can be opened on an emulator.
	- StreamScreen: skip wearable camera permission auto-exit in debug builds so developer UX is testable on emulators.
	- Stream pipeline: introduced `StreamOutput` interface and a placeholder `RtmpPublisher` that writes JPEG frames to `cache/rtmp_frames`.
	- Realtime/debug: added `MockOpenAIRealtimeClient` for conversation testing without account-limited models.
	- Audio: added runtime `AudioRecord` permission handling and a Bluetooth SCO start timeout/fallback in `AudioIoController`.
	- Build: produced a debug APK and installed it on the emulator (used absolute `adb.exe` path on Windows).

How to test on an emulator
1. Open the app, go to Home → Start Streaming (now enabled on emulator).
2. On the Stream screen select a publisher backend, set a target URL (placeholder backend writes frames to cache).
3. Start publishing — inspect app cache (`/data/data/com.metalens.app/cache/rtmp_frames`) via `adb shell` to see JPEG frames.

Remaining work
- Integrate a real RTMP publisher (MediaCodec → RTMP or FFmpeg), adaptive bitrate and reconnect/backoff, overlays and chat/alert widgets, and local recording.

Files changed in this checkpoint (high level)
- `android/app/src/main/java/com/metalens/app/ui/screens/HomeScreen.kt` (force-enable Start Streaming for emulator)
- `android/app/src/main/java/com/metalens/app/ui/screens/StreamScreen.kt` (skip wearable permission auto-exit in debug)
- `android/app/src/main/java/com/metalens/app/stream/` (added `StreamViewModel` wiring, `PublisherBackend`, `StreamOutput`, `RtmpPublisher` scaffold)
- `android/app/src/main/java/com/metalens/app/conversation/` (added `MockOpenAIRealtimeClient`, adjusted `OpenAIRealtimeClient` for mocking)
- `android/app/src/main/java/com/metalens/app/conversation/AudioIoController.kt` (mic permission + SCO fallback)

Quick restore notes
- Do NOT push API keys or secrets. Use `gradle.properties` or environment variables for keys.
- To restore work on another machine, clone the repo and open `docs/annotations/ConversationSession.md` — the `Session Summary` section (above) contains the checkpoint context to paste into the assistant chat if needed.

---

