# Annotation: android/app/build.gradle.kts

Purpose
- App-level Gradle configuration for the Android app module.

Key points
- `compileSdk` and `minSdk` are set for target devices (Compose + Wearables).
- Reads sensitive values at build time using `getBuildProperty(...)` (e.g. `OPENAI_API_KEY`, `OPENAI_MODEL`, `github_token`) — these should be set in `local.properties` and never committed.
- Uses the version catalog (`libs.versions.toml`) for AGP/Kotlin/Compose versions.
- Declares dependencies including `com.meta.wearable:mwdat-*` packages from GitHub Packages; access requires a `github_token` configured in `local.properties` or `GITHUB_TOKEN` env var.

Notes / Next steps
- Do not commit `local.properties` — add a `.gitignore` rule if needed.
- For reproducible builds, document expected `local.properties` keys and minimal SDK/build-tools versions.
