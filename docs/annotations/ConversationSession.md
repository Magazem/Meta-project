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
