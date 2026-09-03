package com.saiky.activetime;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Handles /activetime commands and tab completion.
 */
public class ActiveTimeCommand implements CommandExecutor, TabCompleter {

    private static final String ADMIN_PERMISSION = "activetime.admin";

    private final ActiveTime plugin;
    private final TimeController timeController;
    private final ConfigManager configManager;

    public ActiveTimeCommand(ActiveTime plugin, TimeController timeController, ConfigManager configManager) {
        this.plugin = plugin;
        this.timeController = timeController;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendSummary(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);
        switch (subCommand) {
            case "status":
                sendStatus(sender);
                return true;

            case "freeze":
                handleFreeze(sender);
                return true;

            case "unfreeze":
                handleUnfreeze(sender);
                return true;

            case "reload":
                handleReload(sender);
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown sub-command. Usage: /" + label + " [status|freeze|unfreeze|reload]");
                return true;
        }
    }

    private void sendSummary(CommandSender sender) {
        int onlinePlayers = plugin.getServer().getOnlinePlayers().size();
        SimulationState state = timeController.getState();
        String stateColor = state == SimulationState.RUNNING ? ChatColor.GREEN.toString() : ChatColor.GOLD.toString();
        String autoFreeze = configManager.isEnabled() && configManager.isFreezeWhenEmpty()
                ? ChatColor.GREEN + "ENABLED"
                : ChatColor.RED + "DISABLED";

        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "ActiveTime");
        sender.sendMessage(ChatColor.GRAY + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "State: " + stateColor + state.name());
        sender.sendMessage(ChatColor.GRAY + "Players online: " + ChatColor.WHITE + onlinePlayers);
        sender.sendMessage(ChatColor.GRAY + "Auto-freeze: " + autoFreeze);
    }

    private void sendStatus(CommandSender sender) {
        int onlinePlayers = plugin.getServer().getOnlinePlayers().size();
        SimulationState state = timeController.getState();
        String stateColor = state == SimulationState.RUNNING ? ChatColor.GREEN.toString() : ChatColor.GOLD.toString();

        sender.sendMessage(ChatColor.DARK_AQUA + "=== " + ChatColor.AQUA + "ActiveTime Status" + ChatColor.DARK_AQUA + " ===");
        sender.sendMessage(ChatColor.GRAY + "Plugin Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "Simulation State: " + stateColor + state.name());
        sender.sendMessage(ChatColor.GRAY + "Native Tick Frozen: " + ChatColor.WHITE + timeController.isFrozen());
        sender.sendMessage(ChatColor.GRAY + "Frozen by ActiveTime: " + ChatColor.WHITE + timeController.isFrozenByActiveTime());
        sender.sendMessage(ChatColor.GRAY + "Online Players: " + ChatColor.WHITE + onlinePlayers);
        sender.sendMessage(ChatColor.GRAY + "Master Enabled: " + (configManager.isEnabled() ? ChatColor.GREEN + "true" : ChatColor.RED + "false"));
        sender.sendMessage(ChatColor.GRAY + "Freeze When Empty: " + (configManager.isFreezeWhenEmpty() ? ChatColor.GREEN + "true" : ChatColor.RED + "false"));
        sender.sendMessage(ChatColor.GRAY + "Messages Enabled: " + (configManager.isMessagesEnabled() ? ChatColor.GREEN + "true" : ChatColor.RED + "false"));
    }

    private void handleFreeze(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to manually freeze simulation.");
            return;
        }

        if (timeController.isFrozen()) {
            sender.sendMessage(ChatColor.YELLOW + "Server simulation is already frozen.");
            return;
        }

        boolean changed = timeController.freeze(true);
        if (changed) {
            sender.sendMessage(ChatColor.GREEN + "Server simulation frozen manually.");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Server simulation was already frozen.");
        }
    }

    private void handleUnfreeze(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to manually unfreeze simulation.");
            return;
        }

        if (!timeController.isFrozen()) {
            sender.sendMessage(ChatColor.YELLOW + "Server simulation is already running.");
            return;
        }

        boolean changed = timeController.unfreeze(true);
        if (changed) {
            sender.sendMessage(ChatColor.GREEN + "Server simulation resumed manually.");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Server simulation was already running.");
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to reload configuration.");
            return;
        }

        configManager.reload();
        int onlinePlayers = plugin.getServer().getOnlinePlayers().size();
        timeController.reconcile(onlinePlayers);

        sender.sendMessage(ChatColor.GREEN + "ActiveTime configuration reloaded and state reconciled.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("status");

            if (sender.hasPermission(ADMIN_PERMISSION)) {
                completions.add("freeze");
                completions.add("unfreeze");
                completions.add("reload");
            }

            String current = args[0].toLowerCase(Locale.ROOT);
            List<String> matched = new ArrayList<>();
            for (String sub : completions) {
                if (sub.startsWith(current)) {
                    matched.add(sub);
                }
            }
            return matched;
        }

        return Collections.emptyList();
    }
}

