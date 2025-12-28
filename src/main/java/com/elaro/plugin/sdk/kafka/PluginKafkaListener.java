package com.elaro.plugin.sdk.kafka;

import com.elaro.plugin.sdk.config.PluginProperties;
import com.elaro.plugin.sdk.registration.PluginRegistrar;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener for acknowledgment messages from the Elaro platform.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PluginKafkaListener {

    private final PluginRegistrar registrar;
    private final PluginProperties properties;
    private final ObjectMapper pluginObjectMapper;

    /**
     * Listen for acknowledgment messages from the Elaro platform.
     *
     * @param message The raw JSON message
     */
    @KafkaListener(
        topics = "${elaro.plugin.kafka.ack-topic:elaro.plugin.ack}",
        containerFactory = "pluginKafkaListenerContainerFactory"
    )
    public void onAck(String message) {
        try {
            AckMessage ack = pluginObjectMapper.readValue(message, AckMessage.class);

            // Only process if it's for this plugin
            if (properties.getId().equals(ack.getPluginId())) {
                log.info("Received ACK for plugin {}: {}",
                    ack.getPluginId(), ack.getStatus());
                registrar.onAckReceived(ack);
            }
        } catch (Exception e) {
            log.error("Failed to parse ACK message: {}", e.getMessage());
            log.debug("Raw message: {}", message);
        }
    }
}
