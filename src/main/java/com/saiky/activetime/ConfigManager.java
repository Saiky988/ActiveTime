package com.saiky.activetime;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Logger;

/**
 * Manages loading, caching, and reloading of plugin configuration.
 */
public class ConfigManager {

    private final ActiveTime plugin;
    private final Logger logger;

    private boolean enabled;
    private boolean freezeWhenEmpty;
    private boolean messagesEnabled;
    private String messagePrefix;
    private String freezeMessage;
    private String unfreezeMessage;

    public ConfigManager(ActiveTime plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Loads or reloads configuration values from disk.
     */
    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.enabled = config.getBoolean("enabled", true);
        this.freezeWhenEmpty = config.getBoolean("freeze.when-empty", true);
        this.messagesEnabled = config.getBoolean("messages.enabled", true);
        this.messagePrefix = config.getString("messages.prefix", "&8[&bActiveTime&8]&r ");
        this.freezeMessage = config.getString("messages.freeze", "&eServer simulation frozen because no players are online.");
        this.unfreezeMessage = config.getString("messages.unfreeze", "&aPlayer activity detected. Server simulation resumed.");

        logger.info("Configuration loaded (enabled=" + enabled + ", freeze-when-empty=" + freezeWhenEmpty + ")");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isFreezeWhenEmpty() {
        return freezeWhenEmpty;
    }

    public boolean isMessagesEnabled() {
        return messagesEnabled;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public String getFreezeMessage() {
        return freezeMessage;
    }

    public String getUnfreezeMessage() {
        return unfreezeMessage;
    }
}

