# Legal Advisory REST API & Web Document Generation

## Overview

The **Legal Advisory Web API** provides REST endpoints for accessing sealed forensic advisories through web browsers and mobile apps. All outputs comply with **Verum Omnis Constitution v5.2.7** and include:

- ✅ Constitutional compliance verification
- ✅ HTML sealed documents (web-native)
- ✅ GPS-based jurisdiction routing
- ✅ Sealed advisory sessions (Q&A)
- ✅ Independent verification possible

---

## Architecture

```
┌─────────────────────────────────────────┐
│   Web Browser / Mobile App              │
│   (Android, iOS, Desktop)               │
└─────────────────────────────────────────┘
           ↓
    HTTP(S) REST API
           ↓
┌─────────────────────────────────────────┐
│  Legal Advisory Web API (LAN/Local)    │
│  - JSON request/response                │
│  - Constitutional compliance checks     │
│  - HTML document generation             │
│  - No cloud processing                  │
└─────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────┐
│  Legal Advisory API Core                │
│  (Same as Android API)                  │
└─────────────────────────────────────────┘
```

---

## API Endpoints

### 1. Generate Advisory

**POST `/advisory/generate`**

Generate sealed forensic advisory from sealed summary.

**Request**:
```json
{
  "sealed_summary": {
    "reportHash": "...",
    "generatedAt": "2026-01-21T...",
    "gpsCoordinates": [
      {"latitude": 24.4539, "longitude": 54.3773}
    ],
    "findings": [...]
  },
  "jurisdiction_override": "UAE"
}
```

**Response** (200 OK):
```json
{
  "status": "success",
  "advisory": {
    "reportHash": "...",
    "jurisdiction": "UAE",
    "allApplicableJurisdictions": ["UAE"],
    "recommendations": [...],
    "nextSteps": [...],
    "riskFactors": [...]
  },
  "compliance": "PASSED",
  "constitutional_version": "5.2.7",
  "timestamp": "2026-01-21T..."
}
```

**Compliance Checks**:
- ✓ Immutable Principles verified
- ✓ Nine-Brain Architecture confirmed
- ✓ Triple Verification Doctrine passed
- ✓ Advisory language compliance (probabilistic, not accusatory)

---

### 2. Generate HTML Document

**POST `/document/generate-html`**

Generate sealed HTML advisory document (web-friendly).

**Request**:
```json
{
  "advisory": { ... },
  "sealed_summary": { ... },
  "title": "Greensky Ornamentals Advisory"
}
```

**Response** (200 OK):
```json
{
  "status": "success",
  "html": "<!DOCTYPE html>...",
  "document_hash": "a3f4e2c1...",
  "vault_record_id": "vault-2026-01-21-001",
  "instructions": [
    "1. Save this HTML to file",
    "2. Open in browser to view",
    "3. Print to PDF for sealed copy",
    "4. Document hash: a3f4e2c1..."
  ],
  "timestamp": "2026-01-21T..."
}
```

**HTML Features**:
- ✅ Responsive design (mobile + desktop)
- ✅ Print-friendly (CSS print styles)
- ✅ Watermarked "SEALED" background
- ✅ Includes jurisdictional guidance
- ✅ Constitutional compliance report embedded
- ✅ Immutable (cannot be edited in browser)

---

### 3. Infer Jurisdiction from GPS

**GET `/jurisdiction/infer-from-gps`**

Determine applicable jurisdiction(s) from GPS coordinates.

**Request**:
```json
{
  "coordinates": [
    {"latitude": 24.4539, "longitude": 54.3773},
    {"latitude": -33.9249, "longitude": 18.4241}
  ]
}
```

**Response** (200 OK):
```json
{
  "status": "success",
  "primary_jurisdiction": "UAE",
  "all_jurisdictions": ["UAE", "ZA"],
  "cross_border": true,
  "gps_processed": 2,
  "timestamp": "2026-01-21T..."
}
```

---

### 4. Start Advisory Session

**POST `/session/start`**

Start sealed advisory Q&A session.

**Request**:
```json
{
  "report_hash": "...",
  "jurisdiction": "UAE",
  "case_type": "shareholder_dispute",
  "user_role": "citizen"
}
```

**Response** (201 Created):
```json
{
  "status": "success",
  "session_id": "session-2026-01-21-a1b2c3",
  "created_at": "2026-01-21T12:34:56Z",
  "expires_in_hours": 24,
  "timestamp": "2026-01-21T..."
}
```

---

### 5. Ask Question in Session

**POST `/session/ask`**

Ask follow-up question in sealed advisory session.

**Request**:
```json
{
  "session_id": "session-2026-01-21-a1b2c3",
  "question": "What are the specific next steps for UAE jurisdiction?"
}
```

**Response** (200 OK):
```json
{
  "status": "success",
  "question_hash": "b4f6a1d9...",
  "response_hash": "c5g7b2e0...",
  "transcript_hash": "d6h8c3f1...",
  "timestamp": "2026-01-21T..."
}
```

**Validation**:
- ❌ Rejects: "raw", "upload", "document" keywords
- ✅ Allows: Advisory and clarification questions
- ✅ All exchanges sealed in vault

---

### 6. Close Session

**POST `/session/close`**

Close advisory session and seal transcript.

**Request**:
```json
{
  "session_id": "session-2026-01-21-a1b2c3"
}
```

**Response** (200 OK):
```json
{
  "status": "success",
  "transcript_hash": "e7i9d4g2...",
  "closed_at": "2026-01-21T12:45:00Z",
  "message": "Session sealed and stored in vault",
  "timestamp": "2026-01-21T..."
}
```

---

### 7. Verify Compliance

**GET `/compliance/verify`**

Verify constitutional compliance of sealed summary.

**Request**:
```json
{
  "sealed_summary": { ... }
}
```

**Response** (200 OK):
```json
{
  "status": "success",
  "compliant": true,
  "constitution_version": "5.2.7",
  "checks": [
    "✓ Immutable Principles",
    "✓ Nine-Brain Architecture",
    "✓ Triple Verification Doctrine",
    "✓ Operational Constraints",
    "✓ User Data Sovereignty"
  ],
  "timestamp": "2026-01-21T..."
}
```

---

### 8. Health Check

**GET `/health`**

Health check endpoint.

**Response** (200 OK):
```json
{
  "status": "healthy",
  "version": "1.0.0",
  "constitutional_framework": "5.2.7",
  "timestamp": "2026-01-21T12:00:00Z",
  "features": [
    "sealed_advisory_generation",
    "html_document_generation",
    "gps_jurisdiction_routing",
    "sealed_sessions",
    "constitutional_compliance",
    "offline_only"
  ]
}
```

---

## HTML Document Features

Generated HTML documents include:

### Structure
1. **Watermark**: "SEALED" background
2. **Header**: Document title, generation date, report reference
3. **Disclaimer Banner**: Critical legal disclaimers
4. **Jurisdiction Section**: GPS-determined jurisdiction(s)
5. **Key Findings**: Risk factors summary
6. **Recommendations**: Advisory guidance
7. **Next Steps**: Numbered action items
8. **Cross-Border Analysis**: Multi-jurisdiction implications
9. **Constitutional Compliance Report**: Audit trail
10. **Footer**: Chain of custody, vault reference

### Responsive Design
- ✅ Mobile-friendly (works on phones)
- ✅ Tablet-optimized (iPad layout)
- ✅ Desktop-full (large screens)
- ✅ Print-to-PDF ready

### Features
- ✅ Copy-to-clipboard buttons
- ✅ Print shortcuts (Ctrl+P / Cmd+P)
- ✅ Read-only (no editing)
- ✅ Self-contained (all CSS/JS embedded)
- ✅ No external dependencies

---

## Constitutional Compliance Validation

All API operations include automatic constitutional compliance checks:

### Immutable Principles
✓ **Truth Precedes Authority**: Evidence-based findings verified  
✓ **Evidence Precedes Narrative**: Evidence logged before advisory  
✓ **Guardianship Precedes Power**: Chain of custody confirmed  

### Nine-Brain Architecture
✓ B1: Event Chronology  
✓ B2: Contradiction Detection  
✓ B3-B9: Complete forensic analysis  

### Triple Verification Doctrine
✓ Phase 1: Evidence (SHA-512 hashed)  
✓ Phase 2: Cognitive (B1-B9 executed)  
✓ Phase 3: Contradiction Clearance (analyzed)  

### Operational Constraints
✓ Offline-only (no cloud inference)  
✓ Independent verification (report hash verifiable)  
✓ User data sovereignty (actor IDs hashed)  

---

## Security & Authentication

### API Key (Optional)
If API key required:
```
Authorization: Bearer sk-...
```

Configure via environment variable:
```bash
export LEGAL_API_KEY=sk_...
```

### HTTPS Only
- ❌ HTTP not allowed
- ✅ HTTPS required for production
- ✅ TLS 1.2+ required

### CORS Policy
Default (for local development):
```
Access-Control-Allow-Origin: http://localhost:*
Access-Control-Allow-Methods: GET, POST, OPTIONS
```

Production (restricted):
```
Access-Control-Allow-Origin: https://trusted.domain
```

### Rate Limiting (Optional)
- 100 requests per minute per IP
- 1000 requests per hour per user
- Session limit: 24 hours per session

---

## Web Document HTML Example

Generated HTML structure:

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Sealed Advisory</title>
  <style>
    /* Responsive CSS embedded */
  </style>
</head>
<body>
  <div class="watermark">SEALED FROM: a1b2c3d4...</div>
  
  <header class="document-header">
    <h1>🔒 SEALED FORENSIC ADVISORY</h1>
    <p class="subtitle">Generated from sealed, immutable forensic findings</p>
  </header>

  <div class="banner critical">
    <h3>⚠️ DISCLAIMER</h3>
    <ul>
      <li>This is advisory guidance only, not legal representation</li>
      <!-- ... -->
    </ul>
  </div>

  <main class="content">
    <section>
      <h2>⚖️ Jurisdiction Analysis</h2>
      <!-- ... -->
    </section>

    <section>
      <h2>📊 Key Findings</h2>
      <!-- ... -->
    </section>

    <!-- More sections -->
  </main>

  <footer>
    <div class="footer-section">
      <strong>Document Information:</strong><br>
      Report Hash: ...<br>
      Vault Record: ...
    </div>
  </footer>

  <script>
    /* Embedded JavaScript for interactivity */
  </script>
</body>
</html>
```

---

## Usage Examples

### Example 1: Generate Advisory via Web API

```bash
curl -X POST http://localhost:8080/advisory/generate \
  -H "Content-Type: application/json" \
  -d @request.json
```

### Example 2: Generate HTML Document

```bash
curl -X POST http://localhost:8080/document/generate-html \
  -H "Content-Type: application/json" \
  -d @advisory.json \
  > advisory.html

# Open in browser
open advisory.html
```

### Example 3: Start Advisory Session

```bash
curl -X POST http://localhost:8080/session/start \
  -H "Content-Type: application/json" \
  -d '{
    "report_hash": "...",
    "jurisdiction": "UAE"
  }'

# Get session ID from response
# Use in subsequent /session/ask requests
```

---

## Integration with Android

### From Android App to Web:

```kotlin
// 1. Generate sealed summary (Android)
val summary = forensicEngine.generateSealedForensicSummary(report, coords)

// 2. Send to web API
val response = webClient.post("/advisory/generate", summary)

// 3. Receive and display advisory
val advisory = response.advisory

// 4. Generate HTML document
val html = webDocGenerator.generateHTMLDocument(advisory)

// 5. Display in WebView
webView.loadData(html, "text/html", "UTF-8")
```

---

## Deployment

### Local Development
```bash
# Run on localhost
./gradlew run
# API available at: http://localhost:8080

# Health check
curl http://localhost:8080/health
```

### Production Deployment
1. Use HTTPS (TLS certificate)
2. Set API key via environment: `LEGAL_API_KEY`
3. Configure CORS for trusted domains
4. Enable rate limiting
5. Use reverse proxy (nginx, Apache)

---

## Error Handling

### Response Codes

| Code | Meaning |
|---|---|
| 200 | Success |
| 201 | Created (session started) |
| 400 | Bad request (invalid input) |
| 401 | Unauthorized (API key invalid) |
| 403 | Forbidden (compliance check failed) |
| 404 | Not found (session expired) |
| 500 | Internal error |

### Error Response Format

```json
{
  "status": "error",
  "error": "Constitutional compliance failed: ...",
  "timestamp": "2026-01-21T..."
}
```

---

## Performance

### Response Times (Expected)

| Operation | Time |
|---|---|
| Generate Advisory | < 500ms |
| Generate HTML Document | < 200ms |
| Infer Jurisdiction | < 50ms |
| Start Session | < 100ms |
| Ask Question | < 150ms |
| Close Session | < 100ms |
| Health Check | < 10ms |

---

## Testing

### Unit Tests
```kotlin
fun testGenerateAdvisory()
fun testHTMLGeneration()
fun testJurisdictionInference()
fun testSessionManagement()
fun testConstitutionalCompliance()
```

### Integration Tests
```kotlin
fun testEndToEndAdvisoryFlow()
fun testCrossBrowserCompatibility()
fun testMobileResponsiveness()
fun testHTMLPrintToPDF()
```

---

## Support

- **Documentation**: [LEGAL_API_DOCUMENTATION.md](LEGAL_API_DOCUMENTATION.md)
- **Implementation**: [LEGAL_API_IMPLEMENTATION.md](LEGAL_API_IMPLEMENTATION.md)
- **Architecture**: [this file]
- **Source Code**: `legal/web/`, `legal/documents/`, `legal/compliance/`

---

## License

**Verum Omnis Forensic Engine v5.2.7 - Legal Advisory Web API**
- Released freely to all citizens globally
- No cloud dependencies
- Open source (GPL-3.0)
- **For justice, for truth, for all citizens.**
