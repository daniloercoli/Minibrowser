# MiniBrowser

A small Android WebView browser that automatically disables JavaScript after a configurable interval following page load.

## Features

- **WebView Browser** with full web navigation support
- **URL Sharing** from other apps (handles `text/plain` share intents)
- **Automatic JavaScript Disabling** after a user-configurable delay
- **Settings Screen** to adjust the disable interval (50–1000 ms)
- **Preferences Persistence** via Jetpack DataStore

## Technical Requirements

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34 (compile target)
- Android 7.0+ (API 24) for runtime
- Kotlin 1.9.0+
- Gradle 8.2.0+

## Project Structure

- `MainActivity.kt` – Main activity and Navigation Compose setup
- `BrowserScreen.kt` – Composable hosting the WebView mini-browser
- `SettingsScreen.kt` – Composable for configuring the JavaScript-disable interval
- `DelayDataStore.kt` – DataStore-based persistence implementation
- `AndroidManifest.xml` – Manifest configuration including the share-intent filter
- Gradle build files

## Import & Run

1. Clone or download the repository
2. Open Android Studio Hedgehog (or newer)
3. Select “Open an Existing Project”
4. Navigate to and open the project folder
5. Wait for Gradle sync to complete
6. Connect an Android device or start an emulator
7. Click “Run” (▶️) to build and install the app

## Usage

- On launch, the app opens a default browser page (Google as the home page)
- If started via “Share” from another app with a URL, that link loads immediately
- Enter a new URL in the top text field
- Tap the “Go” (→) button or press Enter to navigate
- Tap the “Refresh” (↻) button to reload the current page
- Tap the “Settings” (⚙️) button to open the settings screen
- In Settings, use the slider to choose how many milliseconds before JavaScript is disabled
- Tap “Save” to persist your choice and return to the browser

## Technical Notes

- Preferences are stored using Jetpack DataStore
- The theme follows the system light/dark setting automatically
- Supports dynamic theming on Android 12+  
