package com.elaro.plugin.sdk.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating ElaroLogger instances.
 * Loggers are cached and reused per class name.
 *
 * <p>Injected via Spring and configured automatically by ElaroLoggingAutoConfiguration.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * {@code
 * @Service
 * public class MyService {
 *     private final ElaroLogger log;
 *
 *     public MyService(ElaroLoggerFactory loggerFactory) {
 *         this.log = loggerFactory.getLogger(MyService.class);
 *     }
 * }
 * }
 * </pre>
 */
public class ElaroLoggerFactory {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, ElaroLogger> loggers = new ConcurrentHashMap<>();

    private String serviceId;
    private String serviceName;
    private UUID tenantId;
    private LogLevel defaultMinKafkaLevel = LogLevel.DEBUG;

    public ElaroLoggerFactory(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Configure the factory with service identity.
     * Called automatically by ElaroLoggingAutoConfiguration.
     *
     * @param serviceId Unique service identifier
     * @param serviceName Human-readable service name
     * @param tenantId Tenant ID for multi-tenant deployments
     */
    public void configure(String serviceId, String serviceName, UUID tenantId) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.tenantId = tenantId;
    }

    /**
     * Set the default minimum level for Kafka publishing.
     * Logs below this level will only go to local SLF4J.
     *
     * @param level Minimum level for Kafka publishing
     */
    public void setDefaultMinKafkaLevel(LogLevel level) {
        this.defaultMinKafkaLevel = level;
    }

    /**
     * Get a logger for the specified class.
     * Loggers are cached and reused.
     *
     * @param clazz The class to create a logger for
     * @return ElaroLogger instance for the class
     */
    public ElaroLogger getLogger(Class<?> clazz) {
        return loggers.computeIfAbsent(clazz.getName(), name -> {
            ElaroLogger logger = new ElaroLogger(clazz, kafkaTemplate, objectMapper,
                serviceId, serviceName, tenantId);
            logger.setMinKafkaLevel(defaultMinKafkaLevel);
            return logger;
        });
    }
}
