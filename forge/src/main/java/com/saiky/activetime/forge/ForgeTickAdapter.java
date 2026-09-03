package com.saiky.activetime.forge;

import com.saiky.activetime.common.TickManagerAdapter;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Native Forge tick adapter using Minecraft's ServerTickRateManager with command fallback.
 */
public class ForgeTickAdapter implements TickManagerAdapter {

    private final Object server;
    private final Logger logger;

    public ForgeTickAdapter(Object server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public boolean isFrozen() {
        if (server == null) {
            return false;
        }

        try {
            Method m = findMethodByName(server.getClass(), "tickRateManager", "getTickManager");
            if (m != null) {
                Object manager = m.invoke(server);
                if (manager != null) {
                    Method isFrozenMethod = findMethodByName(manager.getClass(), "isFrozen");
                    if (isFrozenMethod != null) {
                        return (boolean) isFrozenMethod.invoke(manager);
                    }
                }
            }
        } catch (Throwable t) {
            logger.fine("Could not check isFrozen via TickManager: " + t.getMessage());
        }
        return false;
    }

    @Override
    public void setFrozen(boolean frozen) {
        if (server == null) {
            return;
        }

        try {
            Method m = findMethodByName(server.getClass(), "tickRateManager", "getTickManager");
            if (m != null) {
                Object manager = m.invoke(server);
                if (manager != null) {
                    Method setFrozenMethod = findMethodBySignature(manager.getClass(), "setFrozen", boolean.class);
                    if (setFrozenMethod != null) {
                        setFrozenMethod.invoke(manager, frozen);
                        return;
                    }
                }
            }
        } catch (Throwable t) {
            logger.fine("Could not setFrozen via TickManager: " + t.getMessage());
        }

        // Fallback to native /tick freeze command
        executeCommand(frozen ? "tick freeze" : "tick unfreeze");
    }

    private void executeCommand(String command) {
        try {
            Method getCommands = findMethodByName(server.getClass(), "getCommands", "getCommandManager");
            if (getCommands != null) {
                Object manager = getCommands.invoke(server);
                Method createSource = findMethodByName(server.getClass(), "createCommandSourceStack", "getCommandSource");
                if (createSource != null && manager != null) {
                    Object source = createSource.invoke(server);
                    Method perform = findMethodByName(manager.getClass(), "performPrefixedCommand", "executeWithPrefix");
                    if (perform != null) {
                        perform.invoke(manager, source, command);
                    }
                }
            }
        } catch (Throwable t) {
            logger.warning("Failed to execute /" + command + ": " + t.getMessage());
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

    private static Method findMethodBySignature(Class<?> clazz, String name, Class<?>... paramTypes) {
        try {
            Method m = clazz.getMethod(name, paramTypes);
            m.setAccessible(true);
            return m;
        } catch (Exception ignored) {
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == paramTypes.length) {
                    m.setAccessible(true);
                    return m;
                }
            }
        }
        return null;
    }
}
