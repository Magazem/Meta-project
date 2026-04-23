# Annotation: android/app/src/main/AndroidManifest.xml

Purpose
- Declares permissions, app components (activities, services, receivers), and important intent filters.

Key points
- Runtime permissions requested in the app: `RECORD_AUDIO`, `BLUETOOTH_CONNECT`/`BLUETOOTH_SCAN`, `INTERNET`, and any camera/location permissions needed by wearables.
- Declares foreground services used for streaming or continuous operation — these require `foregroundServiceType` and proper notification handling on Android 12+.
- Includes metadata or permission flags required by the `mwdat` wearable SDK to access device features.

Notes / Next steps
- Ensure the manifest entries match the runtime permission requests in `MainActivity` and that `targetSdk` behavior is correct for newer Android versions.
- Verify `uses-feature` lines if the app relies on camera/microphone hardware.
