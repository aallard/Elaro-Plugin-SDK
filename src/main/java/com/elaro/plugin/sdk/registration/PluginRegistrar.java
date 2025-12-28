package com.elaro.plugin.sdk.registration;

import com.elaro.plugin.sdk.config.PluginProperties;
import com.elaro.plugin.sdk.kafka.AckMessage;
import com.elaro.plugin.sdk.kafka.PluginKafkaProducer;
import com.elaro.plugin.sdk.ui.PluginScreenRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages the plugin beacon registration process.
 * Sends periodic beacon announcements until acknowledged by the Elaro platform.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PluginRegistrar {

    private final PluginKafkaProducer kafkaProducer;
    private final PluginProperties properties;
    private final PluginScreenRegistry screenRegistry;
    private final Environment environment;

    private ScheduledExecutorService scheduler;
    private volatile RegistrationStatus status = RegistrationStatus.PENDING;
    private volatile boolean running = false;
    private final String instanceId = UUID.randomUUID().toString();

    /**
     * Get the current registration status.
     */
    public RegistrationStatus getStatus() {
        return status;
    }

    /**
     * Get the unique instance ID for this running plugin.
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Start sending beacon announcements.
     */
    @PostConstruct
    public void startBeacon() {
        log.info("Starting plugin beacon for: {} (instance: {})",
            properties.getId(), instanceId);

        running = true;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "plugin-beacon-" + properties.getId());
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(
            this::sendBeacon,
            0,  // Start immediately
            properties.getBeaconIntervalSeconds(),
            TimeUnit.SECONDS
        );
    }

    /**
     * Send a single beacon announcement.
     */
    private void sendBeacon() {
        if (!running || status != RegistrationStatus.PENDING) {
            return;
        }

        try {
            PluginManifest manifest = buildManifest();
            kafkaProducer.sendAnnouncement(manifest);
            log.debug("Sent beacon for plugin: {} (instance: {})",
                properties.getId(), instanceId);
        } catch (Exception e) {
            log.error("Failed to send beacon: {}", e.getMessage());
        }
    }

    /**
     * Handle acknowledgment received from the Elaro platform.
     *
     * @param ack The acknowledgment message
     */
    public void onAckReceived(AckMessage ack) {
        if (!ack.getPluginId().equals(properties.getId())) {
            return;
        }

        this.status = ack.getStatus();

        if (status == RegistrationStatus.ACCEPTED) {
            log.info("Plugin ACCEPTED: {} -> environment: {}, tenant: {}",
                properties.getId(),
                ack.getEnvironment(),
                ack.getTenantId());
            stopBeacon();
        } else if (status == RegistrationStatus.REJECTED) {
            log.warn("Plugin REJECTED: {} - Reason: {}",
                properties.getId(), ack.getReason());
            stopBeacon();
        }
    }

    /**
     * Stop sending beacon announcements.
     */
    @PreDestroy
    public void stopBeacon() {
        log.info("Stopping plugin beacon for: {}", properties.getId());
        running = false;

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Build the plugin manifest from configuration and discovered screens.
     */
    private PluginManifest buildManifest() {
        return PluginManifest.builder()
            .pluginId(properties.getId())
            .name(properties.getName())
            .description(properties.getDescription())
            .version(properties.getVersion())
            .vendor(properties.getVendor())
            .vendorEmail(properties.getVendorEmail())
            .iconUrl(properties.getIconUrl())
            .healthEndpoint(properties.getHealthEndpoint())
            .uiBaseUrl(determineUiBaseUrl())
            .screens(screenRegistry.getScreens())
            .tenantId(properties.getTenantId())
            .environment(properties.getEnvironment())
            .instanceId(instanceId)
            .deployedAt(Instant.now())
            .host(determineHost())
            .port(determinePort())
            .build();
    }

    /**
     * Determine the UI base URL for this plugin.
     */
    private String determineUiBaseUrl() {
        if (properties.getUiBaseUrl() != null && !properties.getUiBaseUrl().isEmpty()) {
            return properties.getUiBaseUrl();
        }

        String host = determineHost();
        Integer port = determinePort();

        return String.format("http://%s:%d", host, port);
    }

    /**
     * Determine the host address for this plugin.
     */
    private String determineHost() {
        if (properties.getHost() != null && !properties.getHost().isEmpty()) {
            return properties.getHost();
        }

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.warn("Could not determine host address, using localhost");
            return "localhost";
        }
    }

    /**
     * Determine the port for this plugin.
     */
    private Integer determinePort() {
        if (properties.getPort() != null) {
            return properties.getPort();
        }

        String serverPort = environment.getProperty("server.port", "8080");
        try {
            return Integer.parseInt(serverPort);
        } catch (NumberFormatException e) {
            return 8080;
        }
    }
}
