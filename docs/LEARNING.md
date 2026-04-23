# Learning notes — MetaLensAI

- **Branch**: `learning/meta-lens-ai` (created and pushed)
- **Build status**: `:app:assembleDebug` — succeeded locally

## Environment steps performed

- Moved repository to local disk to avoid Gradle file-watcher errors.
- Installed Temurin/OpenJDK 17 and mapped it to `J:` in the session to avoid `+` characters in the path:
  - Session `JAVA_HOME=J:\`
- Installed Android command-line tools to `%LOCALAPPDATA%\Android\Sdk\cmdline-tools\latest`.
- Used `sdkmanager` to install `platform-tools`, `platforms;android-35`, and `build-tools;35.0.1`.
- Added `sdk.dir=C:/Users/visiteur/AppData/Local/Android/Sdk` to `android/local.properties`.
- Added a GitHub packages token to `android/local.properties` as `github_token` (do NOT commit this file).

## Build & run

- Build command used:

```
& "$env:JAVA_HOME\bin\java.exe" -cp ".\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain :app:assembleDebug --stacktrace --info
```

## Files to annotate next

- [android/app/build.gradle.kts](android/app/build.gradle.kts)
- [android/app/src/main/java/com/metalens/app/MainActivity.kt](android/app/src/main/java/com/metalens/app/MainActivity.kt)
- [android/app/src/main/java/com/metalens/app/wearables/WearablesViewModel.kt](android/app/src/main/java/com/metalens/app/wearables/WearablesViewModel.kt)
- [android/app/src/main/java/com/metalens/app/stream/StreamViewModel.kt](android/app/src/main/java/com/metalens/app/stream/StreamViewModel.kt)
- [android/app/src/main/java/com/metalens/app/conversation/OpenAIRealtimeClient.kt](android/app/src/main/java/com/metalens/app/conversation/OpenAIRealtimeClient.kt)

## Next actions

- Add inline annotations to the files above on branch `learning/meta-lens-ai`.
- Optionally run static checks and unit tests.

Ask me to start annotating files now or to run tests.
