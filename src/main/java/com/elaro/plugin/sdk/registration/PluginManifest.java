package com.elaro.plugin.sdk.registration;

import com.elaro.plugin.sdk.model.ScreenDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Contains all metadata about a plugin that is sent to the Elaro platform
 * during beacon announcements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginManifest {

    /**
     * Unique identifier for this plugin (e.g., "com.acme.billing").
     */
    private String pluginId;

    /**
     * Human-readable display name.
     */
    private String name;

    /**
     * Description of what this plugin does.
     */
    private String description;

    /**
     * Version string (e.g., "1.0.0").
     */
    private String version;

    /**
     * Vendor/company name.
     */
    private String vendor;

    /**
     * Vendor contact email.
     */
    private String vendorEmail;

    /**
     * URL to the plugin's icon for menu display.
     */
    private String iconUrl;

    /**
     * Health check endpoint (default: /actuator/health).
     */
    private String healthEndpoint;

    /**
     * Base URL where the plugin's React screens are served.
     */
    private String uiBaseUrl;

    /**
     * List of screens this plugin provides.
     */
    private List<ScreenDefinition> screens;

    /**
     * Permissions required by this plugin.
     */
    private List<String> requiredPermissions;

    /**
     * Additional metadata key-value pairs.
     */
    private Map<String, String> metadata;

    /**
     * When this plugin instance was deployed/started.
     */
    private Instant deployedAt;

    /**
     * Environment this plugin is running in (dev, staging, prod).
     */
    private String environment;

    /**
     * Tenant ID for multi-tenant deployments.
     */
    private String tenantId;

    /**
     * Unique identifier for this running instance.
     */
    private String instanceId;

    /**
     * The hostname or IP address where the plugin is reachable.
     */
    private String host;

    /**
     * The port the plugin is listening on.
     */
    private Integer port;
}
