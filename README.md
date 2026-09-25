# رفيق المسلم (Muslim Companion) — Android App

A complete, native Android application built with **Kotlin + Jetpack Compose + Material 3**,
providing accurate prayer times, a live Qibla compass, and background Azan notifications.

## Features
- **Prayer Times**: Auto-detects GPS location and fetches Fajr/Dhuhr/Asr/Maghrib/Isha from the
  [Aladhan API](https://aladhan.com/prayer-times-api), with a live countdown to the next prayer.
- **Qibla Compass**: Calculates the great-circle bearing to the Kaaba and renders a live compass
  dial driven by the device's rotation-vector sensor.
- **Background Azan**: `AlarmManager` schedules one exact alarm per prayer; a foreground
  `Service` plays the Azan sound and posts a notification even if the app is closed or the
  phone was rebooted (handled by `BootReceiver` + `WorkManager`).
- **Settings**: Toggle the Azan sound, manually refresh location, and see the
  "Developed with ❤️ by Karim" credit.

## Tech Stack
Kotlin · Jetpack Compose · Material 3 · Hilt (DI) · Retrofit + Gson · Coroutines/Flow ·
Navigation Compose · WorkManager · DataStore Preferences · Fused Location Provider ·
Accompanist Permissions.

## How to Open & Run
1. Open **Android Studio** (Hedgehog / 2023.1.1 or newer).
2. `File → Open` and select the `MuslimCompanionApp` folder (this project's root — the one
   containing `settings.gradle.kts`).
3. Let Gradle sync (it will download the dependencies listed in `app/build.gradle.kts`).
4. Run on a device or emulator with **API 26+** and Google Play Services (needed for
   `FusedLocationProviderClient`).
5. On first launch, grant the location permission (required for both prayer times and the
   Qibla direction) and, on Android 13+, the notification permission.

## Building the APK Without Android Studio (low-spec computers)
Android Studio officially needs **8 GB of RAM minimum**; on a lower-spec machine (e.g. 4 GB
RAM) it will be extremely slow or may not run at all. This project includes a ready-to-use
**GitHub Actions** workflow (`.github/workflows/build-apk.yml`) that builds the APK for free in
the cloud — no installation needed on your own computer:

1. Create a free account at [github.com](https://github.com) if you don't have one.
2. Create a new **public or private repository** (e.g. `muslim-companion-app`).
3. Upload the entire contents of this folder to that repository. The easiest way with no
   command line:
   - On the repo page, click **Add file → Upload files**.
   - Drag the whole extracted `MuslimCompanionApp` folder contents in (GitHub will preserve
     the folder structure) and commit.
4. Go to the **Actions** tab of your repository — a workflow run should start automatically
   (or click **Run workflow** if it doesn't).
5. Wait a few minutes for the build to finish (green checkmark).
6. Open the finished run, scroll to **Artifacts**, and download `MuslimCompanion-debug-apk`
   — it's a zip containing `app-debug.apk`.
7. Transfer that `.apk` file to your Android phone (via USB, Google Drive, WhatsApp to
   yourself, etc.), open it on the phone, and allow "Install unknown apps" when prompted.
8. The app installs and runs like any normal app — no computer needed after this point.

## Adding a Real Azan Audio File (optional)
The app ships without a bundled audio file because it can't be generated as code. By default
`AzanForegroundService` looks for a raw resource named `azan` and, if it doesn't exist, falls
back automatically to the device's default alarm sound — so notifications work immediately
with zero setup.

To use a real Azan recording instead:
1. Add your own `.mp3` file to `app/src/main/res/raw/azan.mp3` (create the `raw` folder if
   needed — it already exists in this project).
2. Rebuild the app. No code changes are required; it's picked up automatically.

## Calculation Method
The Aladhan API `method` parameter defaults to `5` (Egyptian General Authority of Survey).
You can change this in `PreferencesManager` / expose it as a settings dropdown using the
`calculationMethod` flow already wired into `SettingsViewModel`. Common method IDs:
- `2` — ISNA (North America)
- `3` — Muslim World League
- `4` — Umm al-Qura, Makkah
- `5` — Egyptian General Authority of Survey

## Project Structure
```
app/src/main/java/com/karim/muslimcompanion/
├── alarm/          # AlarmManager scheduling, foreground service, boot receiver
├── data/           # Retrofit API, repository, location, DataStore preferences
├── di/             # Hilt modules
├── model/          # Domain models (PrayerTime, QiblaState, ...)
├── navigation/      # NavGraph + bottom-nav routes
├── ui/
│   ├── components/ # Reusable composables (BottomNavBar, PrayerCard)
│   ├── screens/    # PrayerTimesScreen, QiblaScreen, SettingsScreen
│   └── theme/      # Material 3 dark theme (emerald/gold palette)
├── util/           # QiblaCalculator, DateTimeUtils
├── viewmodel/      # PrayerViewModel, QiblaViewModel, SettingsViewModel
└── worker/         # WorkManager sync job (re-schedules alarms after reboot)
```

## Permissions Used
`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `INTERNET`, `POST_NOTIFICATIONS`,
`FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `SCHEDULE_EXACT_ALARM`,
`USE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`.

---
Developed with ❤️ by Karim
