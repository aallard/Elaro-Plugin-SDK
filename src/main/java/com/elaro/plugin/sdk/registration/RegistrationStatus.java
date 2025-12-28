package com.elaro.plugin.sdk.registration;

/**
 * Represents the registration status of a plugin with the Elaro platform.
 */
public enum RegistrationStatus {

    /**
     * Plugin is sending beacons and waiting for acknowledgment.
     */
    PENDING,

    /**
     * Plugin has been accepted by the Elaro platform.
     */
    ACCEPTED,

    /**
     * Plugin registration was rejected by the Elaro platform.
     */
    REJECTED
}
