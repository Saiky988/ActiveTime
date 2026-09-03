package com.saiky.activetime.common;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Platform-agnostic state controller for ActiveTime.
 * Guarantees atomic state transitions and prevents duplicate freeze/unfreeze actions.
 */
public class ActiveTimeController {

    private final ActiveTimeConfig config;
    private final TickManagerAdapter tickAdapter;
    private final Logger logger;
    private final Consumer<String> messageBroadcaster;

    private ActiveTimeState state = ActiveTimeState.UNKNOWN;
    private boolean frozenByActiveTime = false;

    public ActiveTimeController(ActiveTimeConfig config,
                                TickManagerAdapter tickAdapter,
                                Logger logger,
                                Consumer<String> messageBroadcaster) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
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
     * Gets the current simulation state.
     */
    public synchronized ActiveTimeState getState() {
        if (state == ActiveTimeState.UNKNOWN) {
            state = tickAdapter.isFrozen() ? ActiveTimeState.FROZEN : ActiveTimeState.RUNNING;
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
            state = ActiveTimeState.FROZEN;
            return false;
        }

        tickAdapter.setFrozen(true);
        state = ActiveTimeState.FROZEN;
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
     * Resumes the server simulation.
     *
     * @param manual true if triggered manually by an administrator
     * @return true if state transitioned to running, false if already running
     */
    public synchronized boolean unfreeze(boolean manual) {
        if (!isFrozen()) {
            state = ActiveTimeState.RUNNING;
            return false;
        }

        tickAdapter.setFrozen(false);
        state = ActiveTimeState.RUNNING;
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
     * Reconciles simulation state based on current player count and configuration.
     *
     * @param onlinePlayerCount current online players on the server
     */
    public synchronized void reconcile(int onlinePlayerCount) {
        if (!config.isEnabled()) {
            return;
        }

        if (onlinePlayerCount <= 0) {
            if (config.isFreezeWhenEmpty() && !isFrozen()) {
                freeze(false);
            }
        } else {
            if (isFrozen()) {
                unfreeze(false);
            } else {
                state = ActiveTimeState.RUNNING;
            }
        }
    }

    /**
     * Restores normal server ticking before mod/plugin unload or shutdown.
     */
    public synchronized void onShutdown() {
        if (isFrozen() && frozenByActiveTime) {
            logger.info("Restoring normal server simulation tick before shutdown...");
            tickAdapter.setFrozen(false);
            state = ActiveTimeState.RUNNING;
            frozenByActiveTime = false;
        }
    }

    public boolean isFrozenByActiveTime() {
        return frozenByActiveTime;
    }

    public ActiveTimeConfig getConfig() {
        return config;
    }

    private void notifyFreeze() {
        if (config.isMessagesEnabled() && messageBroadcaster != null) {
            String msg = config.getMessagePrefix() + config.getFreezeMessage();
            messageBroadcaster.accept(msg);
        }
    }

    private void notifyUnfreeze() {
        if (config.isMessagesEnabled() && messageBroadcaster != null) {
            String msg = config.getMessagePrefix() + config.getUnfreezeMessage();
            messageBroadcaster.accept(msg);
        }
    }
}
