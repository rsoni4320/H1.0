# Heena AI (हीना AI) — Native Android Smart Voice Assistant

> **Complete Native Android Voice Assistant with Anime Avatar, Hindi Voice, Background Wake Word, AI Chat, and Phone Automation.**

---

## 1. Application Overview

**Heena AI** is a fully functional, production-oriented native Android mobile application built with **Kotlin** and **Jetpack Compose (Material 3)**. It integrates:
- **Bilingual Voice Engine**: Native voice recognition and Text-To-Speech supporting **Hindi**, **Hinglish**, and **English**.
- **Interactive Anime Avatar (`HeenaAvatarView`)**: Animated companion with expressions (*Idle, Listening, Thinking, Speaking, Happy, Error*), floating breathing motions, and sound-reactive aura pulses.
- **Background Wake Word Service**: Foreground listening service detecting "*Hello Heena*", "*नमस्ते हीना*", and "*हीना*".
- **Phone Automation & App Launching**: Intent router for launching YouTube (with search queries), Camera, Chrome, Google Maps, WhatsApp, and Dialer.
- **Safety Safeguard**: Sensitive actions (e.g. phone calls) require explicit modal confirmation.
- **Conversational Intelligence**: Google Gemini API integration with command protocol `[[ACTION: TYPE | PARAM]]` for automated tool execution.
- **Smart Routines & Memory**: Room Database persistence for custom routines, chat history, and user preferences with zero voice retention.

---

## 2. Project Architecture & Codebase

```
/app/src/main/
├── AndroidManifest.xml                  # Manifest with permissions, queries, and services
├── java/com/example/
│   ├── HeenaApplication.kt             # Application class, Room DB & Notification Channels
│   ├── MainActivity.kt                 # Edge-to-edge Scaffold, navigation stack & safeguards
│   ├── data/
│   │   ├── api/
│   │   │   └── GeminiClient.kt         # Direct REST API client, system prompts & actions
│   │   └── local/
│   │       ├── ChatMessageEntity.kt    # Room entities for Chat and Conversations
│   │       ├── UserMemoryEntity.kt     # Room entities for Memory and Routines
│   │       ├── Daos.kt                 # ChatDao, MemoryDao, and RoutineDao
│   │       ├── HeenaDatabase.kt        # Pre-populated Room DB with routines
│   │       └── PreferencesManager.kt   # Central reactive settings flow
│   ├── voice/
│   │   ├── SpeechManager.kt            # Android SpeechRecognizer wrapper with dB audio monitor
│   │   └── TtsManager.kt               # Android TextToSpeech wrapper with utterance sync
│   ├── automation/
│   │   ├── AppLauncherHelper.kt        # Intent launchers (YouTube, Maps, Chrome, Camera, Dialer)
│   │   └── CommandRouter.kt            # Natural language Hindi/English intent classifier
│   ├── service/
│   │   ├── HeenaWakeWordService.kt     # Foreground Service for background wake-word detection
│   │   └── HeenaAccessibilityService.kt# Accessibility Service for authorized automation
│   ├── ui/
│   │   ├── components/
│   │   │   ├── HeenaAvatarView.kt      # State-driven anime companion view with animations
│   │   │   ├── MicButton.kt            # Pulsing microphone FAB with ripple waves
│   │   │   └── BottomNavBar.kt         # Material 3 navigation bar
│   │   ├── theme/
│   │   │   ├── Color.kt                # Palettes for Purple Cyber, Pastel, Pink, Blue, Dark
│   │   │   └── Theme.kt                # HeenaAITheme multi-style theme switcher
│   │   └── screens/
│   │       ├── SplashScreen.kt         # Animated splash screen
│   │       ├── OnboardingScreen.kt     # Multi-step feature walkthrough & language selection
│   │       ├── HomeScreen.kt           # Main dashboard with avatar, mic FAB & shortcuts
│   │       ├── ChatScreen.kt           # AI conversation with TTS, copy & share actions
│   │       ├── AutomationScreen.kt     # Routine manager, execution, & custom routine builder
│   │       ├── PermissionsScreen.kt    # Permissions status, explanations, & Settings intents
│   │       ├── AiConfigScreen.kt       # Masked API key input, model picker, & live connection test
│   │       ├── ThemeStudioScreen.kt    # Theme selector, voice pitch/speed sliders, & name editor
│   │       └── SettingsScreen.kt       # Privacy controls, memory editor, & diagnostics
└── res/
    ├── drawable/                       # High-res anime avatar assets & adaptive icon layers
    ├── mipmap-*/                       # Generated PNGs and round icons across all densities
    └── values/strings.xml              # Localized strings and descriptions
```

---

## 3. Feature-by-Feature Implementation Status Report

| Specification Area | Status | Implementation Details |
|---|---|---|
| **App Identity & Theming** | **Implemented** | Centralized in `PreferencesManager` and `strings.xml`. Themes include Purple Cyber, Pastel, Sakura Pink, Electric Blue, AMOLED Dark, and Light. |
| **Anime Avatar Companion** | **Implemented** | Interactive avatar with 6 distinct states (`IDLE`, `LISTENING`, `THINKING`, `SPEAKING`, `HAPPY`, `ERROR`), floating animations, sound waves, and rotating holographic rings. |
| **Hindi & English Speech Input** | **Implemented** | Uses Android `SpeechRecognizer` configured for `hi-IN` and `en-IN`. Live volume (dB) feeds into avatar audio scale. |
| **Female Voice Output (TTS)** | **Implemented** | Android `TextToSpeech` with female voice pitch (default `1.05`), speed tuning (`0.7x - 1.5x`), and utterance completion tracking. |
| **"Hello Heena" Wake Word** | **Implemented** | `HeenaWakeWordService` foreground service with persistent notification and trigger broadcast. |
| **Android App Launching** | **Implemented** | Safe explicit intents for YouTube, Chrome, Maps, Camera, Gallery, WhatsApp, Dialer, and Clock Timers. |
| **Voice Query Routing** | **Implemented** | Decomposes Hindi queries (e.g. *"YouTube par cricket highlights search karo"*, *"5 minute ka timer lagao"*, *"Delhi ka rasta dikhao"*). |
| **Sensitive Action Safeguard** | **Implemented** | Phone calls prompt a mandatory confirmation dialog before launching the dialer. |
| **AI Chat & Gemini Integration** | **Implemented** | Direct HTTPS REST calls using `gemini-2.5-flash` or `gemini-3.5-flash`, parsing structured `[[ACTION: TYPE \| PARAM]]` tags. |
| **Local Persistence & Memory** | **Implemented** | Room Database with `ChatMessageEntity`, `ConversationEntity`, `UserMemoryEntity`, and `RoutineEntity`. |
| **Smart Routines Builder** | **Implemented** | Pre-populated routines (*Good Morning / सुप्रभात*, *YouTube Music*, *Focus Timer*) and UI creator for custom multi-step routines. |
| **Permissions & Access Center** | **Implemented** | Displays real-time status of Microphone, Notifications, and Accessibility Service with intent buttons to system settings. |
| **Privacy & Security** | **Implemented** | Zero audio retention by default, user-controlled memory deletion, and masked API key handling. |
| **Testing & CI Verification** | **Verified** | Robolectric unit tests pass with `100%` success for CUJs, Hindi command routing, safeguards, and preferences. |

---

## 4. Setup & API Configuration Instructions

### 1. Gemini API Key Configuration
You can supply your Gemini API key through either method:
- **Method A (Secrets Panel)**: In Google AI Studio, enter `GEMINI_API_KEY` into the Secrets panel. It will automatically inject via `BuildConfig.GEMINI_API_KEY`.
- **Method B (In-App Configuration Center)**: In the app, navigate to **Settings > AI Configuration Center** (or tap the sparkle icon in the top bar). Paste your API key, choose your model (`gemini-2.5-flash` or `gemini-3.5-flash`), and click **Test Connection** to verify live connectivity.

### 2. Permissions Setup
When launching the app for the first time:
1. **Microphone**: Tap **Grant Permission** in the Permissions Center or on the Home screen to enable voice input and speech recognition.
2. **Notifications**: Enable to allow the background wake-word service notification.
3. **Accessibility Service (Optional)**: If you wish to use multi-step phone UI automation, follow the in-app link to Android Accessibility Settings and toggle on **Heena AI**.

---

## 5. Build and Installation

### Building with Gradle
To compile and assemble the debug APK:
```bash
gradle assembleDebug
```
The resulting APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

### Running Tests
To run local JVM Robolectric unit tests:
```bash
gradle :app:testDebugUnitTest
```

---

## 6. Known Platform Limitations & Android Compatibility
- **Background Wake Word**: Android 14+ restricts background microphone access when the device is locked or the app is force-stopped by aggressive OEM battery managers (e.g. MIUI, ColorOS). The foreground service notification provides the highest degree of reliability supported by Android.
- **Accessibility Service**: Android requires manual user activation via System Settings for security; apps cannot grant accessibility to themselves.
- **Speech Engine**: Speech recognition accuracy relies on the device's installed Google Speech Services engine.
