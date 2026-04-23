package com.metalens.app.stream

enum class PublisherBackend(val displayName: String, val description: String) {
    PLACEHOLDER(
        "Placeholder (Local)",
        "Writes frames to local cache for development and testing (no network).",
    ),
    RTMP(
        "RTMP (MediaCodec → RTMP)",
        "Hardware-accelerated H.264/AAC encoding and RTMP push; compact and efficient for mobile.",
    ),
    FFMPEG(
        "FFmpeg (ffmpeg-kit)",
        "Flexible transcoding, overlays and filter support; larger native binaries and CPU intensive.",
    ),
    CLOUD(
        "Cloud Relay",
        "Offload encoding and publishing to a remote server; good for unreliable uplinks.",
    ),
}
