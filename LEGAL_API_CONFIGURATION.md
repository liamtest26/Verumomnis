# Legal API Configuration & Security

## Environment Configuration

### API Key Management

**IMPORTANT**: Do NOT commit API keys to source code.

#### Setup via Environment Variable

```bash
# Linux/macOS
export LEGAL_API_KEY="sk_live_..."

# Windows (Command Prompt)
set LEGAL_API_KEY=sk_live_...

# Windows (PowerShell)
$env:LEGAL_API_KEY="sk_live_..."
```

#### Android Configuration

Create `local.properties` in project root:
```properties
# DO NOT COMMIT THIS FILE
legal.api.key=sk_live_...
```

Reference in code:
```kotlin
val apiKey = BuildConfig.LEGAL_API_KEY
```

#### Gradle Configuration

In `build.gradle.kts`:
```kotlin
android {
    defaultConfig {
        buildConfigField "String", "LEGAL_API_KEY", 
            "\"${System.getenv("LEGAL_API_KEY") ?: "sk_test_..."}\""
    }
}
```

---

## Google Cloud Integration (Optional)

The provided API key allows optional integration with Google Cloud services (for future features):

**Key**: `AIzaSyDNsT_R_fPR4WAZEmj6sSTQbUWxm8oBDnE`

### Restrictions

This key is **restricted to**:
- Maps API (for GPS display)
- No data storage (offline-only)
- No analytics (privacy first)
- No authentication required (public)

### Usage Example

```kotlin
// Optional: Use Google Maps for GPS visualization
val mapsApiKey = "AIzaSyDNsT_R_fPR4WAZEmj6sSTQbUWxm8oBDnE"

// Only used for:
// - Displaying evidence location on map
// - Showing jurisdiction boundaries
// - NOT for storing or transmitting evidence
```

### In AndroidManifest.xml

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyDNsT_R_fPR4WAZEmj6sSTQbUWxm8oBDnE" />
```

---

## Web API Server Configuration

### Local Development Server

Create `config/server.properties`:

```properties
# Server Configuration
server.port=8080
server.host=localhost
server.ssl.enabled=false

# Legal API
legal.api.version=1.0.0
legal.api.constitution.version=5.2.7

# Features
features.sealed.documents=true
features.html.generation=true
features.gps.routing=true
features.sessions=true

# Offline Mode
offline.only=true
cloud.disabled=true

# Rate Limiting
ratelimit.enabled=false
ratelimit.requests.per.minute=100
```

### Production Server

Create `config/server-prod.properties`:

```properties
# Server Configuration
server.port=443
server.host=api.legal.verumomnis.org
server.ssl.enabled=true
server.ssl.certificate=/etc/ssl/certs/verumomnis.pem
server.ssl.key=/etc/ssl/private/verumomnis.key

# Legal API
legal.api.version=1.0.0
legal.api.key=${env:LEGAL_API_KEY}
legal.api.constitution.version=5.2.7

# Features
features.sealed.documents=true
features.html.generation=true
features.gps.routing=true
features.sessions=true

# Offline Mode
offline.only=true
cloud.disabled=true

# Rate Limiting
ratelimit.enabled=true
ratelimit.requests.per.minute=100
ratelimit.requests.per.hour=1000

# CORS
cors.enabled=true
cors.allowed.origins=https://legal.verumomnis.org,https://app.verumomnis.org
cors.allowed.methods=GET,POST,OPTIONS
cors.max.age=3600

# Logging
logging.level=INFO
logging.file=/var/log/legal-api.log
```

---

## Vault Storage Configuration

### Local Storage (Development)

```kotlin
val vaultPath = "${getFilesDir().absolutePath}/vault"
val sessionPath = "${getFilesDir().absolutePath}/sessions"

val legalAPI = LegalAdvisoryAPI.initialize(
    vaultPath = vaultPath,
    sessionStoragePath = sessionPath,
    apkRootHash = "56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466"
)
```

### Encrypted Storage (Production)

```kotlin
// Android Security Crypto
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedFile = EncryptedFile.Builder(
    context,
    File(vaultPath, "sealed_documents"),
    masterKey,
    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
).build()
```

---

## HTTPS/TLS Configuration

### Self-Signed Certificate (Development)

```bash
# Generate self-signed certificate
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes

# Convert to PKCS12 for Android
openssl pkcs12 -export -in cert.pem -inkey key.pem -out keystore.p12
```

### Production Certificate (Let's Encrypt)

```bash
# Install Certbot
sudo apt-get install certbot

# Generate certificate
sudo certbot certonly --standalone -d api.legal.verumomnis.org

# Certificate located at
# /etc/letsencrypt/live/api.legal.verumomnis.org/
```

### Android Certificate Pinning

```kotlin
// Pin certificate for production
val certificatePinner = CertificatePinner.Builder()
    .add("api.legal.verumomnis.org", 
        "sha256/..certificate_hash..")
    .build()

val okHttpClient = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

---

## Database Configuration

### SQLite (Local Storage)

```kotlin
val database = Room.databaseBuilder(
    context,
    VaultDatabase::class.java,
    "legal_vault.db"
)
    .enableMultiInstanceInvalidation()
    .build()
```

### Encryption (Optional)

```kotlin
// SQLCipher for encrypted database
val database = Room.databaseBuilder(
    context,
    VaultDatabase::class.java,
    "legal_vault.db"
)
    .openHelperFactory(SupportSQLiteOpenHelper.Configuration.Builder(context)
        .name("legal_vault.db")
        .passphrase("encryption_passphrase".toByteArray())
        .build()
    )
    .build()
```

---

## Logging Configuration

### Development Logging

```properties
# logback.xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="DEBUG">
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```

### Production Logging

```properties
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/legal-api.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>/var/log/legal-api-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

---

## Performance Tuning

### Connection Pooling

```kotlin
val dataSource = HikariDataSource()
dataSource.maxLifetime = 1800000  // 30 minutes
dataSource.maximumPoolSize = 20
dataSource.minimumIdle = 5
```

### Caching

```kotlin
// Cache sealed summaries (read-only)
val cache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterAccess(1, TimeUnit.HOURS)
    .build()
```

### Memory Configuration

```bash
# JVM heap size
java -Xmx2g -Xms1g -jar legal-api.jar
```

---

## Monitoring & Health Checks

### Health Check Endpoint

```
GET /health

Response:
{
  "status": "healthy",
  "version": "1.0.0",
  "uptime": "23h 45m",
  "memory": {
    "used": "512MB",
    "max": "2GB"
  }
}
```

### Prometheus Metrics

```bash
# Available at /metrics
GET /metrics

# Includes:
# - Request rate
# - Response times
# - Error rates
# - JVM metrics
```

---

## Backup & Recovery

### Automated Backup

```bash
#!/bin/bash
# Backup script
VAULT_DIR="/var/lib/legal-api/vault"
BACKUP_DIR="/var/backups/legal-api"
DATE=$(date +%Y%m%d_%H%M%S)

tar -czf "${BACKUP_DIR}/vault_${DATE}.tar.gz" "${VAULT_DIR}"

# Keep last 30 days
find ${BACKUP_DIR} -name "vault_*.tar.gz" -mtime +30 -delete
```

### Disaster Recovery

```bash
# Restore from backup
tar -xzf vault_20260121_120000.tar.gz -C /var/lib/legal-api/

# Verify integrity
sha256sum vault_20260121_120000.tar.gz
```

---

## Security Checklist

- [ ] API key stored in environment variable (not in code)
- [ ] HTTPS/TLS enabled (production)
- [ ] Certificate pinning implemented (Android)
- [ ] Rate limiting enabled
- [ ] CORS restricted to trusted domains
- [ ] Logging doesn't expose sensitive data
- [ ] Database encryption enabled
- [ ] Backups encrypted
- [ ] Access logs maintained
- [ ] Regular security updates applied

---

## Troubleshooting

### API Key Not Working

```bash
# Verify API key is set
echo $LEGAL_API_KEY

# Check if included in build
grep -r "LEGAL_API_KEY" build/
```

### SSL Certificate Issues

```bash
# Verify certificate
openssl s_client -connect api.legal.verumomnis.org:443

# Check certificate expiration
openssl x509 -in cert.pem -noout -dates
```

### Vault Storage Issues

```bash
# Check vault directory permissions
ls -la /var/lib/legal-api/vault

# Fix permissions
chmod 700 /var/lib/legal-api/vault
chown legal-api:legal-api /var/lib/legal-api/vault
```

---

## Support

For configuration support:
- Check environment variables: `env | grep LEGAL`
- Check server logs: `tail -f /var/log/legal-api.log`
- Run health check: `curl https://api.local/health`
- Verify certificates: `openssl verify cert.pem`

---

**Remember**: Never commit API keys or sensitive credentials to source code. Always use environment variables or secure configuration management.
