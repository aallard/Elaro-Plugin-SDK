# BF-064b: SDK Logging Audit

## ElaroLogger

**Location:** `src/main/java/com/elaro/plugin/sdk/logging/ElaroLogger.java`

### How It Works
- Dual logging: Every log call goes to **SLF4J (local)** AND **Kafka (centralized)**
- Kafka publishing is conditional based on `minKafkaLevel` (default: DEBUG)
- Logs below minKafkaLevel only go to local SLF4J
- Kafka failures are caught and logged locally - they don't break the application

### Key Methods
| Method | Description |
|--------|-------------|
| `trace(message)` / `trace(message, context)` | Log at TRACE level |
| `debug(message)` / `debug(message, context)` | Log at DEBUG level |
| `info(message)` / `info(message, context)` | Log at INFO level |
| `warn(message)` / `warn(message, t)` / `warn(message, context)` | Log at WARN level |
| `error(message)` / `error(message, t)` / `error(message, t, context)` | Log at ERROR level |
| `fatal(message)` / `fatal(message, t)` / `fatal(message, t, context)` | Log at FATAL level |
| `setMinKafkaLevel(level)` | Set minimum level for Kafka publishing |
| `static context(keyValues...)` | Helper to build context maps |

### Core Log Flow
```java
private void log(LogLevel level, String message, Throwable t, Map<String, String> context) {
    // 1. Always log to SLF4J locally
    logToSlf4j(level, message, t);

    // 2. Only publish to Kafka if above min level
    if (level.isAtLeast(minKafkaLevel) && kafkaTemplate != null) {
        publishToKafka(level, message, t, context);
    }
}
```

---

## LogMessage

**Location:** `src/main/java/com/elaro/plugin/sdk/logging/LogMessage.java`

### Structure (Java Record)
| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Unique log entry ID (auto-generated) |
| `serviceId` | `String` | Plugin identifier (e.g., "com.example.myplugin") |
| `serviceName` | `String` | Human-readable plugin name |
| `level` | `LogLevel` | Log level enum |
| `logger` | `String` | Logger class name (e.g., "com.example.MyClass") |
| `message` | `String` | Log message text |
| `stackTrace` | `String` | Full stack trace (for exceptions) |
| `context` | `Map<String, String>` | Custom key-value metadata |
| `threadName` | `String` | Thread name (auto-captured) |
| `timestamp` | `Instant` | Log timestamp (auto-captured) |
| `tenantId` | `UUID` | Tenant ID for multi-tenant deployments |

### Builder Pattern
```java
LogMessage logMessage = LogMessage.builder()
    .serviceId(serviceId)
    .serviceName(serviceName)
    .level(level)
    .logger(loggerName)
    .message(message)
    .stackTrace(stackTrace)
    .context(context)
    .tenantId(tenantId)
    .build();
```

---

## LogLevel

**Location:** `src/main/java/com/elaro/plugin/sdk/logging/LogLevel.java`

### Levels (ascending severity)
| Level | Severity Value |
|-------|---------------|
| `TRACE` | 0 |
| `DEBUG` | 10 |
| `INFO` | 20 |
| `WARN` | 30 |
| `ERROR` | 40 |
| `FATAL` | 50 |

### Key Method
```java
public boolean isAtLeast(LogLevel other) {
    return this.severity >= other.severity;
}
```

---

## Log Delivery

| Aspect | Value |
|--------|-------|
| **Sends via** | Kafka (via `KafkaTemplate`) |
| **Topic** | `elaro.logs` (hardcoded in ElaroLogger) |
| **Producer class** | Uses `KafkaTemplate<String, String>` directly |
| **Message key** | `serviceId` |
| **Message value** | JSON-serialized `LogMessage` |
| **Serializer** | Jackson `ObjectMapper` with `JavaTimeModule` |

### Kafka Send Implementation
```java
private void publishToKafka(LogLevel level, String message, Throwable t, Map<String, String> context) {
    try {
        // Build LogMessage with all metadata
        LogMessage logMessage = LogMessage.builder()
            .serviceId(serviceId)
            .serviceName(serviceName)
            .level(level)
            .logger(loggerName)
            .message(message)
            .stackTrace(stackTrace)  // Extracted from Throwable
            .context(context)
            .tenantId(tenantId)
            .build();

        String json = objectMapper.writeValueAsString(logMessage);
        kafkaTemplate.send("elaro.logs", serviceId, json);
    } catch (Exception e) {
        // Silent fail - don't break app due to logging issues
        slf4jLogger.warn("Failed to publish log to Kafka: {}", e.getMessage());
    }
}
```

---

## Configuration

### Required Properties
```yaml
elaro:
  plugin:
    id: com.example.myplugin          # Required - used as Kafka message key
    name: My Plugin                    # Required - included in log metadata
    kafka:
      bootstrap-servers: localhost:9092  # Required for Kafka connectivity
```

### Optional Properties
```yaml
elaro:
  plugin:
    tenant-id: 00000000-0000-0000-0000-000000000001  # Defaults if not set
```

### Related Kafka Configuration
**Location:** `src/main/java/com/elaro/plugin/sdk/config/PluginKafkaConfig.java`

```java
// Producer config used by logging
configProps.put(ProducerConfig.ACKS_CONFIG, "all");
configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
```

---

## Auto-Configuration

**Location:** `src/main/java/com/elaro/plugin/sdk/logging/ElaroLoggingAutoConfiguration.java`

### Activation Conditions
- `@ConditionalOnClass(KafkaTemplate.class)` - Kafka must be on classpath
- `@ConditionalOnBean(KafkaTemplate.class)` - KafkaTemplate bean must exist

### Bean Registration
```java
@Bean
@ConditionalOnBean(KafkaTemplate.class)
@ConditionalOnMissingBean
public ElaroLoggerFactory elaroLoggerFactory(
        KafkaTemplate<String, String> pluginKafkaTemplate,
        ObjectMapper pluginObjectMapper,
        PluginProperties properties) {

    ElaroLoggerFactory factory = new ElaroLoggerFactory(pluginKafkaTemplate, pluginObjectMapper);
    factory.configure(properties.getId(), properties.getName(), tenantId);
    return factory;
}
```

---

## Usage Pattern

### Injection via Constructor
```java
@Service
public class MyService {
    private final ElaroLogger log;

    public MyService(ElaroLoggerFactory loggerFactory) {
        this.log = loggerFactory.getLogger(MyService.class);
    }

    public void doWork() {
        log.info("Processing started");
        log.info("Order processed", ElaroLogger.context("orderId", "123", "duration", "150ms"));
    }

    public void handleError(Exception e) {
        log.error("Processing failed", e, ElaroLogger.context("step", "validation"));
    }
}
```

### Context Helper
```java
// Build context map inline
log.info("Event occurred", ElaroLogger.context(
    "userId", "12345",
    "action", "login",
    "ip", "192.168.1.1"
));
```

---

## All Logging-Related Classes

### `/logging/` Directory
| File | Purpose |
|------|---------|
| `ElaroLogger.java` | Main logger class - dual SLF4J + Kafka logging |
| `ElaroLoggerFactory.java` | Factory for creating/caching logger instances |
| `ElaroLoggingAutoConfiguration.java` | Spring Boot auto-configuration |
| `LogLevel.java` | Enum: TRACE, DEBUG, INFO, WARN, ERROR, FATAL |
| `LogMessage.java` | DTO record sent to Kafka |

### `/kafka/` Directory (Related)
| File | Purpose |
|------|---------|
| `PluginKafkaProducer.java` | Beacon announcements (NOT used for logging) |
| `PluginKafkaListener.java` | ACK message consumer |
| `AckMessage.java` | ACK message DTO |

---

## Key Differences from PluginKafkaProducer

| Aspect | ElaroLogger | PluginKafkaProducer |
|--------|-------------|---------------------|
| **Topic** | `elaro.logs` (hardcoded) | `elaro.plugin.announce` (from config) |
| **Message Key** | `serviceId` | `pluginId` |
| **Payload** | `LogMessage` JSON | `PluginManifest` JSON |
| **Send Method** | Fire-and-forget | With completion callback |
| **Error Handling** | Logs warning, continues | Logs error |

---

## Forge Integration Notes

For Forge to use the same logging mechanism:

1. **Reuse LogMessage format** - Same JSON structure for consistency
2. **Publish to `elaro.logs` topic** - Same topic for Console to consume
3. **Include required fields:**
   - `serviceId` - Unique identifier for Forge
   - `serviceName` - "Elaro Forge"
   - `tenantId` - From authenticated user context
   - `timestamp` - ISO-8601 Instant
4. **Use serviceId as Kafka key** - For partitioning consistency
5. **Can skip SLF4J dual-logging** - Forge may use standard logging separately
