# Build instructions for the native Android app

This project is a native Android app that does not require a Flask server or custom backend.

## Prerequisites
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 34

## Open project
Open the `android/` folder in Android Studio.

## Build debug APK
From Android Studio:
- Build > Build Bundle(s)/APK(s) > Build APK(s)

Or from terminal:

```bash
cd android
./gradlew assembleDebug
```

APK output:
```text
android/app/build/outputs/apk/debug/app-debug.apk
```

## Important notes
- The app supports direct AI provider configuration through user-supplied API keys.
- If no API key is configured, it falls back to a native local rendering path.
- No hardcoded developer secrets are embedded in the APK.
- This app is designed to run without a Flask or custom backend.
