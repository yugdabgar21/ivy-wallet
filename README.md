# Ivy Wallet (Modernized Fork)

[![CI - Debug APK](https://github.com/yugdabgar21/ivy-wallet/actions/workflows/apk.yml/badge.svg?branch=feature/glass-nav-compact)](https://github.com/yugdabgar21/ivy-wallet/actions/workflows/apk.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Android](https://img.shields.io/badge/Android-9.0%2B%20(API%2028%2B)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.8-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)

> [!NOTE]
> **Independent Fork Notice**: This repository is an independent, personal fork of [Ivy-Apps/ivy-wallet](https://github.com/Ivy-Apps/ivy-wallet), which ceased official maintenance in November 2024. In accordance with the [GNU General Public License v3.0](LICENSE), this fork continues development independently to introduce modern Android UI paradigms, performance enhancements, and bug fixes. This fork is not officially affiliated with or endorsed by Ivy Apps Ltd.

---

## 🌟 What's New in this Fork

### 1. Floating Pill Navigation
- **Detached Floating Design**: Replaces the legacy edge-to-edge navigation bar with a compact, detached pill container (64.dp height, 312.dp maximum width, 32.dp corner radius) floating elegantly above app content.
- **Dynamic Text Measurement**: Employs `rememberTextMeasurer` to dynamically evaluate label fit against screen width, ensuring symmetric, balanced touch targets and collapsing to clean icon-only tabs when space is constrained.
- **Center Action Button & Gestures**: The centered Floating Action Button provides instant transaction creation with an expandable radial launcher on the Home screen (supporting directional gesture shortcuts: swipe up for expense, up-left for income, up-right for transfer).
- **Edge-to-Edge Inset Handling**: Seamlessly honors Android system navigation bars and gesture insets across various display aspect ratios.

### 2. Material Liquid Glass UI
- **Hardware-Accelerated Backdrop Blur**: Utilizes Android's native `RenderNode` and custom AGSL runtime shaders to deliver authentic real-time background blurring and specular edge refraction.
- **Tiered Compatibility Architecture**:
  - **Android 13+ (API 33+)**: Full real-time AGSL specular refraction shader chained with hardware `RenderEffect` blur.
  - **Android 12 to 12L (API 31–32)**: Hardware `RenderEffect` backdrop blur with specular glass outlines.
  - **Android 9 to 11 (API 28–30)**: Graceful, crash-free fallback using elegant semi-transparent translucent overlays and elevated drop shadows.
- **Glass Keypad & Sheets**: Liquid glass styling applied across the quick transaction creation amount panel and modal sheets.

### 3. Visual Effects Customization
- **User Settings Control**: Accessible under **Settings > Visual Effects**.
- **Master Switch**: Instantly toggle all glass and blur effects on or off.
- **Granular Controls**: Independently customize or disable effects for individual UI components (Navigation Bar, Dashboard Cards, Transaction Cards, Action Button, Bottom Sheets, Dialogs, and Settings Panels).
- **Blur Intensity Adjustment**: Fine-tune blur radius and intensity to match personal visual preference or optimize performance on lower-power devices.

### 4. Restored Expense & Income Donut Charts
- **Interactive Analytics**: Restored high-performance Donut Charts on the Reports screen displaying proportional spending and income breakdowns.
- **Multi-Currency Support**: Integrated currency exchange conversion (`ExchangeData`) ensuring accurate consolidation across accounts in different currencies.
- **Category Drill-Down**: Tapping any segment navigates directly to granular category statistics and transaction histories.

---

## 📱 App Overview

Ivy Wallet is a free and open-source personal finance and money management application for Android built with **100% Kotlin and Jetpack Compose**. It is designed to replace spreadsheets with a fluid, intuitive, and private expense tracking experience.

### Base Interface (Original Design System)

| | | | |
|:---:|:---:|:---:|:---:|
| ![1](https://user-images.githubusercontent.com/5564499/189540998-4d6cdcd3-ab4d-40f7-85d4-c82fe8a017d1.png) | ![2](https://user-images.githubusercontent.com/5564499/189541011-1ebbd8b6-50fe-432a-91e2-59206efe99ce.png) | ![3](https://user-images.githubusercontent.com/5564499/189541023-35e7f163-d639-4466-9a91-c56890d5a28e.png) | ![4](https://user-images.githubusercontent.com/5564499/189541027-d352314c-fd5c-43eb-82ad-4aba14c7b0fa.png) |
| ![5](https://user-images.githubusercontent.com/5564499/189541030-1a0d7948-33af-420b-b126-936d0211c93f.png) | ![6](https://user-images.githubusercontent.com/5564499/189541035-621c4511-5ec7-4d3f-b08e-925d8da95472.png) | ![7](https://user-images.githubusercontent.com/5564499/189541127-7adf5bfa-0652-461c-80f1-076b7179eb6c.png) | ![8](https://user-images.githubusercontent.com/5564499/189541040-7cab633e-be4c-40b2-a2c6-890a15edf805.png) |

*(Screenshots above represent the original upstream Ivy Wallet interface upon which this modernized fork is built).*

---

## 📲 Download & Installation

### Automated Builds & Releases
- Pre-compiled Debug APKs are automatically generated by GitHub Actions on every update to the feature branch.
- You can download the latest build from the [GitHub Actions APK Workflow](https://github.com/yugdabgar21/ivy-wallet/actions/workflows/apk.yml) or from the repository [Releases page](https://github.com/yugdabgar21/ivy-wallet/releases).
- **Package Name**: `com.ivy.wallet.debug`
- **Application Label**: `Ivy Wallet Debug`
- **Side-by-Side Installation**: The debug build uses a distinct application ID suffix (`.debug`), meaning it can be installed alongside any official release without conflicting with existing data.

> [!TIP]
> Downloaded APKs are unsigned debug builds created for testing and personal use. When installing on your Android device, you may need to allow installation from unknown sources in your browser or file manager settings.

---

## ⚙️ System Requirements & Compatibility

| Specification | Version / Requirement | Notes |
| :--- | :--- | :--- |
| **Minimum Android** | Android 9.0 Pie (API level 28) | Clean translucent styling fallback without hardware blur |
| **Backdrop Blur** | Android 12+ (API level 31+) | Hardware-accelerated `RenderEffect` blur |
| **AGSL Refraction** | Android 13+ (API level 33+) | Real-time specular light refraction runtime shader |
| **Target SDK** | Android 16 (API level 36) | Latest Android platform capabilities |
| **Compile SDK** | SDK 36 (App module) / SDK 35 (Libraries) | Maximizes compatibility across Gradle and testing toolchains |
| **Java Toolchain** | Java 17 | Required for building with Gradle |

---

## 🛠️ Building from Source

To compile and run Ivy Wallet locally:

### 1. Prerequisites
- **JDK 17** (e.g. [Eclipse Temurin 17](https://adoptium.net/temurin/releases/?version=17))
- **Android Studio** (Ladybug | 2024.2.1 or newer recommended)

### 2. Clone the Repository
```bash
git clone https://github.com/yugdabgar21/ivy-wallet.git
cd ivy-wallet
```

### 3. Build the Debug APK
The repository includes a pre-configured debug keystore (`debug.jks`), allowing immediate local compilation without manual keystore configuration:

- **Linux / macOS**:
  ```bash
  ./gradlew assembleDebug
  ```
- **Windows (PowerShell / Command Prompt)**:
  ```powershell
  .\gradlew assembleDebug
  ```

The compiled APK will be generated at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

### 4. Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

---

## 🏛️ Architecture & Tech Stack

Ivy Wallet is built using modern Android architecture best practices with modularized Clean Architecture, Unidirectional Data Flow (MVI), and Jetpack Compose.

Detailed architecture diagrams are available in the repository:
- [Architecture Overview](docs/assets/architecture.svg)
- [Application Layers](docs/assets/app-layers.svg)
- [Modularization Graph](docs/assets/modularization.svg)
- [Data Mapping Pipeline](docs/assets/data-mapping.svg)

### Technology Stack
- **UI & Design**: [Jetpack Compose 1.6.8](https://developer.android.com/jetpack/compose), [Material3 1.2.1](https://m3.material.io/)
- **Language & Runtime**: [Kotlin 2.0.20](https://kotlinlang.org/), [Kotlin Coroutines 1.8.1](https://kotlinlang.org/docs/coroutines-overview.html), [Kotlin Flow](https://kotlinlang.org/docs/flow.html)
- **Dependency Injection**: [Dagger Hilt 2.52](https://dagger.dev/hilt/)
- **Functional Programming**: [ArrowKt 1.2.4](https://arrow-kt.io/)
- **Local Persistence**: [Room DB 2.6.1](https://developer.android.com/training/data-storage/room), [AndroidX DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- **Networking & Serialization**: [Ktor Client 2.3.12](https://ktor.io/), [Kotlinx Serialization 1.7.1](https://github.com/Kotlin/kotlinx.serialization)
- **Testing & Verification**: [JUnit 4](https://github.com/junit-team/junit4), [Kotest 5.9.1](https://kotest.io/), [Paparazzi 1.3.3](https://github.com/cashapp/paparazzi)
- **Static Analysis**: [Detekt](https://github.com/detekt/detekt), [Ktlint](https://github.com/pinterest/ktlint), [Slack Compose Lints](https://slackhq.github.io/compose-lints/)

---

## ⚠️ Known Limitations

- **Shader Availability**: AGSL runtime shaders require Android 13+ (API 33+), and `RenderEffect` hardware blur requires Android 12+ (API 31+). Devices running Android 9 through Android 11 utilize a semi-transparent translucent fallback without hardware blur.
- **Cloud Synchronization**: Upstream online sync and proprietary cloud services from the original Ivy Wallet project are not hosted or maintained by this personal fork. All data remains strictly local and private on your device.
- **Headless Unit Tests**: In virtual headless environments (such as Paparazzi JVM screenshot tests), hardware acceleration is unavailable, and UI components automatically fall back to software-safe rendering.

---

## ⚖️ Credits, Attribution & Licensing

### Original Ivy Wallet
- **Original Authors**: Iliyan Germanov, Stefan Ilijev (co-founder and designer of the Ivy design system), and the Ivy Apps open-source community.
- **Upstream Repository**: [Ivy-Apps/ivy-wallet](https://github.com/Ivy-Apps/ivy-wallet) (Active 2021–2024).
- **License**: Released under the **[GNU General Public License v3.0 (GPL-3.0)](LICENSE)**.

### Third-Party Shader Attribution
- **Liquid Glass AGSL Shader**: The rounded-rectangle refraction shader source is vendored from [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) (commit `65ab177`).
- **Shader License**: Licensed under the **[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)**, Copyright 2025 Kyant.
- Full notice and license details are preserved at `temp/legacy-code/src/main/java/com/ivy/legacy/ui/liquidglass/NOTICE.md`.

---

### License Summary
This software is licensed under the [GNU General Public License v3.0](LICENSE). You are free to inspect, modify, and distribute it in accordance with the terms of the GPL-3.0 license.
