package com.saiky.activetime.paper;

import com.saiky.activetime.common.ActiveTimeConfig;
import com.saiky.activetime.common.ActiveTimeController;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

/**
 * Main Paper plugin entrypoint.
 */
public class ActiveTimePaper extends JavaPlugin {

    private ActiveTimeConfig config;
    private PaperTickAdapter tickAdapter;
    private ActiveTimeController controller;

    @Override
    public void onEnable() {
        // 1. Load config
        loadConfiguration();

        // 2. Initialize tick adapter
        this.tickAdapter = new PaperTickAdapter(getServer(), getLogger());

        // 3. Message broadcaster
        Consumer<String> broadcaster = message -> {
            String colored = ChatColor.translateAlternateColorCodes('&', message);
            getServer().broadcastMessage(colored);
        };

        // 4. Initialize core controller
        this.controller = new ActiveTimeController(config, tickAdapter, getLogger(), broadcaster);

        // 5. Register listener
        getServer().getPluginManager().registerEvents(new PaperPlayerListener(controller), this);

        // 6. Register command
        PluginCommand cmd = getCommand("activetime");
        if (cmd != null) {
            PaperCommand handler = new PaperCommand(this, controller);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        // 7. Initial startup reconciliation
        int count = getServer().getOnlinePlayers().size();
        controller.reconcile(count);

        getLogger().info("ActiveTime (Paper) enabled. Automatic idle freeze: "
                + (config.isEnabled() && config.isFreezeWhenEmpty() ? "enabled" : "disabled") + ".");
    }

    @Override
    public void onDisable() {
        if (controller != null) {
            controller.onShutdown();
        }
        getLogger().info("ActiveTime (Paper) disabled.");
    }

    public void loadConfiguration() {
        saveDefaultConfig();
        reloadConfig();
        FileConfiguration c = getConfig();

        this.config = new ActiveTimeConfig(
                c.getBoolean("enabled", true),
                c.getBoolean("freeze.when-empty", true),
                c.getBoolean("messages.enabled", true),
                c.getString("messages.prefix", "&8[&bActiveTime&8]&r "),
                c.getString("messages.freeze", "&eServer simulation frozen because no players are online."),
                c.getString("messages.unfreeze", "&aPlayer activity detected. Server simulation resumed.")
        );
    }

    public ActiveTimeController getController() {
        return controller;
    }
}

