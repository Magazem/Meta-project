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

Change log
- **2026-04-23:** Added an on-spot microphone pre-permission flow: `requestRecordAudio(onGranted: () -> Unit)` in `MainActivity.kt`. This shows a short in-app dialog explaining why the mic is needed, then launches the OS permission prompt via `ActivityResultContracts.RequestPermission`. On grant, the provided `onGranted` callback is invoked. See [android/app/src/main/java/com/metalens/app/MainActivity.kt](android/app/src/main/java/com/metalens/app/MainActivity.kt#L1-L200).

Recommendation
- Call `requestRecordAudio { /* start mic capture */ }` from the UI at the exact point the user taps the mic button so the request is contextual and less surprising.
