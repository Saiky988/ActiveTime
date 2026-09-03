package com.saiky.activetime.common;

/**
 * Represents the simulation state of the Minecraft server.
 */
public enum ActiveTimeState {
    /**
     * Server simulation is running normally.
     */
    RUNNING,

    /**
     * Server simulation is frozen.
     */
    FROZEN,

    /**
     * State is undetermined or initializing.
     */
    UNKNOWN
}
