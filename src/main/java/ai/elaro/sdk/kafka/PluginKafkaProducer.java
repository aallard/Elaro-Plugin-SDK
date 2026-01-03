package ai.elaro.sdk.kafka;

import ai.elaro.sdk.config.PluginProperties;
import ai.elaro.sdk.registration.PluginManifest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for sending plugin announcements (beacons) to the Elaro platform.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PluginKafkaProducer {

    private final KafkaTemplate<String, String> pluginKafkaTemplate;
    private final PluginProperties properties;
    private final ObjectMapper pluginObjectMapper;

    /**
     * Send a plugin announcement (beacon) to the Elaro platform.
     *
     * @param manifest The plugin manifest to announce
     */
    public void sendAnnouncement(PluginManifest manifest) {
        try {
            String json = pluginObjectMapper.writeValueAsString(manifest);
            String topic = properties.getKafka().getAnnounceTopic();

            CompletableFuture<SendResult<String, String>> future =
                pluginKafkaTemplate.send(topic, manifest.getPluginId(), json);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send beacon for plugin {}: {}",
                        manifest.getPluginId(), ex.getMessage());
                } else {
                    log.debug("Beacon sent for plugin {} to partition {} offset {}",
                        manifest.getPluginId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize plugin manifest: {}", e.getMessage());
        }
    }
}
