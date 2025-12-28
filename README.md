# Elaro Plugin SDK

SDK for building plugins for the Elaro.ai platform. Plugins can register themselves with the Elaro platform and expose custom screens, menus, and functionality.

## How It Works

1. Add the SDK to your Spring Boot application
2. Annotate your main class with `@ElaroPlugin`
3. Configure your plugin properties
4. The SDK automatically sends beacon messages to Elaro until acknowledged
5. Once acknowledged, your plugin is registered and its screens appear in Elaro

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

## Quick Start

### 1. Add the Dependency

```xml
<dependency>
    <groupId>com.elaro</groupId>
    <artifactId>elaro-plugin-sdk</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Annotate Your Application

```java
@SpringBootApplication
@ElaroPlugin(
    id = "com.acme.billing",
    name = "Billing Service",
    vendor = "Acme Corp",
    description = "Invoice and billing management"
)
public class BillingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}
```

### 3. Configure Your Plugin

```yaml
# application.yml
elaro:
  plugin:
    id: com.acme.billing
    name: Billing Service
    vendor: Acme Corp
    vendor-email: support@acme.com
    description: Invoice and billing management
    version: 1.0.0
    tenant-id: acme-corp
    environment: dev
    beacon-interval-seconds: 30
    kafka:
      bootstrap-servers: localhost:9092
      announce-topic: elaro.plugin.announce
      ack-topic: elaro.plugin.ack
```

### 4. Define Screens (Optional)

```java
@PluginScreen(
    path = "/invoices",
    title = "Invoices",
    icon = "FileText",
    order = 10
)
public class InvoicesScreen {
    // Screen configuration
}

@PluginScreen(
    path = "/payments",
    title = "Payments",
    icon = "CreditCard",
    order = 20,
    permissions = {"billing:payments:read"}
)
public class PaymentsScreen {
    // Screen configuration
}
```

## Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `elaro.plugin.id` | Unique plugin identifier (required) | - |
| `elaro.plugin.name` | Display name (required) | - |
| `elaro.plugin.vendor` | Vendor/company name | - |
| `elaro.plugin.vendor-email` | Vendor contact email | - |
| `elaro.plugin.description` | Plugin description | - |
| `elaro.plugin.version` | Plugin version | `1.0.0` |
| `elaro.plugin.icon-url` | URL to plugin icon | - |
| `elaro.plugin.tenant-id` | Tenant ID for multi-tenant | - |
| `elaro.plugin.environment` | Environment (dev/staging/prod) | `dev` |
| `elaro.plugin.beacon-interval-seconds` | Beacon send interval | `30` |
| `elaro.plugin.ui-base-url` | Base URL for UI screens | Auto-detected |
| `elaro.plugin.host` | Host address | Auto-detected |
| `elaro.plugin.port` | Port number | Auto-detected |
| `elaro.plugin.kafka.bootstrap-servers` | Kafka servers | `localhost:9092` |
| `elaro.plugin.kafka.announce-topic` | Beacon topic | `elaro.plugin.announce` |
| `elaro.plugin.kafka.ack-topic` | Acknowledgment topic | `elaro.plugin.ack` |

## @PluginScreen Annotation

Define screens that your plugin provides:

```java
@PluginScreen(
    path = "/invoices",           // Route path
    title = "Invoices",           // Menu title
    icon = "FileText",            // Lucide icon name
    order = 10,                   // Sort order (lower = first)
    parent = "",                  // Parent path for nesting
    permissions = {"billing:read"} // Required permissions
)
public class InvoicesScreen { }
```

### Icon Names

Use any icon from [Lucide Icons](https://lucide.dev/icons):
- `FileText` - Documents
- `CreditCard` - Payments
- `Settings` - Configuration
- `Users` - User management
- `BarChart` - Analytics

## REST Endpoints

The SDK exposes these endpoints on your plugin:

| Endpoint | Description |
|----------|-------------|
| `GET /plugin/manifest` | Full plugin manifest |
| `GET /plugin/screens` | List of registered screens |
| `GET /plugin/health` | Plugin health check |
| `GET /plugin/info` | Basic plugin info |

## Multi-Tenant Setup

For multi-tenant deployments, set the tenant ID:

```yaml
elaro:
  plugin:
    tenant-id: ${TENANT_ID:default}
```

Each tenant can have their own instance of the plugin registered.

## Registration Status

The plugin tracks its registration status:

- `PENDING` - Sending beacons, waiting for acknowledgment
- `ACCEPTED` - Successfully registered with Elaro
- `REJECTED` - Registration was rejected (check logs for reason)

You can check status programmatically:

```java
@Autowired
private PluginLifecycleManager lifecycleManager;

public void checkStatus() {
    if (lifecycleManager.isRegistered()) {
        // Plugin is registered
    } else if (lifecycleManager.isPending()) {
        // Still waiting for acknowledgment
    } else if (lifecycleManager.isRejected()) {
        // Registration was rejected
    }
}
```

## Building

```bash
./mvnw clean install
```

The SDK will be installed to your local Maven repository.

## Requirements

- Java 21+
- Spring Boot 3.2+
- Apache Kafka (for beacon messaging)

## License

Proprietary - Elaro.ai
