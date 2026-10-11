# CallBack

Phase 1 Android skeleton for **CallBack** — fully on-device missed-call auto-text (no backend).

- **Stack:** Kotlin, Jetpack Compose (Material 3), Room, WorkManager
- **Gradle:** Kotlin DSL + version catalog (`gradle/libs.versions.toml`)
- **JDK:** 17 (bytecode target)
- **SDK:** minSdk 26, targetSdk 34, `applicationId` `com.callback.app`

## Build

1. Install [Android SDK](https://developer.android.com/studio) and set `ANDROID_HOME` (or create `local.properties` with `sdk.dir=...`).
2. Use JDK 17+.
3. From the repo root:

```bash
./gradlew assembleDebug lint testDebugUnitTest
```

Unsigned debug and release builds work without signing secrets. Release signing is applied only when **all** of these environment variables are set:

| Variable | Purpose |
|----------|---------|
| `CALLBACK_KEYSTORE_PATH` | Path to the release keystore file |
| `CALLBACK_KEYSTORE_PASSWORD` | Keystore password |
| `CALLBACK_KEY_ALIAS` | Key alias |
| `CALLBACK_KEY_PASSWORD` | Key password |

## Project layout

| Package | Role |
|---------|------|
| `data` | Room (`Lead`, `TextedNumber`), DAOs, `AppDatabase` |
| `detect` | `PhoneStateReceiver`, `MissedCallChecker`, 24h repeat policy |
| `sms` | `AutoTextWorker`, `SmsReceiver`, `ReplyParser` |
| `ui` | `MainActivity`, Compose theme, Inbox (New/Done), onboarding placeholder |

## Permissions

Declared in the manifest: `READ_CALL_LOG`, `READ_PHONE_STATE`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CONTACTS`, `POST_NOTIFICATIONS`.
