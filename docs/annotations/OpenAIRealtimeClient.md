# Annotation: android/app/src/main/java/com/metalens/app/conversation/OpenAIRealtimeClient.kt

Purpose
- WebSocket client that connects to OpenAI Realtime endpoint to stream audio and receive assistant responses.

Key points
- Uses OkHttp WebSocket to `wss://api.openai.com/v1/realtime` and manages connect/reconnect.
- Audio format: PCM16 mono at 24 kHz (send/receive). Methods: `sendPcm16Audio()`, `sendUserText()`, `sendResponseCreate()`, `sendResponseCancel()`.
- Sends session updates for `turn_detection` and `input_audio_transcription` as JSON frames.
- Handles assistant audio playback, local interruptions, and turn state orchestration.

Notes / Next steps
- Document JSON message shapes used for realtime messages and any rate/size considerations.
