package ai.elaro.sdk.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Elaro centralized logger that publishes log messages to both SLF4J (local)
 * and Kafka (centralized collection).
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * @Service
 * public class MyService {
 *     private final ElaroLogger log;
 *
 *     public MyService(ElaroLoggerFactory loggerFactory) {
 *         this.log = loggerFactory.getLogger(MyService.class);
 *     }
 *
 *     public void doWork() {
 *         log.info("Processing started");
 *         log.info("Order processed", ElaroLogger.context("orderId", "123"));
 *     }
 * }
 * }
 * </pre>
 */
public class ElaroLogger {

    private static final String LOGS_TOPIC = "elaro.logs";

    private final Logger slf4jLogger;
    private final String loggerName;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String serviceId;
    private final String serviceName;
    private final UUID tenantId;
    private LogLevel minKafkaLevel = LogLevel.DEBUG;

    public ElaroLogger(Class<?> clazz, KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper, String serviceId, String serviceName, UUID tenantId) {
        this.slf4jLogger = LoggerFactory.getLogger(clazz);
        this.loggerName = clazz.getName();
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.tenantId = tenantId;
    }

    /**
     * Set minimum level for Kafka publishing (local SLF4J logging unaffected).
     * @param level Minimum level to publish to Kafka
     */
    public void setMinKafkaLevel(LogLevel level) {
        this.minKafkaLevel = level;
    }

    // ========== TRACE ==========
    public void trace(String message) {
        log(LogLevel.TRACE, message, null, null);
    }

    public void trace(String message, Object... args) {
        log(LogLevel.TRACE, format(message, args), null, null);
    }

    public void trace(String message, Map<String, String> context) {
        log(LogLevel.TRACE, message, null, context);
    }

    // ========== DEBUG ==========
    public void debug(String message) {
        log(LogLevel.DEBUG, message, null, null);
    }

    public void debug(String message, Object... args) {
        log(LogLevel.DEBUG, format(message, args), null, null);
    }

    public void debug(String message, Map<String, String> context) {
        log(LogLevel.DEBUG, message, null, context);
    }

    // ========== INFO ==========
    public void info(String message) {
        log(LogLevel.INFO, message, null, null);
    }

    public void info(String message, Object... args) {
        log(LogLevel.INFO, format(message, args), null, null);
    }

    public void info(String message, Map<String, String> context) {
        log(LogLevel.INFO, message, null, context);
    }

    // ========== WARN ==========
    public void warn(String message) {
        log(LogLevel.WARN, message, null, null);
    }

    public void warn(String message, Object... args) {
        log(LogLevel.WARN, format(message, args), null, null);
    }

    public void warn(String message, Throwable t) {
        log(LogLevel.WARN, message, t, null);
    }

    public void warn(String message, Map<String, String> context) {
        log(LogLevel.WARN, message, null, context);
    }

    public void warn(String message, Throwable t, Map<String, String> context) {
        log(LogLevel.WARN, message, t, context);
    }

    // ========== ERROR ==========
    public void error(String message) {
        log(LogLevel.ERROR, message, null, null);
    }

    public void error(String message, Object... args) {
        log(LogLevel.ERROR, format(message, args), null, null);
    }

    public void error(String message, Throwable t) {
        log(LogLevel.ERROR, message, t, null);
    }

    public void error(String message, Throwable t, Map<String, String> context) {
        log(LogLevel.ERROR, message, t, context);
    }

    // ========== FATAL ==========
    public void fatal(String message) {
        log(LogLevel.FATAL, message, null, null);
    }

    public void fatal(String message, Throwable t) {
        log(LogLevel.FATAL, message, t, null);
    }

    public void fatal(String message, Throwable t, Map<String, String> context) {
        log(LogLevel.FATAL, message, t, context);
    }

    // ========== CORE LOG METHOD ==========
    private void log(LogLevel level, String message, Throwable t, Map<String, String> context) {
        // Always log to SLF4J locally
        logToSlf4j(level, message, t);

        // Only publish to Kafka if above min level
        if (level.isAtLeast(minKafkaLevel) && kafkaTemplate != null) {
            publishToKafka(level, message, t, context);
        }
    }

    private void logToSlf4j(LogLevel level, String message, Throwable t) {
        switch (level) {
            case TRACE -> {
                if (t != null) slf4jLogger.trace(message, t);
                else slf4jLogger.trace(message);
            }
            case DEBUG -> {
                if (t != null) slf4jLogger.debug(message, t);
                else slf4jLogger.debug(message);
            }
            case INFO -> {
                if (t != null) slf4jLogger.info(message, t);
                else slf4jLogger.info(message);
            }
            case WARN -> {
                if (t != null) slf4jLogger.warn(message, t);
                else slf4jLogger.warn(message);
            }
            case ERROR, FATAL -> {
                if (t != null) slf4jLogger.error(message, t);
                else slf4jLogger.error(message);
            }
        }
    }

    private void publishToKafka(LogLevel level, String message, Throwable t, Map<String, String> context) {
        try {
            String stackTrace = null;
            if (t != null) {
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                stackTrace = sw.toString();
            }

            LogMessage logMessage = LogMessage.builder()
                .serviceId(serviceId)
                .serviceName(serviceName)
                .level(level)
                .logger(loggerName)
                .message(message)
                .stackTrace(stackTrace)
                .context(context != null ? context : new HashMap<>())
                .tenantId(tenantId)
                .build();

            String json = objectMapper.writeValueAsString(logMessage);
            kafkaTemplate.send(LOGS_TOPIC, serviceId, json);
        } catch (Exception e) {
            // Don't let logging failures break the application
            slf4jLogger.warn("Failed to publish log to Kafka: {}", e.getMessage());
        }
    }

    private String format(String message, Object... args) {
        if (args == null || args.length == 0) return message;
        // Simple {} placeholder replacement
        String result = message;
        for (Object arg : args) {
            result = result.replaceFirst("\\{}", arg != null ? arg.toString() : "null");
        }
        return result;
    }

    // ========== STATIC CONTEXT BUILDER ==========
    /**
     * Helper to build context maps fluently.
     * <p>Usage:</p>
     * <pre>
     * log.info("Order processed", ElaroLogger.context("orderId", "123", "duration", "150ms"));
     * </pre>
     *
     * @param keyValues Alternating key-value pairs
     * @return Map containing the key-value pairs
     */
    public static Map<String, String> context(String... keyValues) {
        Map<String, String> ctx = new HashMap<>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            ctx.put(keyValues[i], keyValues[i + 1]);
        }
        return ctx;
    }
}
