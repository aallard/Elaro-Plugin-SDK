package ai.elaro.sdk.logging;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Structured log message DTO sent to Kafka for centralized logging.
 * Contains all metadata needed for the Console to display and filter logs.
 */
public record LogMessage(
    UUID id,
    String serviceId,
    String serviceName,
    LogLevel level,
    String logger,           // e.g., "com.example.MyClass"
    String message,
    String stackTrace,       // For errors
    Map<String, String> context,  // Additional metadata
    String threadName,
    Instant timestamp,
    UUID tenantId
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id = UUID.randomUUID();
        private String serviceId;
        private String serviceName;
        private LogLevel level = LogLevel.INFO;
        private String logger;
        private String message;
        private String stackTrace;
        private Map<String, String> context;
        private String threadName = Thread.currentThread().getName();
        private Instant timestamp = Instant.now();
        private UUID tenantId;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder level(LogLevel level) {
            this.level = level;
            return this;
        }

        public Builder logger(String logger) {
            this.logger = logger;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder stackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public Builder context(Map<String, String> context) {
            this.context = context;
            return this;
        }

        public Builder threadName(String threadName) {
            this.threadName = threadName;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder tenantId(UUID tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public LogMessage build() {
            return new LogMessage(id, serviceId, serviceName, level, logger,
                message, stackTrace, context, threadName, timestamp, tenantId);
        }
    }
}
