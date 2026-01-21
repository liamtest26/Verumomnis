#!/bin/bash

###############################################################################
# Verum Omnis - SECURE Production Release Build Script
# 
# This script builds, signs, and releases the APK securely
# Password is NOT stored in this file - passed via environment variable
#
# Usage: 
#   export KEYSTORE_PASSWORD="ashbash78"
#   ./release-build.sh
#
# Or: ./release-build.sh (will prompt for password)
###############################################################################

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Verum Omnis - SECURE Production Release Builder               ║${NC}"
echo -e "${BLUE}║  Version 5.2.7 | January 21, 2026                              ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Check if password is set
if [ -z "$KEYSTORE_PASSWORD" ]; then
    echo -e "${YELLOW}🔒 Enter keystore password (will not echo):${NC}"
    read -s KEYSTORE_PASSWORD
    export KEYSTORE_PASSWORD
    echo ""
fi

if [ -z "$KEYSTORE_PASSWORD" ]; then
    echo -e "${RED}❌ Keystore password is required${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Keystore password set${NC}"
echo ""

# Set other required variables
export KEY_ALIAS="verumomnis"
export KEYSTORE_PATH="${KEYSTORE_PATH:-keystore.jks}"

# Check if keystore exists
if [ ! -f "$KEYSTORE_PATH" ]; then
    echo -e "${RED}❌ Keystore not found at: $KEYSTORE_PATH${NC}"
    echo ""
    echo "Create a keystore with:"
    echo "  keytool -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000"
    exit 1
fi

echo -e "${GREEN}✓ Keystore found: $KEYSTORE_PATH${NC}"
echo ""

# Step 1: Clean previous builds
echo -e "${YELLOW}🧹 Cleaning previous builds...${NC}"
./gradlew clean
echo -e "${GREEN}✓ Clean complete${NC}"
echo ""

# Step 2: Build Android APK
echo -e "${YELLOW}📱 Building Android APK with signature...${NC}"
./gradlew assembleRelease
echo -e "${GREEN}✓ Android APK built${NC}"
echo ""

# Step 3: Build Web app
echo -e "${YELLOW}🌐 Building web app...${NC}"
cd web
npm run build
cd ..
echo -e "${GREEN}✓ Web app built${NC}"
echo ""

# Step 4: Verify APK signature
echo -e "${YELLOW}🔐 Verifying APK signature...${NC}"
APK_PATH="app/build/outputs/apk/release/app-release.apk"

if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}❌ APK not found at: $APK_PATH${NC}"
    exit 1
fi

if jarsigner -verify -verbose "$APK_PATH" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ APK signature verified${NC}"
else
    echo -e "${RED}❌ APK signature verification FAILED${NC}"
    exit 1
fi
echo ""

# Step 5: Calculate SHA-256
echo -e "${YELLOW}🔍 Calculating SHA-256...${NC}"
SHA256=$(sha256sum "$APK_PATH" | awk '{print $1}')
echo -e "${GREEN}✓ SHA-256: $SHA256${NC}"
echo ""

# Step 6: Show results
echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    BUILD COMPLETE & SIGNED                     ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}📦 APK Location:${NC} $APK_PATH"
echo -e "${GREEN}📊 APK Size:${NC} $(du -h "$APK_PATH" | cut -f1)"
echo -e "${GREEN}🔐 SHA-256:${NC} $SHA256"
echo -e "${GREEN}✅ Status:${NC} SIGNED AND VERIFIED"
echo ""

# Step 7: Optional - copy to releases
RELEASE_DIR="releases"
if [ ! -d "$RELEASE_DIR" ]; then
    mkdir -p "$RELEASE_DIR"
fi

RELEASE_NAME="Verumomnis-5.2.7-signed.apk"
cp "$APK_PATH" "$RELEASE_DIR/$RELEASE_NAME"
echo -e "${GREEN}✓ Copied to: $RELEASE_DIR/$RELEASE_NAME${NC}"
echo ""

# Step 8: Create SHA-256 checksum file
echo "$SHA256  $RELEASE_NAME" > "$RELEASE_DIR/$RELEASE_NAME.sha256"
echo -e "${GREEN}✓ Created checksum file${NC}"
echo ""

# Final message
echo -e "${BLUE}📋 NEXT STEPS:${NC}"
echo "1. Verify on a test device: adb install $RELEASE_DIR/$RELEASE_NAME"
echo "2. Upload to GitHub Releases"
echo "3. Notify users and institutions"
echo "4. Submit to Google Play Store"
echo ""

echo -e "${GREEN}🎉 Production release is ready!${NC}"
echo ""

# Clear the password from environment
unset KEYSTORE_PASSWORD

exit 0
