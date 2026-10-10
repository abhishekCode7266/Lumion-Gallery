# Lumina Gallery — AI Photo, Video & Movie Scene Clarity Studio

A modern, responsive, and secure Android Gallery and Media Studio application built with Jetpack Compose, Material 3, Room SQLite persistence, and advanced AI image/video processing algorithms.

[![Android CI Build](https://github.com/abhishekCode7266/Lumion-Gallery/actions/workflows/android.yml/badge.svg)](https://github.com/abhishekCode7266/Lumion-Gallery/actions/workflows/android.yml)
[![Live Web App](https://img.shields.io/badge/Live%20Web%20App-GitHub%20Pages-blue?logo=google-chrome)](https://abhishekcode7266.github.io/Lumion-Gallery/)
[![Direct Download APK](https://img.shields.io/badge/Download-Debug%20APK-brightgreen?logo=android)](https://nightly.link/abhishekCode7266/Lumion-Gallery/workflows/android/main/Lumina-Gallery-debug-apk.zip)
[![Latest Release](https://img.shields.io/github/v/release/abhishekCode7266/Lumion-Gallery?label=Release&logo=github)](https://github.com/abhishekCode7266/Lumion-Gallery/releases/latest)

---

## 🚀 Live App & APK Download URLs

- 🌐 **Live Interactive Web App:** [https://abhishekcode7266.github.io/Lumion-Gallery/](https://abhishekcode7266.github.io/Lumion-Gallery/)
- 📱 **Direct APK Download (1-Click, No Login):** [Download Lumina Gallery APK](https://nightly.link/abhishekCode7266/Lumion-Gallery/workflows/android/main/Lumina-Gallery-debug-apk.zip)
- 🏷️ **GitHub Release (Direct .apk):** [Download latest app-debug.apk](https://github.com/abhishekCode7266/Lumion-Gallery/releases/latest/download/app-debug.apk)
- 📦 **GitHub Actions CI/CD Artifacts:** [View All Workflow Runs](https://github.com/abhishekCode7266/Lumion-Gallery/actions/workflows/android.yml)

---

## ✨ Features

1. **Smart Photo & Video Gallery**
   - Filter chips: All, Photos, Videos, Edited Media, Camera, Screenshots, Downloads.
   - Grid and List views with customizable column density (2, 3, or 4 columns).
   - Zero-permission Android Photo Picker (`PickMultipleVisualMedia`) and Camera capture.
   - Sorting by Date, Name, and Size.

2. **AI Blur Removal & Clarification**
   - High-pass detail recovery & unsharp masking.
   - Edge sharpening and sensor noise reduction.
   - Draggable before/after split comparison slider.

3. **Advanced Blur Studio**
   - Gaussian Blur, Motion Blur, Radial Zoom Blur.
   - White Haze diffusion, Cinematic Dark Haze, and Privacy Mosaic.

4. **Background Studio**
   - Pure White studio backdrop, Solid Black, Transparent PNG, and Portrait Depth-of-Field Blur.

5. **Movie Clarity & Scene Restoration Studio**
   - Automated scene boundary detection.
   - Frame-level Laplacian blur variance analysis.
   - Isolated scene restoration (deblur only the blurry scene, keep surrounding scenes untouched).
   - In-place scene replacement with synchronized audio and timing.
   - High-resolution video frame extraction.

6. **Natural Language AI Assistant**
   - Execute editing operations via natural language prompts (e.g., *"Remove blur from this photo"*, *"Make background white"*, *"Find blurry scenes in movie"*).

7. **Security & Organization**
   - Custom albums with safe deletion.
   - Private 4-digit PIN Vault.
   - Safe Trash recovery bin with permanent deletion confirmation.

---

## ⚙️ GitHub Actions Workflow (CI/CD)

The repository includes an automated workflow in `.github/workflows/android.yml`:

- **Triggers:** On every `push` to `main` or `master`, on `pull_request`, or manually via **Run workflow** (`workflow_dispatch`).
- **Steps:**
  1. Checks out the repository.
  2. Sets up JDK 17 with Gradle caching.
  3. Generates debug keystore if required.
  4. Runs unit and Robolectric tests (`gradle :app:testDebugUnitTest`).
  5. Assembles debug APK (`gradle :app:assembleDebug`).
  6. Uploads the generated APK as an artifact (`Lumina-Gallery-debug-apk`).

### How to Download the APK from GitHub:
1. Go to your repository on GitHub.
2. Click the **Actions** tab.
3. Select the latest run of **Build & Test Android APK**.
4. Scroll down to **Artifacts** and download **Lumina-Gallery-debug-apk**.
5. Install the APK directly on your Android phone or emulator.

---

## 🛠️ Local Build Instructions

```bash
# Run unit & Robolectric tests
gradle :app:testDebugUnitTest

# Assemble debug APK
gradle :app:assembleDebug
```
The output APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`
