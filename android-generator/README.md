# Android Project Generator

A robust CLI tool to generate Android projects with Clean Architecture, Hilt, and Compose.

## Installation

You can install this tool globally using npm:

```bash
npm install -g .
```

Or run it directly from the source:

```bash
node index.js <package-name> <app-name>
```

## Usage

```bash
android-generator com.example.myapp MyApp
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
