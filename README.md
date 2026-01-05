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
```

## Features

- **Clean Architecture:** Generates `data`, `domain`, `presentation`, `di`, `core` packages.
- **Modern Stack:** Kotlin 2.0, Compose, Hilt, Coroutines.
- **Configurable:** Optional Firebase integration.
- **Ready-to-Run:** Generates a buildable project with `gradlew` wrapper.

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

## Command Definition
- CLI entry: [index.js](file:///Users/farhan.pahlevi/Documents/trae_projects/android-project-generator/index.js#L22-L42)
- Options parsing and action: [index.js](file:///Users/farhan.pahlevi/Documents/trae_projects/android-project-generator/index.js#L43-L235)
