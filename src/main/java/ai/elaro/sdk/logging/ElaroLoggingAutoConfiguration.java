package ai.elaro.sdk.logging;

import ai.elaro.sdk.config.PluginProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

/**
 * Auto-configuration for Elaro centralized logging.
 * Automatically registers ElaroLoggerFactory when Kafka is available.
 */
@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
public class ElaroLoggingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ElaroLoggingAutoConfiguration.class);
    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    @ConditionalOnMissingBean
    public ElaroLoggerFactory elaroLoggerFactory(
            KafkaTemplate<String, String> pluginKafkaTemplate,
            ObjectMapper pluginObjectMapper,
            PluginProperties properties) {

        ElaroLoggerFactory factory = new ElaroLoggerFactory(pluginKafkaTemplate, pluginObjectMapper);

        UUID tenantId = parseTenantId(properties.getTenantId());

        factory.configure(
            properties.getId(),
            properties.getName(),
            tenantId
        );

        log.info("Elaro centralized logging configured for service: {} ({})",
            properties.getName(), properties.getId());

        return factory;
    }

    private UUID parseTenantId(String tenantIdStr) {
        if (tenantIdStr == null || tenantIdStr.isBlank()) {
            return DEFAULT_TENANT_ID;
        }
        try {
            return UUID.fromString(tenantIdStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid tenantId '{}', using default", tenantIdStr);
            return DEFAULT_TENANT_ID;
        }
    }
}
