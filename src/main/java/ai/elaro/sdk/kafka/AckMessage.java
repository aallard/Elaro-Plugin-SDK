package ai.elaro.sdk.kafka;

import ai.elaro.sdk.registration.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Acknowledgment message sent from the Elaro platform to plugins
 * in response to beacon announcements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AckMessage {

    /**
     * The plugin ID this acknowledgment is for.
     */
    private String pluginId;

    /**
     * The registration status (ACCEPTED or REJECTED).
     */
    private RegistrationStatus status;

    /**
     * The environment the plugin was registered to (if accepted).
     */
    private String environment;

    /**
     * Reason for rejection (if rejected).
     */
    private String reason;

    /**
     * When this acknowledgment was sent.
     */
    private Instant timestamp;

    /**
     * The tenant ID this plugin was registered under.
     */
    private String tenantId;
}
