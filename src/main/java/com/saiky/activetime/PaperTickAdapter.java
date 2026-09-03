package com.saiky.activetime;

import org.bukkit.Server;
import org.bukkit.ServerTickManager;

import java.util.logging.Logger;

/**
 * Native Paper tick adapter using Bukkit's ServerTickManager with safe fallback to command dispatch.
 */
public class PaperTickAdapter implements TickAdapter {

    private final Server server;
    private final Logger logger;

    public PaperTickAdapter(Server server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public boolean isFrozen() {
        try {
            ServerTickManager tickManager = server.getServerTickManager();
            if (tickManager != null) {
                return tickManager.isFrozen();
            }
        } catch (Throwable t) {
            logger.fine("ServerTickManager.isFrozen() unavailable, assuming unfrozen: " + t.getMessage());
        }
        return false;
    }

    @Override
    public void setFrozen(boolean frozen) {
        try {
            ServerTickManager tickManager = server.getServerTickManager();
            if (tickManager != null) {
                tickManager.setFrozen(frozen);
                return;
            }
        } catch (Throwable t) {
            logger.fine("ServerTickManager.setFrozen() error, falling back to command dispatch: " + t.getMessage());
        }

        // Graceful fallback to Minecraft's native /tick freeze command
        String cmd = frozen ? "tick freeze" : "tick unfreeze";
        server.dispatchCommand(server.getConsoleSender(), cmd);
    }
}

