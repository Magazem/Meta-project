# Annotation: android/app/src/main/java/com/metalens/app/wearables/WearablesViewModel.kt

Purpose
- Manages discovery, connection, and short-lived sessions with Meta wearable devices.

Key points
- Scans for devices and exposes device list via LiveData/StateFlow for the UI.
- `startStreamSession(...)` begins a streaming session (video + telemetry) using the wearable SDK.
- Prepares short-lived sessions for instant photo capture using `mwdat-camera` APIs.
- Handles reconnection, lifecycle, and error states; coordinates with `StreamViewModel` for frame delivery.

Notes / Next steps
- Trace where `Wearables.startStreamSession` calls into the `mwdat` library to document lifecycle and threading expectations.
