# Annotation: android/app/src/main/java/com/metalens/app/stream/StreamViewModel.kt

Purpose
- Responsible for ingesting raw frames from wearables and producing UI-friendly bitmaps/previews.

Key points
- Receives I420 video frames, converts to NV21, then JPEG for Compose preview.
- Utility methods: `decodeToBitmap(...)`, `convertI420toNV21(...)` — critical for performance-sensitive paths.
- Exposes a flow/state of the latest preview `Bitmap` for UI consumption.

Notes / Next steps
- Consider offloading conversion to a specialized thread or using hardware codecs if CPU-bound.
