package com.saiky.activetime.paper;

import com.saiky.activetime.common.TickManagerAdapter;
import org.bukkit.Server;
import org.bukkit.ServerTickManager;

import java.util.logging.Logger;

/**
 * Native Paper tick adapter using ServerTickManager with fallback to console /tick freeze.
 */
public class PaperTickAdapter implements TickManagerAdapter {

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
            logger.fine("ServerTickManager.isFrozen() unavailable: " + t.getMessage());
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

        String cmd = frozen ? "tick freeze" : "tick unfreeze";
        server.dispatchCommand(server.getConsoleSender(), cmd);
    }
}
