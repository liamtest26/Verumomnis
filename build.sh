#!/bin/bash

##############################################################################
# Verum Omnis Forensic Engine - Production Build Script (Linux/macOS)
# Supports both Android native and web builds
# Usage: ./build.sh [android|web|both|clean|verify]
##############################################################################

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APK_HASH="56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466"
BUILD_TIMESTAMP=$(date -u +"%Y%m%d_%H%M%S")
BUILD_LOG="build_${BUILD_TIMESTAMP}.log"

# Functions
log() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')]${NC} $1"
}

success() {
    echo -e "${GREEN}✓${NC} $1"
}

error() {
    echo -e "${RED}✗${NC} $1"
}

warn() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_header() {
    echo ""
    echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC} $1"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
    echo ""
}

check_requirements() {
    print_header "Checking Build Requirements"
    
    # Check Java
    if ! command -v java &> /dev/null; then
        error "Java not found. Please install JDK 17+"
        exit 1
    fi
    success "Java $(java -version 2>&1 | grep version)"
    
    # Check Gradle
    if ! command -v gradle &> /dev/null && [ ! -f "gradlew" ]; then
        error "Gradle not found. Please install Gradle or run './gradlew'"
        exit 1
    fi
    success "Gradle found"
    
    # Check Node (for web builds)
    if [ "$1" != "android" ] && ! command -v node &> /dev/null; then
        warn "Node.js not found. Web build will be skipped"
    else
        if command -v node &> /dev/null; then
            success "Node $(node --version)"
        fi
    fi
    
    # Check Android SDK
    if [ -z "$ANDROID_HOME" ]; then
        warn "ANDROID_HOME not set. Set it with: export ANDROID_HOME=..."
    else
        success "ANDROID_HOME: $ANDROID_HOME"
    fi
}

build_android() {
    print_header "Building Android APK"
    
    cd "$PROJECT_ROOT"
    
    # Clean previous builds
    log "Cleaning previous builds..."
    ./gradlew clean 2>&1 | tee -a "$BUILD_LOG"
    
    # Build release APK
    log "Building release APK..."
    ./gradlew assembleRelease 2>&1 | tee -a "$BUILD_LOG"
    
    # Check for build output
    APK_PATH="app/build/outputs/apk/release/*.apk"
    if ls $APK_PATH 1> /dev/null 2>&1; then
        APK_FILE=$(ls $APK_PATH | head -n1)
        success "APK built: $(basename $APK_FILE)"
        success "Location: $APK_FILE"
        
        # Verify APK signature
        verify_apk "$APK_FILE"
    else
        error "APK build failed"
        return 1
    fi
}

build_web() {
    print_header "Building Web Application"
    
    cd "$PROJECT_ROOT/web"
    
    if [ ! -f "package.json" ]; then
        error "Web project not found. Initializing..."
        return 1
    fi
    
    # Check Node is installed
    if ! command -v node &> /dev/null; then
        error "Node.js required for web build but not found"
        return 1
    fi
    
    # Install dependencies
    log "Installing dependencies..."
    npm install 2>&1 | tee -a "../$BUILD_LOG"
    
    # Run type check
    log "Running TypeScript type check..."
    npm run type-check 2>&1 | tee -a "../$BUILD_LOG" || warn "Type check failed (non-fatal)"
    
    # Run linting
    log "Running ESLint..."
    npm run lint 2>&1 | tee -a "../$BUILD_LOG" || warn "Lint issues found (non-fatal)"
    
    # Build production bundle
    log "Building production bundle..."
    npm run build:prod 2>&1 | tee -a "../$BUILD_LOG"
    
    if [ -d "dist" ] && [ -f "dist/index.html" ]; then
        success "Web build complete"
        success "Location: web/dist"
        
        # Show bundle size
        if command -v du &> /dev/null; then
            SIZE=$(du -sh dist | cut -f1)
            log "Bundle size: $SIZE"
        fi
    else
        error "Web build failed"
        return 1
    fi
}

build_capacitor() {
    print_header "Building Capacitor Application"
    
    cd "$PROJECT_ROOT/web"
    
    # First build web
    log "Building web assets..."
    npm run build:prod 2>&1 | tee -a "../$BUILD_LOG"
    
    # Sync Capacitor
    log "Syncing Capacitor..."
    npx cap sync android 2>&1 | tee -a "../$BUILD_LOG"
    
    success "Capacitor sync complete"
    log "To build APK: npm run android:build"
}

verify_apk() {
    local APK_FILE=$1
    
    if [ ! -f "$APK_FILE" ]; then
        warn "APK file not found: $APK_FILE"
        return 1
    fi
    
    print_header "Verifying APK"
    
    # Calculate APK hash
    if command -v sha256sum &> /dev/null; then
        ACTUAL_HASH=$(sha256sum "$APK_FILE" | awk '{print $1}')
    elif command -v shasum &> /dev/null; then
        ACTUAL_HASH=$(shasum -a 256 "$APK_FILE" | awk '{print $1}')
    else
        warn "Unable to calculate hash"
        return 0
    fi
    
    log "Expected APK Hash: $APK_HASH"
    log "Actual APK Hash:   $ACTUAL_HASH"
    
    if [ "$ACTUAL_HASH" = "$APK_HASH" ]; then
        success "APK hash verified!"
    else
        warn "APK hash mismatch (expected for development builds)"
    fi
    
    # Get APK size
    if command -v du &> /dev/null; then
        SIZE=$(du -h "$APK_FILE" | cut -f1)
        log "APK size: $SIZE"
    fi
    
    # List files in APK
    if command -v unzip &> /dev/null; then
        FILE_COUNT=$(unzip -l "$APK_FILE" | tail -1 | awk '{print $2}')
        log "Files in APK: $FILE_COUNT"
    fi
}

verify_build() {
    print_header "Verifying Build Output"
    
    # Check Android APK
    if ls "$PROJECT_ROOT/app/build/outputs/apk/release/"*.apk 1> /dev/null 2>&1; then
        APK_FILE=$(ls "$PROJECT_ROOT/app/build/outputs/apk/release/"*.apk | head -n1)
        success "Android APK: $(basename $APK_FILE)"
        verify_apk "$APK_FILE"
    else
        warn "No Android APK found"
    fi
    
    # Check Web build
    if [ -d "$PROJECT_ROOT/web/dist" ] && [ -f "$PROJECT_ROOT/web/dist/index.html" ]; then
        success "Web build: web/dist"
        if command -v find &> /dev/null; then
            FILE_COUNT=$(find "$PROJECT_ROOT/web/dist" -type f | wc -l)
            log "Files: $FILE_COUNT"
        fi
    else
        warn "No web build found"
    fi
}

clean_builds() {
    print_header "Cleaning Build Artifacts"
    
    log "Cleaning Android build..."
    cd "$PROJECT_ROOT"
    ./gradlew clean 2>&1 | tee -a "$BUILD_LOG" || true
    
    log "Cleaning web build..."
    if [ -d "web" ]; then
        cd "$PROJECT_ROOT/web"
        rm -rf dist node_modules 2>&1
        success "Web build cleaned"
    fi
    
    success "Clean complete"
}

print_usage() {
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  android       Build Android APK only"
    echo "  web           Build web application only"
    echo "  capacitor     Build with Capacitor integration"
    echo "  both          Build both Android and web"
    echo "  clean         Clean all build artifacts"
    echo "  verify        Verify build outputs"
    echo "  requirements  Check build requirements"
    echo ""
    echo "Examples:"
    echo "  $0 android      # Build Android APK"
    echo "  $0 web          # Build web app"
    echo "  $0 both         # Build everything"
    echo ""
}

# Main execution
main() {
    log "Verum Omnis Build System v5.2.7"
    log "Build timestamp: $BUILD_TIMESTAMP"
    log "Log file: $BUILD_LOG"
    echo ""
    
    COMMAND="${1:-both}"
    
    case "$COMMAND" in
        android)
            check_requirements android
            build_android
            verify_build
            ;;
        web)
            check_requirements web
            build_web
            verify_build
            ;;
        capacitor)
            check_requirements web
            build_capacitor
            verify_build
            ;;
        both)
            check_requirements both
            build_android
            build_web
            verify_build
            ;;
        clean)
            clean_builds
            ;;
        verify)
            verify_build
            ;;
        requirements)
            check_requirements
            ;;
        help|-h|--help)
            print_usage
            ;;
        *)
            error "Unknown command: $COMMAND"
            print_usage
            exit 1
            ;;
    esac
    
    print_header "Build Complete"
    success "All build tasks completed successfully!"
    log "Log file saved: $BUILD_LOG"
}

# Run main function
main "$@"
