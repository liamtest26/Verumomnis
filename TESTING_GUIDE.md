# Verum Omnis - Testing & Quality Assurance Guide

**Version**: 5.2.7  
**Status**: Production Ready  
**Last Updated**: January 21, 2026

Complete testing procedures to ensure production readiness before deployment.

---

## 📋 Pre-Deployment Testing Checklist

### ✓ Build Quality
- [ ] Android APK builds without errors
- [ ] Android APK builds without warnings
- [ ] Web build completes successfully
- [ ] TypeScript type checking passes
- [ ] ESLint validation passes
- [ ] No deprecated API usage
- [ ] ProGuard obfuscation configured
- [ ] Build logs are clean

### ✓ Security Verification
- [ ] APK hash matches expected value: `56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466`
- [ ] No hardcoded credentials in code
- [ ] No API keys in source files
- [ ] Environment variables properly used
- [ ] npm security audit passes
- [ ] Dependencies are up-to-date
- [ ] No vulnerable dependencies

### ✓ Functionality Testing
- [ ] App launches without crashes
- [ ] All activities load correctly
- [ ] Camera functionality works
- [ ] Document processing works
- [ ] PDF generation works
- [ ] Web interface responsive
- [ ] Navigation works on all platforms
- [ ] Settings persist correctly

### ✓ Performance Testing
- [ ] App startup time < 3 seconds
- [ ] Document processing < 30 seconds
- [ ] PDF generation < 10 seconds
- [ ] Web page load time < 2 seconds
- [ ] Memory usage < 200 MB
- [ ] CPU usage normal during idle
- [ ] No memory leaks during long use

### ✓ Compatibility Testing
- [ ] Works on Android 7.0+ (SDK 24+)
- [ ] Works on Android 14 (SDK 34)
- [ ] Works on minimum RAM (2GB)
- [ ] Works on 4K displays
- [ ] Works on small devices (5-inch screens)
- [ ] Works on tablets (7-10 inch)
- [ ] Works in offline mode
- [ ] Works with limited storage

### ✓ Offline Functionality
- [ ] All features work without internet
- [ ] Documents process offline
- [ ] PDFs generate offline
- [ ] No cloud access attempted
- [ ] No analytics collection
- [ ] No crash reporting calls
- [ ] Storage works correctly

### ✓ Data Privacy
- [ ] No data leaves device
- [ ] No cloud storage used
- [ ] No user tracking
- [ ] No analytics
- [ ] No ads
- [ ] No third-party SDKs (except approved)
- [ ] Encrypted local storage

---

## 🧪 Automated Testing

### Unit Tests

```bash
cd web
npm run test
```

**Test Coverage**:
- Hash calculations
- PDF generation
- Document parsing
- Constitutional compliance checks
- Jurisdiction routing

**Expected Results**:
- All tests pass
- Coverage > 80%
- No failed assertions

### Type Checking

```bash
cd web
npm run type-check
```

**Expected Results**:
- No TypeScript errors
- No type mismatches
- All imports resolved

### Linting

```bash
cd web
npm run lint
```

**Expected Results**:
- No ESLint errors
- No style violations
- Code follows standards

### Security Audit

```bash
npm audit
```

**Expected Results**:
- 0 critical vulnerabilities
- 0 high vulnerabilities
- Moderate vulnerabilities documented

---

## 🔍 Manual Testing Procedures

### Android APK Testing

#### 1. Installation
```bash
# Connect device/emulator
adb devices

# Install APK
adb install -r app/build/outputs/apk/release/app-release.apk

# Verify installation
adb shell pm list packages | grep verumomnis
```

Expected: APK installs successfully

#### 2. Launch
```bash
# Launch app
adb shell am start -n org.verumomnis.forensic/.MainActivity

# Monitor logs
adb logcat -s "VerumOmnis" | grep -i "launch\|error\|warning"
```

Expected: App launches without crashes

#### 3. Verify APK Integrity
```bash
# Check APK hash
sha256sum app/build/outputs/apk/release/app-release.apk

# Expected output should start with:
# 56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466
```

#### 4. Test Core Features

**Camera Testing**:
- Open scanner
- Capture test image
- Verify image saves
- Check image quality

**Document Processing**:
- Upload multi-page PDF
- Process through forensic engine
- Verify B1-B9 analysis
- Check metadata extraction

**PDF Generation**:
- Generate sealed report
- Verify PDF structure
- Check watermark presence
- Verify hash is present
- Test QR code

**Session Management**:
- Create new session
- Add documents
- Save session
- Reload session
- Verify data persists

#### 5. Performance Testing

```bash
# Monitor performance
adb shell dumpsys meminfo org.verumomnis.forensic

# Expected:
# - TOTAL memory < 300 MB
# - Native heap < 50 MB
# - Java heap < 100 MB

# Monitor CPU
adb shell top -n 1 | grep verumomnis

# Expected:
# - CPU% < 20% at idle
# - CPU% < 50% during processing
```

#### 6. Network Testing

```bash
# Verify no network access
adb shell "pm grant org.verumomnis.forensic android.permission.INTERNET" || true
adb shell am start -n org.verumomnis.forensic/.VerificationActivity

# Monitor network
adb shell dumpsys connectivity | grep -A5 "INTERNET"

# Expected: No active connections
```

### Web Application Testing

#### 1. Browser Compatibility
Test on:
- Chrome 120+ (Linux, Windows, macOS)
- Firefox 121+
- Safari 17+ (macOS/iOS)
- Edge 120+
- Mobile browsers (Chrome, Safari)

#### 2. Responsive Design
```bash
npm run serve
```

Test layouts:
- [ ] 320px (small phone)
- [ ] 768px (tablet)
- [ ] 1024px (laptop)
- [ ] 1920px (desktop)
- [ ] 4K (3840px)

#### 3. Performance Metrics

```bash
# Analyze bundle
npm run build:prod

# Expected sizes:
# - JavaScript: < 2 MB
# - CSS: < 500 KB
# - Images: < 1 MB
# - Total: < 4 MB

# Test load time
open http://localhost:3000
# Should load in < 2 seconds on 4G
# Should load in < 1 second on WiFi
```

#### 4. Functionality Testing

**Document Upload**:
- [ ] Upload PDF
- [ ] Upload images
- [ ] Upload text files
- [ ] Upload multiple files
- [ ] Verify file sizes
- [ ] Check file type validation

**Document Processing**:
- [ ] Process single document
- [ ] Process multiple documents
- [ ] Verify results display
- [ ] Check hash calculations
- [ ] Verify watermarks

**Report Generation**:
- [ ] Generate sealed report
- [ ] Download PDF
- [ ] Verify PDF integrity
- [ ] Check QR code
- [ ] Verify digital signature

**Navigation**:
- [ ] Home page
- [ ] Upload page
- [ ] Processing page
- [ ] Results page
- [ ] Settings page
- [ ] About page

#### 5. Security Testing

```bash
# Check for CSP violations
open http://localhost:3000
Press F12 (DevTools)
Console tab
# Expected: No CSP violations

# Check for CORS issues
# Network tab
# Expected: No CORS errors

# Check for mixed content
# Expected: All HTTPS on production
# Expected: No warnings about mixed content

# Verify no sensitive data in localStorage
localStorage
# Expected: Only non-sensitive session IDs
```

#### 6. Accessibility Testing

```bash
# Test keyboard navigation
Tab through all elements
# Expected: All interactive elements accessible

# Test screen reader
Screen Reader enabled
# Expected: All content readable

# Test color contrast
# Use accessibility checker
# Expected: WCAG AA compliance
```

---

## 🔐 Security Testing

### Static Code Analysis

```bash
# Android
./gradlew lint

# Web
npm run lint
eslint src --ext .ts,.tsx
```

### Dependency Scanning

```bash
# Android dependencies
./gradlew dependencyUpdates

# Web dependencies
npm audit
npm outdated
```

### OWASP Top 10 Checks

#### 1. Injection
- [ ] No SQL injection possible (offline-only)
- [ ] No command injection in PDF generation
- [ ] Input validation on file uploads

#### 2. Broken Authentication
- [ ] No authentication (offline-first)
- [ ] Device-level security used
- [ ] No credentials stored in app

#### 3. Sensitive Data Exposure
- [ ] Local storage encrypted
- [ ] No data sent to cloud
- [ ] PDF contains hash only
- [ ] No PII in logs

#### 4. XML External Entities (XXE)
- [ ] PDF parsing secure
- [ ] XML parsing configured safely
- [ ] Entity expansion disabled

#### 5. Broken Access Control
- [ ] No backend access control needed
- [ ] Device controls file access
- [ ] Settings protected locally

#### 6. Security Misconfiguration
- [ ] ProGuard properly configured
- [ ] Debug mode disabled in release
- [ ] No debug logging in production

#### 7. Cross-Site Scripting (XSS)
- [ ] All inputs sanitized
- [ ] Template escaping enabled
- [ ] No innerHTML usage

#### 8. Insecure Deserialization
- [ ] No untrusted deserialization
- [ ] JSON parsing safe
- [ ] Object instantiation controlled

#### 9. Using Components with Known Vulnerabilities
- [ ] Dependencies updated
- [ ] No known CVEs
- [ ] Regular audits scheduled

#### 10. Insufficient Logging & Monitoring
- [ ] App errors logged locally
- [ ] No cloud logging
- [ ] Logs don't expose sensitive data

---

## 📊 Performance Testing

### Android Performance

```bash
# Profile APK size
aapt dump badging app/build/outputs/apk/release/app-release.apk | grep package

# Expected:
# - APK size: 45-55 MB
# - Dex count: 1
# - No unnecess dependencies

# Profiling
adb shell dumpsys cpuinfo
adb shell dumpsys meminfo
adb shell dumpsys battery
```

### Web Performance

```bash
# Bundle analysis
npm run build:prod
npm install -g webpack-bundle-analyzer
npx webpack-bundle-analyzer web/dist/assets/*.js
```

**Expected**:
- JavaScript: < 2 MB
- CSS: < 500 KB
- Total: < 4 MB

### Load Testing

**Web Server**:
```bash
# Install Apache Bench
sudo apt-get install apache2-utils

# Test performance
ab -n 1000 -c 10 http://localhost:3000/

# Expected:
# - Requests per second > 100
# - Mean response time < 100ms
# - Failure rate: 0%
```

---

## 🎯 Regression Testing

### Features to Test After Updates

1. **Document Processing**
   - PDF processing unchanged
   - Hash calculations correct
   - Metadata extraction working

2. **UI/UX**
   - No visual regressions
   - Navigation unchanged
   - Responsive design intact

3. **Cryptography**
   - SHA-512 calculations correct
   - HMAC generation working
   - QR code generation valid

4. **Performance**
   - Load times maintained
   - Memory usage stable
   - CPU usage optimal

---

## 🚀 Pre-Production Checklist

### Code Quality
```bash
# Full test suite
./build.sh verify

# Check requirements
./build.sh requirements

# Verify all builds
./build.sh both
./build.sh verify
```

### Documentation
- [ ] README.md updated
- [ ] BUILD_GUIDE.md current
- [ ] LEGAL_API_DOCUMENTATION.md complete
- [ ] DEPLOYMENT_CHECKLIST.md reviewed
- [ ] CHANGELOG.md updated

### Release Preparation
- [ ] Version bumped in:
  - app/build.gradle.kts
  - web/package.json
  - README.md
- [ ] Changelog entries added
- [ ] Test plan completed
- [ ] Security review passed
- [ ] Performance baselines met

### Deployment
- [ ] Staging environment tested
- [ ] Rollback plan ready
- [ ] Monitoring configured
- [ ] Support docs prepared
- [ ] Communications ready

---

## 📈 Continuous Monitoring

### Post-Deployment Checks

```bash
# Monitor crash reports
# (if configured)

# Monitor performance metrics
# Check app analytics (if enabled)

# Monitor user feedback
# GitHub issues and discussions

# Monitor security
# npm audit regularly
# Check for CVEs
```

### Weekly Maintenance
```bash
# Update dependencies
npm outdated
npm update

# Run security audit
npm audit

# Check build status
# Review GitHub Actions logs
```

### Monthly Review
```bash
# Full dependency audit
npm audit --production

# Performance baseline
npm run build:prod && du -sh web/dist

# Security review
# Check for new CVEs
# Update ProGuard rules if needed
```

---

## 📞 Testing Support

For test failures:
1. Review logs in `build_*.log`
2. Check [PRODUCTION_BUILD_GUIDE.md](PRODUCTION_BUILD_GUIDE.md#troubleshooting)
3. Run diagnostics: `./build.sh requirements`
4. Open GitHub issue with details

---

**Last Updated**: January 21, 2026  
**Version**: 5.2.7  
**Status**: ✓ Production Ready
