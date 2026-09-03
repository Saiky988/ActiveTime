package com.saiky.activetime.common;

/**
 * Platform-independent abstraction over Minecraft's native tick freeze control.
 */
public interface TickManagerAdapter {

    /**
     * Checks if the server simulation is currently frozen.
     *
     * @return true if frozen, false otherwise
     */
    boolean isFrozen();

    /**
     * Sets the server simulation frozen state.
     *
     * @param frozen true to freeze simulation, false to unfreeze
     */
    void setFrozen(boolean frozen);
}

