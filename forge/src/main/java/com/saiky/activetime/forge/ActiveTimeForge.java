package com.saiky.activetime.forge;

import com.saiky.activetime.common.ActiveTimeConfig;
import com.saiky.activetime.common.ActiveTimeController;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Forge mod entrypoint for ActiveTime.
 */
@Mod("activetime")
public class ActiveTimeForge {

    public static final String MOD_ID = "activetime";
    private static final Logger LOGGER = Logger.getLogger("ActiveTime-Forge");
    private static ActiveTimeForge instance;

    private ActiveTimeConfig config;
    private ActiveTimeController controller;
    private ForgeTickAdapter tickAdapter;
    private ScheduledExecutorService scheduler;
    private Object serverInstance;

    public ActiveTimeForge() {
        instance = this;

        // 1. Load config
        this.config = new ActiveTimeConfig();
        Path configPath = Paths.get("config", "activetime.properties");
        config.loadFromFile(configPath, LOGGER);

        LOGGER.info("ActiveTime (Forge) initialized. Automatic idle freeze: "
                + (config.isEnabled() && config.isFreezeWhenEmpty() ? "enabled" : "disabled") + ".");

        // 2. Start monitor loop
        startPresenceMonitor();
    }

    private void startPresenceMonitor() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ActiveTime-ForgePresenceMonitor");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (serverInstance == null) {
                    serverInstance = locateServerInstance();
                    if (serverInstance != null) {
                        this.tickAdapter = new ForgeTickAdapter(serverInstance, LOGGER);
                        this.controller = new ActiveTimeController(config, tickAdapter, LOGGER, this::broadcastMessage);
                        LOGGER.info("ActiveTime bound to Forge MinecraftServer instance successfully.");
                    }
                }

                if (serverInstance != null && controller != null) {
                    int players = getPlayerCount(serverInstance);
                    controller.reconcile(players);
                }
            } catch (Throwable t) {
                LOGGER.fine("Forge presence monitor tick error: " + t.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private Object locateServerInstance() {
        // Look via net.minecraftforge.server.ServerLifecycleHooks
        try {
            Class<?> hooks = Class.forName("net.minecraftforge.server.ServerLifecycleHooks");
            Method m = hooks.getMethod("getCurrentServer");
            return m.invoke(null);
        } catch (Throwable ignored) {
        }
        return null;
    }

    private int getPlayerCount(Object server) {
        try {
            Method m = findMethodByName(server.getClass(), "getPlayerCount", "getCurrentPlayerCount");
            if (m != null) {
                return (int) m.invoke(server);
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    private void broadcastMessage(String msg) {
        String clean = msg.replaceAll("&[0-9a-fk-or]", "");
        LOGGER.info(clean);
    }

    public void onServerStopping() {
        if (controller != null) {
            controller.onShutdown();
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private static Method findMethodByName(Class<?> clazz, String... names) {
        for (String name : names) {
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(name)) {
                    m.setAccessible(true);
                    return m;
                }
            }
        }
        return null;
    }

    public static ActiveTimeForge getInstance() {
        return instance;
    }

    public ActiveTimeController getController() {
        return controller;
    }
}
