package com.elaro.plugin.sdk.ui;

import com.elaro.plugin.sdk.config.PluginProperties;
import com.elaro.plugin.sdk.model.ScreenDefinition;
import com.elaro.plugin.sdk.registration.PluginManifest;
import com.elaro.plugin.sdk.registration.PluginRegistrar;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller that exposes plugin metadata endpoints.
 * These endpoints can be called by the Elaro platform or for debugging.
 */
@RestController
@RequestMapping("/plugin")
@RequiredArgsConstructor
public class PluginUiController {

    private final PluginProperties properties;
    private final PluginScreenRegistry screenRegistry;
    private final PluginRegistrar registrar;

    /**
     * Get the full plugin manifest.
     */
    @GetMapping("/manifest")
    public PluginManifest getManifest() {
        return PluginManifest.builder()
            .pluginId(properties.getId())
            .name(properties.getName())
            .description(properties.getDescription())
            .version(properties.getVersion())
            .vendor(properties.getVendor())
            .vendorEmail(properties.getVendorEmail())
            .iconUrl(properties.getIconUrl())
            .screens(screenRegistry.getScreens())
            .environment(properties.getEnvironment())
            .tenantId(properties.getTenantId())
            .instanceId(registrar.getInstanceId())
            .build();
    }

    /**
     * Get all registered screens.
     */
    @GetMapping("/screens")
    public List<ScreenDefinition> getScreens() {
        return screenRegistry.getScreens();
    }

    /**
     * Health check endpoint for the plugin.
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("pluginId", properties.getId());
        response.put("name", properties.getName());
        response.put("version", properties.getVersion());
        response.put("registrationStatus", registrar.getStatus().name());
        response.put("instanceId", registrar.getInstanceId());
        return response;
    }

    /**
     * Get basic plugin info.
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", properties.getId());
        response.put("name", properties.getName());
        response.put("vendor", properties.getVendor());
        response.put("version", properties.getVersion());
        response.put("environment", properties.getEnvironment());
        response.put("screenCount", screenRegistry.getScreenCount());
        return response;
    }
}
