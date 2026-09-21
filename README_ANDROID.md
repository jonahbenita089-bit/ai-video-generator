# AI Video Generator Android WebView Wrapper

This repository now includes a minimal Android wrapper app that opens the Flask app in a WebView.

## What it does
- Uses a WebView to load the Flask app from the local backend URL.
- Defaults to `http://10.0.2.2:5000/` so the Android emulator can reach the local Flask server.
- Allows cleartext HTTP traffic for local development.

## Prerequisites
- Android Studio
- JDK 17
- Python environment for the backend app

## Build the backend Flask app
From the repository root:

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python app.py
```

Then keep the app running.

## Build the Android APK
Open the project in Android Studio and choose:
- Build > Generate Signed APK
or
- Build > Build Bundle(s) / APKs > Build APK

If you prefer the command line, from the repo root run:

```bash
./gradlew assembleDebug
```

The debug APK will be generated under:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Notes
- This project does not yet include a generated production APK because the APK must be built on a machine with the Android SDK and Gradle installed.
- For a real deployed app, replace `http://10.0.2.2:5000/` with your production URL such as a hosted Flask app or backend service.
