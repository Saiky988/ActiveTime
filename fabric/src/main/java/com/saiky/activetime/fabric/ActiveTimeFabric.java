package com.saiky.activetime.fabric;

import com.saiky.activetime.common.ActiveTimeConfig;
import com.saiky.activetime.common.ActiveTimeController;
import net.fabricmc.api.DedicatedServerModInitializer;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Fabric server entrypoint for ActiveTime.
 */
public class ActiveTimeFabric implements DedicatedServerModInitializer {

    private static final Logger LOGGER = Logger.getLogger("ActiveTime-Fabric");
    private static ActiveTimeFabric instance;

    private ActiveTimeConfig config;
    private ActiveTimeController controller;
    private FabricTickAdapter tickAdapter;
    private ScheduledExecutorService scheduler;
    private Object serverInstance;

    @Override
    public void onInitializeServer() {
        instance = this;

        // 1. Load config
        this.config = new ActiveTimeConfig();
        Path configPath = Paths.get("config", "activetime.properties");
        config.loadFromFile(configPath, LOGGER);

        LOGGER.info("ActiveTime (Fabric) initialized. Automatic idle freeze: "
                + (config.isEnabled() && config.isFreezeWhenEmpty() ? "enabled" : "disabled") + ".");

        // 2. Start monitor loop to detect server lifecycle and player presence
        startPresenceMonitor();
    }

    private void startPresenceMonitor() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ActiveTime-PresenceMonitor");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (serverInstance == null) {
                    serverInstance = locateServerInstance();
                    if (serverInstance != null) {
                        this.tickAdapter = new FabricTickAdapter(serverInstance, LOGGER);
                        this.controller = new ActiveTimeController(config, tickAdapter, LOGGER, this::broadcastMessage);
                        LOGGER.info("ActiveTime bound to MinecraftServer instance successfully.");
                    }
                }

                if (serverInstance != null && controller != null) {
                    int players = getPlayerCount(serverInstance);
                    controller.reconcile(players);
                }
            } catch (Throwable t) {
                LOGGER.fine("Presence monitor tick error: " + t.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private Object locateServerInstance() {
        // Attempt to locate via net.fabricmc.loader.api.FabricLoader or ServerLifecycle
        try {
            Class<?> flClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object loader = flClass.getMethod("getInstance").invoke(null);
            Method getGameInstance = loader.getClass().getMethod("getGameInstance");
            Object game = getGameInstance.invoke(loader);
            if (game != null && game.getClass().getName().contains("Server")) {
                return game;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private int getPlayerCount(Object server) {
        try {
            Method m = findMethod(server.getClass(), "getPlayerCount", "getCurrentPlayerCount");
            if (m != null) {
                return (int) m.invoke(server);
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    private void broadcastMessage(String msg) {
        // Format legacy & codes if any and log/broadcast
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

    private static Method findMethod(Class<?> clazz, String... names) {
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

    public static ActiveTimeFabric getInstance() {
        return instance;
    }

    public ActiveTimeController getController() {
        return controller;
    }
}
