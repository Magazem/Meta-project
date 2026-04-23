# Annotation: android/app/src/main/java/com/metalens/app/pictureanalysis/OpenAIImageAnalysisService.kt

Purpose
- Performs single-shot image analysis using the OpenAI Responses API (HTTP) to analyze camera frames and return structured responses.

Key points
- Encodes a JPEG frame as a data URL and sends it with a text prompt to the Responses endpoint (or other image-capable endpoint).
- Parses the API response for extracted text, labels, and recommended actions; surface results to the UI via a simple API.
- Handles timeouts, retries, and error mapping for UX-friendly messages.

Notes / Next steps
- Document the exact HTTP request body and expected response schema for maintainability.
- Add rate-limiting or debounce logic to avoid sending every frame when doing live previews.
