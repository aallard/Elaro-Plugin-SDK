package com.elaro.plugin.sdk.registration;

import com.elaro.plugin.sdk.config.PluginProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * Manages the plugin lifecycle, handling startup and shutdown events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PluginLifecycleManager implements ApplicationListener<ContextClosedEvent> {

    private final PluginProperties properties;
    private final PluginRegistrar registrar;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Plugin shutting down: {} (instance: {})",
            properties.getId(),
            registrar.getInstanceId());

        // The PluginRegistrar's @PreDestroy will handle stopping the beacon
        // This listener is here for any additional shutdown logic needed

        RegistrationStatus status = registrar.getStatus();
        log.info("Final registration status: {}", status);
    }

    /**
     * Check if the plugin is currently registered with the Elaro platform.
     */
    public boolean isRegistered() {
        return registrar.getStatus() == RegistrationStatus.ACCEPTED;
    }

    /**
     * Check if the plugin is still waiting for registration acknowledgment.
     */
    public boolean isPending() {
        return registrar.getStatus() == RegistrationStatus.PENDING;
    }

    /**
     * Check if the plugin registration was rejected.
     */
    public boolean isRejected() {
        return registrar.getStatus() == RegistrationStatus.REJECTED;
    }

    /**
     * Get the current registration status.
     */
    public RegistrationStatus getStatus() {
        return registrar.getStatus();
    }
}
