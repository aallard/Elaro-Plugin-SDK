package com.elaro.plugin.sdk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Elaro Plugin SDK.
 * These can be set in application.yml or application.properties.
 *
 * <p>Example configuration:</p>
 * <pre>
 * elaro:
 *   plugin:
 *     id: com.acme.billing
 *     name: Billing Service
 *     vendor: Acme Corp
 *     vendor-email: support@acme.com
 *     beacon-interval-seconds: 30
 *     kafka:
 *       bootstrap-servers: localhost:9092
 * </pre>
 */
@ConfigurationProperties(prefix = "elaro.plugin")
@Data
public class PluginProperties {

    /**
     * Unique identifier for this plugin (e.g., "com.acme.billing").
     */
    private String id;

    /**
     * Human-readable display name.
     */
    private String name;

    /**
     * Vendor/company name.
     */
    private String vendor;

    /**
     * Vendor contact email.
     */
    private String vendorEmail;

    /**
     * Description of what this plugin does.
     */
    private String description;

    /**
     * Plugin version.
     */
    private String version = "1.0.0";

    /**
     * URL to the plugin's icon.
     */
    private String iconUrl;

    /**
     * Tenant ID for multi-tenant deployments.
     */
    private String tenantId;

    /**
     * Environment (dev, staging, prod).
     */
    private String environment = "dev";

    /**
     * Interval in seconds between beacon announcements.
     */
    private int beaconIntervalSeconds = 30;

    /**
     * Base URL where the plugin's UI is served.
     * If not set, will be auto-detected from server properties.
     */
    private String uiBaseUrl;

    /**
     * Health endpoint path.
     */
    private String healthEndpoint = "/actuator/health";

    /**
     * Host address for the plugin. Auto-detected if not set.
     */
    private String host;

    /**
     * Port the plugin is running on. Auto-detected if not set.
     */
    private Integer port;

    /**
     * Kafka configuration for plugin communication.
     */
    private KafkaProperties kafka = new KafkaProperties();

    @Data
    public static class KafkaProperties {

        /**
         * Kafka bootstrap servers.
         */
        private String bootstrapServers = "localhost:9092";

        /**
         * Topic for plugin announcements (beacons).
         */
        private String announceTopic = "elaro.plugin.announce";

        /**
         * Topic for acknowledgment messages.
         */
        private String ackTopic = "elaro.plugin.ack";

        /**
         * Consumer group ID for acknowledgment listener.
         */
        private String groupId = "elaro-plugin";
    }
}
