##############################################################################
# Verum Omnis Forensic Engine - Production Build Script (Windows PowerShell)
# Supports both Android native and web builds
# Usage: .\build.ps1 -Command "android|web|both|clean|verify"
##############################################################################

param(
    [Parameter(Position = 0)]
    [ValidateSet("android", "web", "capacitor", "both", "clean", "verify", "requirements", "help")]
    [string]$Command = "both",
    
    [switch]$Verbose,
    [switch]$Help
)

# Set error action preference
$ErrorActionPreference = "Stop"

# Configuration
$ProjectRoot = $PSScriptRoot
$APKHash = "56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466"
$BuildTimestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$BuildLog = "build_$BuildTimestamp.log"

# Color functions
function Write-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

function Write-Warn {
    param([string]$Message)
    Write-Host "⚠ $Message" -ForegroundColor Yellow
}

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor Cyan
    Add-Content -Path "$ProjectRoot\$BuildLog" -Value "[$timestamp] $Message"
}

function Print-Header {
    param([string]$Title)
    Write-Host ""
    Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║ $Title" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
}

function Check-Requirements {
    param([string]$BuildType = "both")
    
    Print-Header "Checking Build Requirements"
    
    # Check Java
    try {
        $javaVersion = java -version 2>&1 | Select-String "version"
        Write-Success "Java found: $javaVersion"
    }
    catch {
        Write-Error-Custom "Java not found. Please install JDK 17+"
        exit 1
    }
    
    # Check Gradle wrapper or Gradle
    if ((Test-Path ".\gradlew.bat") -or (Get-Command gradle -ErrorAction SilentlyContinue)) {
        Write-Success "Gradle found"
    }
    else {
        Write-Error-Custom "Gradle not found. Please install Gradle or ensure gradlew.bat exists"
        exit 1
    }
    
    # Check Node (for web builds)
    if ($BuildType -ne "android") {
        try {
            $nodeVersion = node --version
            Write-Success "Node.js $nodeVersion"
        }
        catch {
            Write-Warn "Node.js not found. Web build will be skipped"
        }
    }
    
    # Check Android SDK
    if ($env:ANDROID_HOME) {
        Write-Success "ANDROID_HOME: $env:ANDROID_HOME"
    }
    else {
        Write-Warn "ANDROID_HOME not set. Set it with: `$env:ANDROID_HOME = 'C:\Android\sdk'"
    }
    
    # Check Git
    try {
        $gitVersion = git --version
        Write-Success "$gitVersion"
    }
    catch {
        Write-Warn "Git not found (optional)"
    }
}

function Build-Android {
    Print-Header "Building Android APK"
    
    Set-Location $ProjectRoot
    
    # Clean previous builds
    Write-Log "Cleaning previous builds..."
    & .\gradlew.bat clean *>> $BuildLog
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "Gradle clean failed"
        return $false
    }
    
    # Build release APK
    Write-Log "Building release APK..."
    & .\gradlew.bat assembleRelease *>> $BuildLog
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "APK build failed"
        return $false
    }
    
    # Check for build output
    $apkPath = Get-ChildItem -Path "app\build\outputs\apk\release\*.apk" -ErrorAction SilentlyContinue | Select-Object -First 1
    
    if ($apkPath) {
        Write-Success "APK built: $($apkPath.Name)"
        Write-Success "Location: $($apkPath.FullName)"
        
        # Verify APK
        Verify-APK -APKPath $apkPath.FullName
        return $true
    }
    else {
        Write-Error-Custom "APK file not found in output directory"
        return $false
    }
}

function Build-Web {
    Print-Header "Building Web Application"
    
    Set-Location "$ProjectRoot\web"
    
    if (-not (Test-Path "package.json")) {
        Write-Error-Custom "Web project not found. Initializing..."
        return $false
    }
    
    # Check Node
    try {
        $null = node --version
    }
    catch {
        Write-Error-Custom "Node.js required for web build but not found"
        return $false
    }
    
    # Install dependencies
    Write-Log "Installing dependencies..."
    npm install *>> "$ProjectRoot\$BuildLog"
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "npm install failed"
        return $false
    }
    
    # Run type check
    Write-Log "Running TypeScript type check..."
    npm run type-check *>> "$ProjectRoot\$BuildLog"
    if ($LASTEXITCODE -ne 0) {
        Write-Warn "Type check failed (non-fatal)"
    }
    
    # Run linting
    Write-Log "Running ESLint..."
    npm run lint *>> "$ProjectRoot\$BuildLog"
    if ($LASTEXITCODE -ne 0) {
        Write-Warn "Lint issues found (non-fatal)"
    }
    
    # Build production
    Write-Log "Building production bundle..."
    npm run build:prod *>> "$ProjectRoot\$BuildLog"
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "Web build failed"
        return $false
    }
    
    # Check for output
    if ((Test-Path "dist") -and (Test-Path "dist\index.html")) {
        Write-Success "Web build complete"
        Write-Success "Location: web/dist"
        
        $distSize = (Get-ChildItem -Path "dist" -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
        Write-Log "Bundle size: $([Math]::Round($distSize, 2)) MB"
        return $true
    }
    else {
        Write-Error-Custom "Web build output not found"
        return $false
    }
}

function Build-Capacitor {
    Print-Header "Building Capacitor Application"
    
    Set-Location "$ProjectRoot\web"
    
    # Build web
    Write-Log "Building web assets..."
    npm run build:prod *>> "$ProjectRoot\$BuildLog"
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "Web build failed"
        return $false
    }
    
    # Sync Capacitor
    Write-Log "Syncing Capacitor..."
    npx cap sync android *>> "$ProjectRoot\$BuildLog"
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "Capacitor sync failed"
        return $false
    }
    
    Write-Success "Capacitor sync complete"
    Write-Log "To build APK: npm run android:build"
    return $true
}

function Verify-APK {
    param([string]$APKPath)
    
    if (-not (Test-Path $APKPath)) {
        Write-Warn "APK file not found: $APKPath"
        return
    }
    
    Print-Header "Verifying APK"
    
    # Calculate APK hash
    $actualHash = (Get-FileHash -Path $APKPath -Algorithm SHA256).Hash.ToLower()
    
    Write-Log "Expected APK Hash: $APKHash"
    Write-Log "Actual APK Hash:   $actualHash"
    
    if ($actualHash -eq $APKHash) {
        Write-Success "APK hash verified!"
    }
    else {
        Write-Warn "APK hash mismatch (expected for development builds)"
    }
    
    # Get APK size
    $apkSize = (Get-Item $APKPath).Length / 1MB
    Write-Log "APK size: $([Math]::Round($apkSize, 2)) MB"
    
    # Count files in APK (using temp extraction)
    $tempDir = New-TemporaryDirectory
    Expand-Archive -Path $APKPath -DestinationPath $tempDir -ErrorAction SilentlyContinue
    $fileCount = (Get-ChildItem -Path $tempDir -Recurse).Count
    Write-Log "Files in APK: $fileCount"
    Remove-Item -Path $tempDir -Recurse -Force
}

function New-TemporaryDirectory {
    $tempDir = Join-Path -Path ([System.IO.Path]::GetTempPath()) -ChildPath ([System.Guid]::NewGuid().ToString())
    New-Item -ItemType Directory -Path $tempDir | Out-Null
    return $tempDir
}

function Verify-Build {
    Print-Header "Verifying Build Output"
    
    # Check Android APK
    $apkFiles = Get-ChildItem -Path "$ProjectRoot\app\build\outputs\apk\release\*.apk" -ErrorAction SilentlyContinue
    if ($apkFiles) {
        $apkFile = $apkFiles | Select-Object -First 1
        Write-Success "Android APK: $($apkFile.Name)"
        Verify-APK -APKPath $apkFile.FullName
    }
    else {
        Write-Warn "No Android APK found"
    }
    
    # Check Web build
    if ((Test-Path "$ProjectRoot\web\dist") -and (Test-Path "$ProjectRoot\web\dist\index.html")) {
        Write-Success "Web build: web/dist"
        $fileCount = (Get-ChildItem -Path "$ProjectRoot\web\dist" -Recurse).Count
        Write-Log "Files: $fileCount"
    }
    else {
        Write-Warn "No web build found"
    }
}

function Clean-Builds {
    Print-Header "Cleaning Build Artifacts"
    
    Set-Location $ProjectRoot
    
    # Clean Android
    Write-Log "Cleaning Android build..."
    & .\gradlew.bat clean *>> $BuildLog
    Write-Success "Android build cleaned"
    
    # Clean Web
    Write-Log "Cleaning web build..."
    if (Test-Path "web") {
        Set-Location "web"
        if (Test-Path "dist") {
            Remove-Item -Path "dist" -Recurse -Force
        }
        if (Test-Path "node_modules") {
            Remove-Item -Path "node_modules" -Recurse -Force
        }
        Write-Success "Web build cleaned"
    }
    
    Set-Location $ProjectRoot
    Write-Success "Clean complete"
}

function Print-Usage {
    Write-Host "Verum Omnis Build System v5.2.7"
    Write-Host ""
    Write-Host "Usage: .\build.ps1 -Command <command> [options]"
    Write-Host ""
    Write-Host "Commands:"
    Write-Host "  android       Build Android APK only"
    Write-Host "  web           Build web application only"
    Write-Host "  capacitor     Build with Capacitor integration"
    Write-Host "  both          Build both Android and web (default)"
    Write-Host "  clean         Clean all build artifacts"
    Write-Host "  verify        Verify build outputs"
    Write-Host "  requirements  Check build requirements"
    Write-Host "  help          Show this help message"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -Verbose      Show detailed output"
    Write-Host "  -Help         Show this help message"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\build.ps1 -Command android         # Build Android APK"
    Write-Host "  .\build.ps1 -Command web             # Build web app"
    Write-Host "  .\build.ps1 -Command both            # Build everything"
    Write-Host "  .\build.ps1 -Command clean           # Clean builds"
    Write-Host ""
}

# Main execution
function Main {
    Write-Log "Verum Omnis Build System v5.2.7"
    Write-Log "Build timestamp: $BuildTimestamp"
    Write-Log "Log file: $BuildLog"
    Write-Host ""
    
    if ($Help) {
        Print-Usage
        return
    }
    
    $success = $true
    
    switch ($Command.ToLower()) {
        "android" {
            Check-Requirements "android"
            $success = Build-Android
            if ($success) {
                Verify-Build
            }
        }
        "web" {
            Check-Requirements "web"
            $success = Build-Web
            if ($success) {
                Verify-Build
            }
        }
        "capacitor" {
            Check-Requirements "web"
            $success = Build-Capacitor
            if ($success) {
                Verify-Build
            }
        }
        "both" {
            Check-Requirements "both"
            $success = Build-Android
            if ($success) {
                $success = Build-Web
            }
            if ($success) {
                Verify-Build
            }
        }
        "clean" {
            Clean-Builds
        }
        "verify" {
            Verify-Build
        }
        "requirements" {
            Check-Requirements
        }
        "help" {
            Print-Usage
        }
        default {
            Write-Error-Custom "Unknown command: $Command"
            Print-Usage
            exit 1
        }
    }
    
    if ($success) {
        Print-Header "Build Complete"
        Write-Success "All build tasks completed successfully!"
        Write-Log "Log file saved: $BuildLog"
    }
    else {
        Write-Error-Custom "Build failed. Check $BuildLog for details"
        exit 1
    }
}

# Execute main
Main
