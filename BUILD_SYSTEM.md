# Verum Omnis Build System Documentation

**Version**: 5.2.7  
**Status**: Production Ready  
**Updated**: January 21, 2026

This document explains how to build Verum Omnis on any platform using the integrated build system.

---

## 📋 Quick Reference

### Build Commands

**Linux/macOS/WSL (Bash)**:
```bash
chmod +x build.sh
./build.sh [android|web|capacitor|both|clean|verify]
```

**Windows (PowerShell)**:
```powershell
.\build.ps1 -Command [android|web|capacitor|both|clean|verify]
```

**Git (Any Platform)**:
```bash
git clone https://github.com/liamtest26/Verumomnis.git
cd Verumomnis
bash build.sh both        # Linux/macOS/WSL
powershell -File build.ps1 -Command both  # Windows
```

---

## 🎯 Common Tasks

### I want to build everything
```bash
# Linux/macOS
./build.sh both

# Windows
.\build.ps1 -Command both
```

### I want to build just the Android APK
```bash
# Linux/macOS
./build.sh android

# Windows
.\build.ps1 -Command android
```

### I want to build just the web app
```bash
# Linux/macOS
./build.sh web

# Windows
.\build.ps1 -Command web
```

### I want to verify my build outputs
```bash
# Linux/macOS
./build.sh verify

# Windows
.\build.ps1 -Command verify
```

### I want to clean everything
```bash
# Linux/macOS
./build.sh clean

# Windows
.\build.ps1 -Command clean
```

---

## 🔧 Build Scripts

### Bash Build Script (`build.sh`)

The bash script (`build.sh`) is for **Linux, macOS, and Windows (WSL/Git Bash)**.

**Features**:
- Checks all system requirements
- Provides colored output with timestamps
- Generates build logs (`build_YYYYMMDD_HHMMSS.log`)
- Verifies APK hashes
- Shows bundle sizes
- Supports both development and release builds

**Usage**:
```bash
chmod +x build.sh
./build.sh [command]

# Commands:
# android    - Build Android APK
# web        - Build web application
# capacitor  - Build Capacitor hybrid
# both       - Build everything (default)
# clean      - Remove build artifacts
# verify     - Verify build outputs
# requirements - Check system setup
```

### PowerShell Build Script (`build.ps1`)

The PowerShell script (`build.ps1`) is for **Windows (native, not WSL)**.

**Features**:
- Windows-native command execution
- Handles spaces in paths
- Generates build logs
- Colored console output
- PowerShell module compliance

**Usage**:
```powershell
.\build.ps1 -Command [command]

# Parameters:
# -Command [android|web|capacitor|both|clean|verify|requirements|help]
# -Verbose           Show detailed output
# -Help              Show help message

# Examples:
.\build.ps1 -Command android
.\build.ps1 -Command both -Verbose
.\build.ps1 -Command verify
```

---

## 📦 Build Outputs

### Android APK

**Location**: `app/build/outputs/apk/release/app-release.apk`

**Properties**:
- **Hash**: `56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466` (production)
- **Minimum SDK**: 24 (Android 7.0+)
- **Target SDK**: 34 (Android 14)
- **Size**: ~45-55 MB (typical)

### Web Build

**Location**: `web/dist/`

**Contents**:
- `index.html` - Main entry point
- `assets/` - JavaScript bundles, CSS, images
- `manifest.json` - PWA manifest

**Size**: ~2-3 MB (gzipped)

---

## 🚀 Getting Started

### Step 1: Clone Repository

```bash
git clone https://github.com/liamtest26/Verumomnis.git
cd Verumomnis
```

### Step 2: Check Requirements

```bash
# Linux/macOS
./build.sh requirements

# Windows PowerShell
.\build.ps1 -Command requirements
```

### Step 3: Install Missing Dependencies

Refer to [PRODUCTION_BUILD_GUIDE.md](PRODUCTION_BUILD_GUIDE.md) for:
- JDK 17 installation
- Gradle setup
- Node.js installation
- Android SDK configuration

### Step 4: Build

```bash
# Build everything
./build.sh both           # Linux/macOS
.\build.ps1 -Command both # Windows
```

### Step 5: Verify

```bash
# Verify build outputs
./build.sh verify           # Linux/macOS
.\build.ps1 -Command verify # Windows
```

---

## 📝 Build Logs

Build logs are automatically saved to `build_YYYYMMDD_HHMMSS.log`

**Example**: `build_20260121_160300.log`

**Contents**:
- Build command output
- Error messages
- Performance metrics
- Verification results

**View logs**:
```bash
# Linux/macOS
tail -f build_*.log

# Windows PowerShell
Get-Content build_*.log -Tail 20 -Wait
```

---

## 🔐 Security & Signing

### Production APK Signing

For production releases, the APK must be signed:

```bash
# 1. Generate signing key (one-time)
keytool -genkey -v -keystore release.jks \
  -keyalg RSA -keysize 2048 -validity 36500 \
  -alias verumomnis

# 2. Set environment variables
export KEYSTORE_PASSWORD="your-password"
export KEY_PASSWORD="your-password"

# 3. Build script will automatically sign
./build.sh android
```

---

## 🌐 Web Deployment

### Deploy to Web Server

```bash
# Build
./build.sh web

# Copy to web server
scp -r web/dist user@server:/var/www/html

# Or with Docker
docker build -t verumomnis:5.2.7 .
docker push registry.example.com/verumomnis:5.2.7
```

---

## 📱 Capacitor Hybrid Build

### Build for Both Web and Mobile

```bash
# Build everything with Capacitor integration
./build.sh capacitor

# This:
# 1. Builds web assets
# 2. Syncs to native platforms
# 3. Prepares for Android/iOS build

# Then open in native IDE:
npm run cap:build:android  # Opens Android Studio
npm run cap:build:ios      # Opens Xcode
```

---

## 🐛 Troubleshooting

### Build fails with "Java not found"

```bash
# Install JDK 17
# Ubuntu
sudo apt-get install openjdk-17-jdk

# macOS
brew install openjdk@17

# Windows
choco install openjdk17

# Set JAVA_HOME
export JAVA_HOME=/path/to/jdk-17
```

### Build fails with "Gradle not found"

```bash
# Make gradlew executable
chmod +x gradlew

# Or install Gradle
brew install gradle  # macOS
sudo apt-get install gradle  # Ubuntu
choco install gradle  # Windows
```

### Build fails with "npm not found"

```bash
# Install Node.js
# From https://nodejs.org/

# Verify installation
node --version
npm --version

# Should be Node 18+ and npm 9+
```

### APK installation fails

```bash
# Clear previous installation
adb uninstall org.verumomnis.forensic

# Install fresh
adb install -r app/build/outputs/apk/release/app-release.apk

# Check for errors
adb logcat | grep verumomnis
```

### Web build is too large

```bash
# Analyze bundle
npm install -g webpack-bundle-analyzer

# Build and analyze
cd web
npm run build:prod

# Check for large dependencies
npm ls
```

---

## 📊 Performance Tuning

### Faster Gradle Builds

Add to `gradle.properties`:
```properties
org.gradle.parallel=true
org.gradle.workers.max=8
org.gradle.caching=true
org.gradle.vfs.watch=true
```

### Faster npm Builds

```bash
# Use npm ci instead of install
npm ci

# Clear cache
npm cache clean --force

# Use latest npm
npm install -g npm@latest
```

---

## 🔄 Continuous Integration

### GitHub Actions

The project includes automated CI/CD in `.github/workflows/build.yml`

**Runs on**:
- Every push to `main` or `develop`
- Every pull request
- Manual trigger (workflow_dispatch)

**Builds**:
- ✓ Android APK
- ✓ Web application
- ✓ Runs tests
- ✓ Security scanning
- ✓ Creates releases for tags

**View results**: https://github.com/liamtest26/Verumomnis/actions

---

## 📚 Additional Resources

- [PRODUCTION_BUILD_GUIDE.md](PRODUCTION_BUILD_GUIDE.md) - Complete build guide with deployment
- [README.md](README.md) - Project overview
- [LEGAL_API_DOCUMENTATION.md](LEGAL_API_DOCUMENTATION.md) - API reference
- [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md) - Pre-deployment checklist

---

## ❓ FAQ

**Q: How do I build on Windows?**  
A: Use `.\build.ps1 -Command both` (PowerShell) or `bash build.sh both` (Git Bash)

**Q: Can I build on Mac?**  
A: Yes, use `./build.sh both` after installing JDK 17, Gradle, and Node.js

**Q: How do I deploy to Play Store?**  
A: See [PRODUCTION_BUILD_GUIDE.md](PRODUCTION_BUILD_GUIDE.md#deploy-android-apk)

**Q: Where are the APKs saved?**  
A: In `app/build/outputs/apk/release/`

**Q: How do I verify the APK hash?**  
A: Run `./build.sh verify` or check manually with `sha256sum app-release.apk`

**Q: Can I build offline?**  
A: After dependencies are downloaded, yes (except npm would need cached packages)

**Q: How often should I update dependencies?**  
A: Check quarterly with `npm outdated` and `gradle dependencyUpdates`

---

## 📞 Support

For issues:
1. Check [PRODUCTION_BUILD_GUIDE.md](PRODUCTION_BUILD_GUIDE.md#troubleshooting)
2. Review build logs: `cat build_*.log`
3. Check system requirements: `./build.sh requirements`
4. Open an issue on GitHub

---

**Last Updated**: January 21, 2026  
**Version**: 5.2.7  
**Status**: ✓ Production Ready
