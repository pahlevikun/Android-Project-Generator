# Android Project Generator

A robust CLI tool to generate Android projects with Clean Architecture, Hilt, and Compose.

## Prerequisites
- Node.js 18+ recommended
- Android Studio (to open and build the generated project)
- JDK 17 (compatible with modern Android/Gradle)

## Installation

You can install this tool globally using npm:

```bash
npm install -g .
```

Or run it directly from the source:

```bash
node index.js <package-name> <app-name>
```

## Run Locally
- Clone the repository and install dependencies:
  ```bash
  git clone https://github.com/<your-username>/android-project-generator.git
  cd android-project-generator
  npm install
  ```
- Run via Node:
  ```bash
  node index.js com.example.myapp MyApp --firebase
  ```
- Or link as a global command for local development:
  ```bash
  npm link
  android-generator com.example.myapp MyApp --flavors dev,staging,prod --flavor-dimension env
  ```
- View help:
  ```bash
  android-generator -h
  ```

## Usage

```bash
android-generator <packageName> <appName> [options]
```

- packageName: Java/Kotlin package name, e.g. `com.example.myapp`
- appName: Project/app name, e.g. `MyApp`

### Options
- `--firebase` Include Firebase dependencies
- `--no-firebase` Skip Firebase dependencies
- `--flavors <list>` Comma-separated product flavors, default `staging,production`
- `--flavor-dimension <name>` Flavor dimension name, default `environment`
- `--disable-ribbon-flavors <list>` Flavors to disable EasyLauncher ribbon, default `production`
- `--disable-ribbon-variants <list>` Variants to disable EasyLauncher ribbon, default `release`
  
Notes:
- If `--firebase`/`--no-firebase` isn’t provided, the tool may prompt interactively.
- Flavor names are free-form; they will be added under the specified dimension.

### Examples
```bash
# Basic
android-generator com.example.myapp MyApp

# With Firebase
android-generator com.example.myapp MyApp --firebase

# Custom flavors
android-generator com.example.myapp MyApp --flavors dev,staging,prod --flavor-dimension env

# Disable ribbons for productionRelease only (default behavior)
android-generator com.example.myapp MyApp --firebase --disable-ribbon-flavors production --disable-ribbon-variants release

# Disable ribbons for multiple flavors/variants
android-generator com.example.myapp MyApp --disable-ribbon-flavors staging,production --disable-ribbon-variants debug,release
```

## Features

- **Clean Architecture:** Generates `data`, `domain`, `presentation`, `di`, `core` packages.
- **Modern Stack:** Kotlin 2.0, Compose, Hilt, Coroutines.
- **Configurable:** Optional Firebase integration.
- **Ready-to-Run:** Generates a buildable project with `gradlew` wrapper.
- **BuildSrc & AppProperties:** Generates buildSrc with `AppProperties`, `GitExtension`, `SingletonHolder`, and `ArtifactNameManipulator` to centralize configuration and artifact tasks.
- **Firebase App Distribution:** When `--firebase` is used, applies Crashlytics and App Distribution plugins, includes release notes file, and wires basic upload tasks.

## Generated Structure

```
MyApp/
├── app/
│   ├── src/main/java/com/example/myapp/
│   │   ├── data/
│   │   ├── domain/
│   │   ├── presentation/
│   │   ├── di/
│   │   ├── core/
│   │   ├── MainActivity.kt
│   │   └── MainApplication.kt
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
    └── libs.versions.toml
```

## After Generation
```bash
cd MyApp
./gradlew assembleDebug
```
If you enabled Firebase, remember to add your `google-services.json` into `app/`.

### Firebase release notes
- The generator includes `app_distribution_notes.txt` in the project root.
- App Distribution uses this file for both `release` and `debug` uploads.
- Update the contents before distributing builds.

### Flavor-based google-services.json
- If flavors are present, the generator creates placeholder `google-services.json` in:
  - `app/src/production/google-services.json` (base package)
  - `app/src/<flavor>/google-services.json` (package with flavor suffix)
- Replace these placeholders with your real Firebase configuration files per flavor.

### BuildSrc overview
- `buildSrc/src/main/kotlin/<package>/plugin/properties/AppProperties.kt`
  - Reads values from `gradle.properties` (application ID, SDK versions, app name, keystore, Firebase credentials, flavor dimension).
- `buildSrc/src/main/kotlin/<package>/plugin/extension/GitExtension.kt`
  - Provides utilities like short commit retrieval.
- `buildSrc/src/main/kotlin/<package>/plugin/extension/ArtifactNameManipulator.kt`
  - Hook point for customizing artifact naming per variant.
- `buildSrc/src/main/kotlin/<package>/plugin/SingletonHolder.kt`
  - Utility to implement singleton-style accessors.

## Command Definition
- CLI entry: [index.js](file:///Users/farhan.pahlevi/Documents/AuraLockIn/tools/android-generator/index.js)
- Templates:
  - App template: [templates/app/build.gradle.kts](file:///Users/farhan.pahlevi/Documents/AuraLockIn/tools/android-generator/templates/app/build.gradle.kts)
  - Root gradle files: [templates/root](file:///Users/farhan.pahlevi/Documents/AuraLockIn/tools/android-generator/templates/root)
  - BuildSrc: [templates/buildSrc](file:///Users/farhan.pahlevi/Documents/AuraLockIn/tools/android-generator/templates/buildSrc)
