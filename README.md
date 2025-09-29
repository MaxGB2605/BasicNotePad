# Basic Notepad - Android App

A simple, clean notepad application for Android.

## Features

- ✍️ **Simple Text Editor** - Multi-line text editing with a clean interface
- 💾 **Auto-Save** - Automatically saves your notes after 2 seconds of inactivity
- 📱 **Material Design** - Modern UI following Material Design guidelines
- 🔄 **Persistent Storage** - Notes are saved using SharedPreferences and persist across app restarts
- 🗑️ **Clear Function** - Clear all text with confirmation dialog
- 💿 **Manual Save** - Save button in toolbar for immediate saving

## Requirements

- Android Studio Arctic Fox or later
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)
- Kotlin 1.9.0

## How to Build

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Click "Run" or press Shift+F10
4. Select your device or emulator

## How to Use

1. Launch the app
2. Start typing in the text area
3. Your notes will auto-save after 2 seconds
4. Use the save icon in the toolbar to save immediately
5. Use the menu (three dots) to clear all text
6. Your notes persist even after closing the app

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/basicnotepad/
│   │   └── MainActivity.kt          # Main activity with note logic
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml    # Main UI layout
│   │   ├── menu/
│   │   │   └── menu_main.xml        # Toolbar menu
│   │   └── values/
│   │       ├── strings.xml          # String resources
│   │       ├── colors.xml           # Color definitions
│   │       └── themes.xml           # App theme
│   └── AndroidManifest.xml          # App manifest
└── build.gradle                      # App-level build config
```

## License

This is a simple educational project. Feel free to use and modify as needed.
