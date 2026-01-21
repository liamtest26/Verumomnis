# Verum Omnis - Production Build & Deployment Guide

**Version**: 5.2.7  
**Status**: Production Ready  
**Last Updated**: January 21, 2026  
**Platforms**: Android (native), Web, Capacitor Hybrid

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [System Requirements](#system-requirements)
3. [Build Configuration](#build-configuration)
4. [Building Android APK](#building-android-apk)
5. [Building Web Application](#building-web-application)
6. [Building Capacitor Hybrid](#building-capacitor-hybrid)
7. [Cross-Platform Builds](#cross-platform-builds)
8. [Verification & Testing](#verification--testing)
9. [Production Deployment](#production-deployment)
10. [Troubleshooting](#troubleshooting)

---

## Quick Start

### Linux/macOS
```bash
# Make build script executable
chmod +x build.sh

# Build both Android and web
./build.sh both

# Or build specific target
./build.sh android     # Android APK only
./build.sh web         # Web app only
./build.sh capacitor   # Capacitor hybrid
```

### Windows (PowerShell)
```powershell
# Build both Android and web
.\build.ps1 -Command both

# Or build specific target
.\build.ps1 -Command android     # Android APK only
.\build.ps1 -Command web         # Web app only
.\build.ps1 -Command capacitor   # Capacitor hybrid
```

### Git (Cross-Platform)
```bash
# Clone repository
git clone https://github.com/liamtest26/Verumomnis.git
cd Verumomnis

# Build using native build scripts
# Linux/macOS:
./build.sh both

# Windows (Git Bash or WSL):
bash build.sh both

# Windows (PowerShell):
.\build.ps1 -Command both
```

---

## System Requirements

### Minimum Requirements
| Component | Minimum | Recommended |
|-----------|---------|-------------|
| Java | JDK 11 | JDK 17+ |
| Gradle | 7.0 | 8.x |
| Node.js | 16.x | 18.x+ |
| Android SDK | API 24 | API 34+ |
| RAM | 4GB | 8GB+ |
| Disk | 10GB | 20GB+ |

### Platform-Specific Setup

#### Linux (Ubuntu 20.04+)
```bash
# Update system
sudo apt-get update && sudo apt-get upgrade -y

# Install JDK 17
sudo apt-get install -y openjdk-17-jdk

# Install Gradle
sudo apt-get install -y gradle

# Install Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Set environment variables
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH
```

#### macOS
```bash
# Install Homebrew if not installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install dependencies
brew install openjdk@17 gradle node android-sdk

# Set environment variables
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH
```

#### Windows
```powershell
# Install using Chocolatey (if not installed)
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install dependencies
choco install openjdk17 gradle nodejs android-sdk -y

# Set environment variables
$env:JAVA_HOME = "C:\Program Files\OpenJDK\jdk-17.x.x"
$env:ANDROID_HOME = "C:\Android\sdk"
$env:Path += ";$env:JAVA_HOME\bin;$env:ANDROID_HOME\tools;$env:ANDROID_HOME\platform-tools"
```

---

## Build Configuration

### Gradle Configuration
[app/build.gradle.kts](app/build.gradle.kts) includes:
- Target SDK 34 (Android 14)
- Min SDK 24 (Android 7.0)
- ProGuard obfuscation for release builds
- ViewBinding enabled
- Kotlin 1.9.20

### Web Configuration
[web/package.json](web/package.json) defines:
- Vite build tool
- React 18.2
- TypeScript 5.3
- Capacitor 6.0
- Build scripts for web/Android/iOS

### Build Properties
[gradle.properties](gradle.properties) contains:
```properties
# Gradle Configuration
org.gradle.jvmargs=-Xmx2048m
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true

# Android Configuration
android.useAndroidX=true
android.enableJetifier=true

# Build Configuration
android.enableOnDemandModules=true
android.suppressUnsupportedCompileSdk=33
```

---

## Building Android APK

### Development Build
```bash
# Linux/macOS
./build.sh android

# Windows PowerShell
.\build.ps1 -Command android
```

**Output**: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build (Production)
```bash
# Using build script
./build.sh android              # Linux/macOS
.\build.ps1 -Command android    # Windows

# Or manual Gradle
./gradlew assembleRelease       # Linux/macOS/Windows Bash
.\gradlew.bat assembleRelease   # Windows CMD
```

**Output**: `app/build/outputs/apk/release/app-release.apk`

### Signing Release APK

**Important**: Production APKs must be signed with your release key.

#### Generate Signing Key
```bash
# Generate keystore (one-time)
keytool -genkey -v -keystore release.jks \
  -keyalg RSA -keysize 2048 -validity 36500 \
  -alias verumomnis

# When prompted:
# - Key password: (strong password)
# - Store password: (strong password)
# - First name: Your Name
# - Organization: Verum Omnis
# - Country: ZA (or your country)
```

#### Sign APK
```bash
# Sign the release APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore release.jks \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  verumomnis

# Verify signature
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk

# Optimize with zipalign
zipalign -v 4 app/build/outputs/apk/release/app-release.apk \
  app-release-final.apk
```

#### Automate Signing in build.gradle.kts
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("release.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = "verumomnis"
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        isMinifyEnabled = true
        proguardFiles(...)
    }
}
```

### APK Verification

```bash
# Generate APK hash
sha256sum app-release-final.apk
# Expected: 56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466

# Verify APK contents
unzip -t app-release-final.apk

# Check manifest
aapt dump badging app-release-final.apk
```

---

## Building Web Application

### Install Dependencies
```bash
cd web
npm install
# or
yarn install
```

### Development Build
```bash
# Start dev server (hot reload)
npm run serve          # Runs on http://localhost:3000

# Build for testing
npm run build
npm run preview        # Preview production build
```

### Production Build
```bash
npm run build:prod

# Output: web/dist/
# Ready for deployment to web servers
```

### Type Checking
```bash
npm run type-check     # TypeScript validation
```

### Linting
```bash
npm run lint           # ESLint validation
npm run lint:fix       # Auto-fix issues
```

### Testing
```bash
npm run test           # Run unit tests
```

---

## Building Capacitor Hybrid

### Setup Capacitor
```bash
cd web

# Install Capacitor CLI
npm install -g @capacitor/cli

# Add platform
npx cap add android
npx cap add ios

# Or use build script
./build.sh capacitor
.\build.ps1 -Command capacitor
```

### Build Process

```bash
# 1. Build web assets
npm run build:prod

# 2. Sync to native platforms
npx cap sync android
npx cap sync ios

# 3. Open native IDE
npx cap open android   # Opens Android Studio
npx cap open ios       # Opens Xcode

# Or build APK directly
npm run android:build  # Opens Android Studio to build
npm run ios:build      # Opens Xcode to build
```

### Native Build from Android Studio
1. Open `web/android` in Android Studio
2. Select "Build" → "Build Bundle(s) / APK(s)" → "Build APK(s)"
3. Sign APK when prompted
4. APK located in `web/android/app/build/outputs/apk/release/`

---

## Cross-Platform Builds

### Build on Git (Any Platform)
```bash
# Clone repository
git clone https://github.com/liamtest26/Verumomnis.git
cd Verumomnis

# Install Git (if not already installed)
# Windows: https://git-scm.com/download/win
# macOS: brew install git
# Linux: sudo apt-get install git

# Checkout correct branch
git checkout main

# Build using appropriate script
# Linux/macOS/WSL:
bash build.sh both

# Windows (PowerShell):
powershell -ExecutionPolicy Bypass -File build.ps1 -Command both

# Windows (Git Bash):
bash build.sh both
```

### GitHub Actions CI/CD

Create `.github/workflows/build.yml`:

```yaml
name: Build

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'adopt'
      - name: Build Android APK
        run: |
          chmod +x build.sh
          ./build.sh android
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: android-apk
          path: app/build/outputs/apk/release/*.apk

  build-web:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Node
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      - name: Build Web
        run: |
          chmod +x build.sh
          ./build.sh web
      - name: Upload Web Build
        uses: actions/upload-artifact@v3
        with:
          name: web-build
          path: web/dist
```

---

## Verification & Testing

### Run Verification
```bash
# Linux/macOS
./build.sh verify

# Windows PowerShell
.\build.ps1 -Command verify
```

### Manual Verification

#### Android APK
```bash
# 1. Hash verification
sha256sum app-release-final.apk
# Expected: 56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466

# 2. APK integrity
unzip -t app-release-final.apk

# 3. Manifest inspection
aapt dump badging app-release-final.apk

# 4. Size check (should be reasonable)
ls -lh app-release-final.apk

# 5. Install on emulator/device
adb install -r app-release-final.apk

# 6. Run app verification
adb shell am start -n org.verumomnis.forensic/.MainActivity
```

#### Web Build
```bash
# 1. Check build output
ls -la web/dist

# 2. Verify key files
test -f web/dist/index.html && echo "✓ index.html found"
test -f web/dist/manifest.json && echo "✓ manifest.json found"

# 3. Check bundle size
du -sh web/dist

# 4. Test locally
cd web && npm run preview
# Visit http://localhost:5000

# 5. Security scan
npm audit
```

### Functional Testing

#### Android APK Testing
1. **Installation**: `adb install app-release-final.apk`
2. **Launch**: `adb shell am start -n org.verumomnis.forensic/.MainActivity`
3. **APK Verification**: App shows hash `56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466`
4. **Camera**: Test document scanning
5. **PDF Generation**: Create sealed document
6. **Document Processing**: Process multi-page documents
7. **Offline**: Verify works without internet

#### Web Testing
1. **Load**: http://localhost:5000
2. **Navigation**: Test all routes
3. **File Upload**: Upload documents
4. **Processing**: Generate sealed reports
5. **Export**: Download generated PDFs
6. **Security**: Test CSP headers, no console errors
7. **Performance**: Check load times

---

## Production Deployment

### Deploy Android APK

#### Google Play Store
1. Create developer account: https://play.google.com/apps/publish
2. Create application
3. Upload signed APK
4. Set store listing, permissions, content rating
5. Submit for review

**Required Files**:
- Signed APK (app-release-final.apk)
- App icon (512x512 PNG)
- Feature graphic (1024x500 PNG)
- Screenshots (2-8 per size)
- Description, privacy policy

#### Direct Distribution (APK)
```bash
# Host on your server
scp app-release-final.apk user@server:/var/www/apk/

# Users can download and install
# adb install https://your-server.com/apk/app-release-final.apk

# Or generate QR code
qrcode "https://your-server.com/apk/app-release-final.apk"
```

### Deploy Web Application

#### Web Server (Nginx)
```nginx
server {
    listen 80;
    server_name api.verumomnis.org;
    
    root /var/www/verumomnis/dist;
    index index.html;
    
    # Single Page App routing
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # Static assets caching
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # Security headers
    add_header X-Content-Type-Options "nosniff";
    add_header X-Frame-Options "SAMEORIGIN";
    add_header X-XSS-Protection "1; mode=block";
    add_header Referrer-Policy "no-referrer-when-downgrade";
    
    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
}
```

#### HTTPS Configuration
```bash
# Install Certbot
sudo apt-get install certbot python3-certbot-nginx

# Get certificate
sudo certbot certonly --nginx -d api.verumomnis.org

# Auto-renewal
sudo systemctl enable certbot.timer
```

#### Deploy Script
```bash
#!/bin/bash
set -e

# Pull latest code
cd /var/www/verumomnis
git pull origin main

# Build web app
cd web
npm install
npm run build:prod

# Copy to web root
sudo cp -r dist/* /var/www/html/

# Set permissions
sudo chown -R www-data:www-data /var/www/html

# Reload web server
sudo systemctl reload nginx

echo "✓ Deployment complete"
```

### Docker Deployment

Create `Dockerfile`:
```dockerfile
FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build:prod

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

Build and run:
```bash
# Build image
docker build -t verumomnis:5.2.7 .

# Run container
docker run -p 80:80 verumomnis:5.2.7

# Push to registry
docker tag verumomnis:5.2.7 registry.example.com/verumomnis:5.2.7
docker push registry.example.com/verumomnis:5.2.7
```

---

## Troubleshooting

### Build Failures

#### "Java not found"
```bash
# Install JDK 17
# Ubuntu
sudo apt-get install openjdk-17-jdk

# macOS
brew install openjdk@17

# Windows
choco install openjdk17

# Set JAVA_HOME
export JAVA_HOME=/path/to/java
```

#### "Gradle not found"
```bash
# Linux/macOS
chmod +x gradlew
./gradlew --version

# Windows
gradlew.bat --version
```

#### "Node not found" (web build)
```bash
# Install Node 18+
# https://nodejs.org/

# Verify
node --version
npm --version
```

#### "Android SDK not found"
```bash
# Set ANDROID_HOME
export ANDROID_HOME=$HOME/Android/Sdk

# Or install Android Studio
# https://developer.android.com/studio
```

### APK Issues

#### "APK build failed"
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleRelease

# Check logs
cat app/build/reports/lint-results*.html

# Debug
./gradlew assembleRelease --debug
```

#### "APK won't install"
```bash
# Check device compatibility
adb devices

# Clear previous installation
adb uninstall org.verumomnis.forensic

# Reinstall
adb install -r app-release-final.apk

# Check for errors
adb logcat | grep verumomnis
```

### Web Build Issues

#### "npm install fails"
```bash
# Clear cache
npm cache clean --force
rm -rf node_modules package-lock.json

# Reinstall
npm install --legacy-peer-deps
```

#### "Port 3000 already in use"
```bash
# Use different port
npm run serve -- --port 3001

# Or kill process
lsof -i :3000
kill -9 <PID>
```

### Performance Issues

#### "Build slow"
```bash
# Enable parallel gradle
# In gradle.properties
org.gradle.parallel=true
org.gradle.workers.max=8

# Or set via command line
./gradlew build -Dorg.gradle.workers.max=8
```

#### "Large bundle size"
```bash
# Analyze web bundle
npm install -g webpack-bundle-analyzer

# Check what's included
npm run build:prod
```

---

## Maintenance

### Update Dependencies

#### Android (Gradle)
```bash
# Check for updates
./gradlew dependencyUpdates

# Update to latest
./gradlew --refresh-dependencies build
```

#### Web (npm)
```bash
# Check for updates
npm outdated

# Update all
npm update

# Update to latest major versions
npm install -g npm-check-updates
ncu -u
npm install
```

### Release New Version

1. Update version in:
   - `app/build.gradle.kts`: `versionName = "X.Y.Z"`
   - `web/package.json`: `"version": "X.Y.Z"`

2. Update [CHANGELOG.md](CHANGELOG.md)

3. Create release build
   ```bash
   ./build.sh both
   ```

4. Tag release
   ```bash
   git tag v5.2.8
   git push origin v5.2.8
   ```

5. Create GitHub release with binaries

---

## Support & Documentation

- [README.md](README.md) - Project overview
- [LEGAL_API_DOCUMENTATION.md](LEGAL_API_DOCUMENTATION.md) - API reference
- [LEGAL_API_CONFIGURATION.md](LEGAL_API_CONFIGURATION.md) - Configuration guide
- [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md) - Pre-deployment checklist

---

**Last Updated**: January 21, 2026  
**Version**: 5.2.7  
**Status**: Production Ready ✓
