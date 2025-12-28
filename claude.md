# Claude Code Context: Elaro Plugin SDK

## Project Overview

This is the **Elaro Plugin SDK** - a Java library that allows external services to register as plugins with the Elaro platform. Plugins send beacon messages via Kafka until discovered and acknowledged by the Elaro Console.

## Architecture

```
┌─────────────────────┐     Kafka: elaro.plugin.announce     ┌─────────────────────┐
│   Plugin Service    │ ─────────────────────────────────────▶│   Elaro Console     │
│  (uses Plugin SDK)  │                                       │                     │
│                     │◀───────────────────────────────────── │                     │
│  Sends beacon every │     Kafka: elaro.plugin.ack           │                     │
│  30 seconds until   │                                       │                     │
│  acknowledged       │                                       │                     │
└─────────────────────┘                                       └─────────────────────┘
```

## Tech Stack

- Java 21
- Spring Boot 3.2.1
- Spring Kafka
- Lombok
- Jackson (JSON serialization)

## Project Structure

```
src/main/java/com/elaro/plugin/sdk/
├── ElaroPlugin.java                    # Main annotation to enable plugin
├── ElaroPluginAutoConfiguration.java   # Spring Boot auto-config
├── config/
│   ├── PluginProperties.java           # @ConfigurationProperties
│   └── PluginKafkaConfig.java          # Kafka producer/consumer config
├── registration/
│   ├── PluginRegistrar.java            # Beacon sender logic
│   ├── PluginManifest.java             # Plugin metadata DTO
│   ├── RegistrationStatus.java         # Enum: PENDING, ACCEPTED, REJECTED
│   └── PluginLifecycleManager.java     # Manages startup/shutdown
├── kafka/
│   ├── PluginKafkaProducer.java        # Sends to elaro.plugin.announce
│   ├── PluginKafkaListener.java        # Listens to elaro.plugin.ack
│   └── AckMessage.java                 # ACK message DTO
├── ui/
│   ├── PluginScreen.java               # Annotation for React screens
│   ├── PluginScreenRegistry.java       # Collects all @PluginScreen classes
│   └── PluginUiController.java         # Serves plugin metadata endpoints
└── model/
    ├── ScreenDefinition.java           # Screen metadata
    └── MenuDefinition.java             # Menu structure
```

## Key Concepts

### 1. Beacon Registration
- Plugin sends `PluginManifest` to `elaro.plugin.announce` topic every 30 seconds
- Continues until it receives an `AckMessage` on `elaro.plugin.ack` topic
- Status transitions: PENDING → ACCEPTED or REJECTED

### 2. Core Annotations
- `@ElaroPlugin` - Applied to main class, triggers auto-configuration
- `@PluginScreen` - Marks classes as UI screen definitions

### 3. Configuration Prefix
All properties use `elaro.plugin.*` prefix:
```yaml
elaro:
  plugin:
    id: com.example.myplugin
    name: My Plugin
    vendor: Example Corp
    kafka:
      bootstrap-servers: localhost:9092
```

## Build Commands

```bash
./mvnw clean compile    # Compile
./mvnw clean install    # Install to local Maven repo
./mvnw clean test       # Run tests
```

## Guidelines for Changes

### Adding New Features
1. Follow existing package structure
2. Use Lombok annotations (@Data, @Builder, @RequiredArgsConstructor)
3. Add to auto-configuration if new beans are needed
4. Update README.md with documentation

### Modifying DTOs
- `PluginManifest` is the main contract with Elaro Console - changes require coordination
- `AckMessage` is received from Elaro Console - must match Console's producer

### Kafka Topics
- `elaro.plugin.announce` - SDK produces, Console consumes
- `elaro.plugin.ack` - Console produces, SDK consumes

## REST Endpoints Exposed

| Endpoint | Description |
|----------|-------------|
| `GET /plugin/manifest` | Full plugin manifest |
| `GET /plugin/screens` | List of registered screens |
| `GET /plugin/health` | Plugin health check |
| `GET /plugin/info` | Basic plugin info |

## Related Projects

- **Elaro Console** (`~/Documents/GitHub/Elaro/elaro-console/`) - Receives beacons, sends ACKs
- **Elaro Gateway** (`~/Documents/GitHub/Elaro-Gateway/`) - API gateway

## Testing Locally

1. Start Kafka locally (port 9092)
2. Create a test Spring Boot app with this SDK
3. Configure `elaro.plugin.id` and other required properties
4. Watch logs for beacon sending
5. Elaro Console must be running to send ACK

## Common Issues

| Issue | Solution |
|-------|----------|
| Beacons not sending | Check `elaro.plugin.id` is configured |
| Kafka connection failed | Verify `elaro.plugin.kafka.bootstrap-servers` |
| ACK not received | Ensure Elaro Console is running and listening |
| Screens not discovered | Ensure @PluginScreen classes are in component scan path |
