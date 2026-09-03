package com.saiky.activetime.common;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Platform-independent configuration model for ActiveTime.
 */
public class ActiveTimeConfig {

    private boolean enabled = true;
    private boolean freezeWhenEmpty = true;
    private boolean messagesEnabled = true;
    private String messagePrefix = "&8[&bActiveTime&8]&r ";
    private String freezeMessage = "&eServer simulation frozen because no players are online.";
    private String unfreezeMessage = "&aPlayer activity detected. Server simulation resumed.";

    public ActiveTimeConfig() {
    }

    public ActiveTimeConfig(boolean enabled, boolean freezeWhenEmpty, boolean messagesEnabled,
                            String messagePrefix, String freezeMessage, String unfreezeMessage) {
        this.enabled = enabled;
        this.freezeWhenEmpty = freezeWhenEmpty;
        this.messagesEnabled = messagesEnabled;
        this.messagePrefix = messagePrefix;
        this.freezeMessage = freezeMessage;
        this.unfreezeMessage = unfreezeMessage;
    }

    /**
     * Loads or creates default configuration from a file path.
     */
    public void loadFromFile(Path configFile, Logger logger) {
        if (!Files.exists(configFile)) {
            try {
                if (configFile.getParent() != null) {
                    Files.createDirectories(configFile.getParent());
                }
                saveDefaultConfig(configFile);
            } catch (IOException e) {
                logger.warning("Could not create default config: " + e.getMessage());
            }
            return;
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(configFile);
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            props.load(reader);

            this.enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
            this.freezeWhenEmpty = Boolean.parseBoolean(props.getProperty("freeze.when-empty", "true"));
            this.messagesEnabled = Boolean.parseBoolean(props.getProperty("messages.enabled", "true"));
            this.messagePrefix = props.getProperty("messages.prefix", this.messagePrefix);
            this.freezeMessage = props.getProperty("messages.freeze", this.freezeMessage);
            this.unfreezeMessage = props.getProperty("messages.unfreeze", this.unfreezeMessage);

            logger.info("ActiveTime configuration loaded from " + configFile.getFileName());
        } catch (IOException e) {
            logger.warning("Error reading config file: " + e.getMessage());
        }
    }

    private void saveDefaultConfig(Path configFile) throws IOException {
        String defaultContent = "# ActiveTime Configuration\n"
                + "enabled=true\n"
                + "freeze.when-empty=true\n"
                + "messages.enabled=true\n"
                + "messages.prefix=&8[&bActiveTime&8]&r \n"
                + "messages.freeze=&eServer simulation frozen because no players are online.\n"
                + "messages.unfreeze=&aPlayer activity detected. Server simulation resumed.\n";
        Files.write(configFile, defaultContent.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFreezeWhenEmpty() {
        return freezeWhenEmpty;
    }

    public void setFreezeWhenEmpty(boolean freezeWhenEmpty) {
        this.freezeWhenEmpty = freezeWhenEmpty;
    }

    public boolean isMessagesEnabled() {
        return messagesEnabled;
    }

    public void setMessagesEnabled(boolean messagesEnabled) {
        this.messagesEnabled = messagesEnabled;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public void setMessagePrefix(String messagePrefix) {
        this.messagePrefix = messagePrefix;
    }

    public String getFreezeMessage() {
        return freezeMessage;
    }

    public void setFreezeMessage(String freezeMessage) {
        this.freezeMessage = freezeMessage;
    }

    public String getUnfreezeMessage() {
        return unfreezeMessage;
    }

    public void setUnfreezeMessage(String unfreezeMessage) {
        this.unfreezeMessage = unfreezeMessage;
    }
}
