package ai.elaro.sdk;

import ai.elaro.sdk.config.PluginKafkaConfig;
import ai.elaro.sdk.config.PluginProperties;
import ai.elaro.sdk.kafka.PluginKafkaListener;
import ai.elaro.sdk.kafka.PluginKafkaProducer;
import ai.elaro.sdk.registration.PluginLifecycleManager;
import ai.elaro.sdk.registration.PluginRegistrar;
import ai.elaro.sdk.ui.PluginScreenRegistry;
import ai.elaro.sdk.ui.PluginUiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Spring Boot auto-configuration for the Elaro Plugin SDK.
 * This configuration is automatically loaded when the SDK is on the classpath
 * and the plugin is properly configured.
 */
@AutoConfiguration
@EnableConfigurationProperties(PluginProperties.class)
@ConditionalOnProperty(prefix = "elaro.plugin", name = "id")
@Import(PluginKafkaConfig.class)
@EnableKafka
@ComponentScan(basePackages = "ai.elaro.sdk")
@Slf4j
public class ElaroPluginAutoConfiguration {

    public ElaroPluginAutoConfiguration(PluginProperties properties) {
        log.info("Initializing Elaro Plugin SDK for: {} ({})",
            properties.getName(), properties.getId());
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginScreenRegistry pluginScreenRegistry(
            org.springframework.context.ApplicationContext applicationContext) {
        return new PluginScreenRegistry(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginKafkaProducer pluginKafkaProducer(
            org.springframework.kafka.core.KafkaTemplate<String, String> pluginKafkaTemplate,
            PluginProperties properties,
            com.fasterxml.jackson.databind.ObjectMapper pluginObjectMapper) {
        return new PluginKafkaProducer(pluginKafkaTemplate, properties, pluginObjectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginRegistrar pluginRegistrar(
            PluginKafkaProducer kafkaProducer,
            PluginProperties properties,
            PluginScreenRegistry screenRegistry,
            org.springframework.core.env.Environment environment) {
        return new PluginRegistrar(kafkaProducer, properties, screenRegistry, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginKafkaListener pluginKafkaListener(
            PluginRegistrar registrar,
            PluginProperties properties,
            com.fasterxml.jackson.databind.ObjectMapper pluginObjectMapper) {
        return new PluginKafkaListener(registrar, properties, pluginObjectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginLifecycleManager pluginLifecycleManager(
            PluginProperties properties,
            PluginRegistrar registrar) {
        return new PluginLifecycleManager(properties, registrar);
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginUiController pluginUiController(
            PluginProperties properties,
            PluginScreenRegistry screenRegistry,
            PluginRegistrar registrar) {
        return new PluginUiController(properties, screenRegistry, registrar);
    }
}
