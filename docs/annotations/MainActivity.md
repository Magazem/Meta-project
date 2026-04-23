# Annotation: android/app/src/main/java/com/metalens/app/MainActivity.kt

Purpose
- Entry activity for the app; requests runtime permissions and initializes wearables SDK.

Key points
- Requests permissions (Bluetooth, Microphone, Camera, Location as needed) and handles grant results.
- Calls `Wearables.initialize(this)` early to prepare the Meta wearables integration.
- Hosts the Compose UI and wires `WearablesPermissionRequester` to the UI flow.
- May start foreground services (for streaming/continuous work) defined in the manifest.

Notes / Next steps
- Check the activity lifecycle and scopes used by `Wearables` initialization to avoid leaks.
- Annotate any non-obvious permission fallbacks.
