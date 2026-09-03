package com.saiky.activetime;

/**
 * Represents the simulation state of the Minecraft server.
 */
public enum SimulationState {
    /**
     * Server tick simulation is running normally.
     */
    RUNNING,

    /**
     * Server tick simulation is frozen.
     */
    FROZEN,

    /**
     * Simulation state has not yet been determined (e.g. during early initialization).
     */
    UNKNOWN
}

