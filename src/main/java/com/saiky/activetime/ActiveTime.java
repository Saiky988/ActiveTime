package com.saiky.activetime;

import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

/**
 * Main plugin entrypoint for ActiveTime.
 * Your world only progresses while you play.
 */
public class ActiveTime extends JavaPlugin {

    private static ActiveTime instance;

    private ConfigManager configManager;
    private TickAdapter tickAdapter;
    private TimeController timeController;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Initialize and load configuration
        this.configManager = new ConfigManager(this);
        this.configManager.reload();

        // 2. Initialize native Paper tick adapter
        this.tickAdapter = new PaperTickAdapter(getServer(), getLogger());

        // 3. Setup broadcast messenger with color code translation
        Consumer<String> messageBroadcaster = message -> {
            String colored = ChatColor.translateAlternateColorCodes('&', message);
            getServer().broadcastMessage(colored);
        };

        // 4. Initialize time controller
        this.timeController = new TimeController(configManager, tickAdapter, getLogger(), messageBroadcaster);

        // 5. Register player listener
        getServer().getPluginManager().registerEvents(new PlayerListener(timeController), this);

        // 6. Register command executor and tab completer
        PluginCommand command = getCommand("activetime");
        if (command != null) {
            ActiveTimeCommand commandHandler = new ActiveTimeCommand(this, timeController, configManager);
            command.setExecutor(commandHandler);
            command.setTabCompleter(commandHandler);
        } else {
            getLogger().warning("Could not find 'activetime' command in plugin.yml!");
        }

        // 7. Initial startup reconciliation
        int onlinePlayers = getServer().getOnlinePlayers().size();
        timeController.reconcile(onlinePlayers);

        // 8. Log startup summary
        getLogger().info("ActiveTime enabled.");
        getLogger().info("Automatic idle freeze: " + (configManager.isEnabled() && configManager.isFreezeWhenEmpty() ? "enabled" : "disabled") + ".");
    }

    @Override
    public void onDisable() {
        if (timeController != null) {
            timeController.onShutdown();
        }
        getLogger().info("ActiveTime disabled.");
        instance = null;
    }

    public static ActiveTime getInstance() {
        return instance;
    }

    public TimeController getTimeController() {
        return timeController;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TickAdapter getTickAdapter() {
        return tickAdapter;
    }
}

