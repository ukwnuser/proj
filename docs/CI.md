# Continuous integration

This repository uses two GitHub Actions workflows under `.github/workflows/`.

## CI (`ci.yml`)

**Triggers:** every `pull_request`, and pushes to `main`.

**What it does:**

1. Checks out the repo on `ubuntu-latest`.
2. Installs Temurin JDK 17 and enables Gradle caching (`gradle/actions/setup-gradle@v4`).
3. Runs `./gradlew assembleDebug lint testDebugUnitTest`.

**On failure**, lint HTML/XML reports and unit-test result files are uploaded as workflow artifacts (`lint-reports`, `test-results`) so you can download them from the Actions run.

Concurrent runs for the same branch/ref cancel older in-progress jobs (`cancel-in-progress: true`).

## Release (`release.yml`)

**Trigger:** manual only (`workflow_dispatch`) from the Actions tab.

**What it does:**

1. Fails immediately if any required signing secret is missing (see below).
2. Decodes the base64 keystore into `$RUNNER_TEMP/release.jks` and sets `CALLBACK_KEYSTORE_PATH` for Gradle (same env vars as [local release signing](../README.md#build)).
3. Runs `./gradlew assembleRelease`.
4. Verifies the APK with `apksigner verify --print-certs` (Android SDK build-tools on the runner).
5. Writes a `sha256sum` sidecar file next to the APK.
6. Uploads the APK and `.sha256` file as a single artifact named **`callback-release-apk`**.
7. Deletes the temporary keystore file in an `always()` cleanup step.

## Repository secrets (release signing)

Create these under **Settings → Secrets and variables → Actions → New repository secret**:

| Secret | Description |
|--------|-------------|
| `CALLBACK_KEYSTORE_BASE64` | Base64-encoded release `.jks` file (no newlines) |
| `CALLBACK_KEYSTORE_PASSWORD` | Keystore password |
| `CALLBACK_KEY_ALIAS` | Key alias inside the keystore |
| `CALLBACK_KEY_PASSWORD` | Key password |

Encode your keystore on Linux:

```bash
base64 -w0 release.jks
```

Copy the single line of output into `CALLBACK_KEYSTORE_BASE64`. On macOS, use `base64 release.jks | tr -d '\n'` instead of `-w0`.

The CI workflow does **not** need these secrets; only the release workflow does.
