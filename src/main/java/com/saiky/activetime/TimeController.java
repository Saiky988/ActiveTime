package com.saiky.activetime;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Core state manager controlling server simulation tick state.
 */
public class TimeController {

    private final ConfigManager configManager;
    private final TickAdapter tickAdapter;
    private final Logger logger;
    private final Consumer<String> messageBroadcaster;

    private SimulationState state = SimulationState.UNKNOWN;
    private boolean frozenByActiveTime = false;

    public TimeController(ConfigManager configManager,
                          TickAdapter tickAdapter,
                          Logger logger,
                          Consumer<String> messageBroadcaster) {
        this.configManager = Objects.requireNonNull(configManager, "configManager cannot be null");
        this.tickAdapter = Objects.requireNonNull(tickAdapter, "tickAdapter cannot be null");
        this.logger = Objects.requireNonNull(logger, "logger cannot be null");
        this.messageBroadcaster = messageBroadcaster;
    }

    /**
     * Checks if the server simulation is currently frozen.
     */
    public boolean isFrozen() {
        return tickAdapter.isFrozen();
    }

    /**
     * Gets the current tracked simulation state.
     */
    public SimulationState getState() {
        // Synchronize with native adapter if state was unknown
        if (state == SimulationState.UNKNOWN) {
            state = tickAdapter.isFrozen() ? SimulationState.FROZEN : SimulationState.RUNNING;
        }
        return state;
    }

    /**
     * Freezes the server simulation.
     *
     * @param manual true if triggered manually by an administrator
     * @return true if state transitioned to frozen, false if already frozen
     */
    public synchronized boolean freeze(boolean manual) {
        if (isFrozen()) {
            state = SimulationState.FROZEN;
            return false;
        }

        tickAdapter.setFrozen(true);
        state = SimulationState.FROZEN;
        this.frozenByActiveTime = !manual;

        if (manual) {
            logger.info("Server simulation frozen manually by administrator.");
        } else {
            logger.info("No players online. Freezing server simulation.");
        }

        notifyFreeze();
        return true;
    }

    /**
     * Resumes normal server simulation.
     *
     * @param manual true if triggered manually by an administrator
     * @return true if state transitioned to running, false if already running
     */
    public synchronized boolean unfreeze(boolean manual) {
        if (!isFrozen()) {
            state = SimulationState.RUNNING;
            return false;
        }

        tickAdapter.setFrozen(false);
        state = SimulationState.RUNNING;
        this.frozenByActiveTime = false;

        if (manual) {
            logger.info("Server simulation resumed manually by administrator.");
        } else {
            logger.info("Player activity detected. Resuming server simulation.");
        }

        notifyUnfreeze();
        return true;
    }

    /**
     * Reconciles simulation state against current online player count and configuration.
     *
     * @param onlinePlayerCount number of online players currently in the world
     */
    public synchronized void reconcile(int onlinePlayerCount) {
        if (!configManager.isEnabled()) {
            logger.fine("ActiveTime is disabled. Skipping reconciliation.");
            return;
        }

        if (onlinePlayerCount <= 0) {
            if (configManager.isFreezeWhenEmpty() && !isFrozen()) {
                freeze(false);
            }
        } else {
            if (isFrozen()) {
                unfreeze(false);
            } else {
                state = SimulationState.RUNNING;
            }
        }
    }

    /**
     * Handles safety restoration on server/plugin shutdown.
     */
    public synchronized void onShutdown() {
        if (isFrozen() && frozenByActiveTime) {
            logger.info("Restoring normal server simulation tick before ActiveTime shuts down...");
            tickAdapter.setFrozen(false);
            state = SimulationState.RUNNING;
            frozenByActiveTime = false;
        }
    }

    public boolean isFrozenByActiveTime() {
        return frozenByActiveTime;
    }

    private void notifyFreeze() {
        if (configManager.isMessagesEnabled() && messageBroadcaster != null) {
            String msg = configManager.getMessagePrefix() + configManager.getFreezeMessage();
            messageBroadcaster.accept(msg);
        }
    }

    private void notifyUnfreeze() {
        if (configManager.isMessagesEnabled() && messageBroadcaster != null) {
            String msg = configManager.getMessagePrefix() + configManager.getUnfreezeMessage();
            messageBroadcaster.accept(msg);
        }
    }
}

