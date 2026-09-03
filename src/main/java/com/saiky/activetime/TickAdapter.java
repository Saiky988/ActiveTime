package com.saiky.activetime;

/**
 * Abstraction over server tick freeze manipulation.
 * Enables clean native Paper integration and direct testability.
 */
public interface TickAdapter {

    /**
     * Checks if the server simulation is currently frozen.
     *
     * @return true if frozen, false otherwise
     */
    boolean isFrozen();

    /**
     * Sets the server simulation frozen state.
     *
     * @param frozen true to freeze, false to unfreeze
     */
    void setFrozen(boolean frozen);
}

