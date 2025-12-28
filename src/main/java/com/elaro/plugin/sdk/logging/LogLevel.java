package com.elaro.plugin.sdk.logging;

/**
 * Log levels for the Elaro centralized logging system.
 * Each level has a severity value used for filtering.
 */
public enum LogLevel {
    TRACE(0),
    DEBUG(10),
    INFO(20),
    WARN(30),
    ERROR(40),
    FATAL(50);

    private final int severity;

    LogLevel(int severity) {
        this.severity = severity;
    }

    public int getSeverity() {
        return severity;
    }

    /**
     * Check if this level is at least as severe as another level.
     * @param other The level to compare against
     * @return true if this level's severity >= other's severity
     */
    public boolean isAtLeast(LogLevel other) {
        return this.severity >= other.severity;
    }
}
