package com.saiky.activetime.paper;

import com.saiky.activetime.common.ActiveTimeConfig;
import com.saiky.activetime.common.ActiveTimeController;
import com.saiky.activetime.common.ActiveTimeState;
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
 * Handles /activetime commands on Paper.
 */
public class PaperCommand implements CommandExecutor, TabCompleter {

    private static final String ADMIN_PERMISSION = "activetime.admin";

    private final ActiveTimePaper plugin;
    private final ActiveTimeController controller;

    public PaperCommand(ActiveTimePaper plugin, ActiveTimeController controller) {
        this.plugin = plugin;
        this.controller = controller;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendSummary(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
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
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /" + label + " [status|freeze|unfreeze|reload]");
                return true;
        }
    }

    private void sendSummary(CommandSender sender) {
        int count = plugin.getServer().getOnlinePlayers().size();
        ActiveTimeState state = controller.getState();
        String color = state == ActiveTimeState.RUNNING ? ChatColor.GREEN.toString() : ChatColor.GOLD.toString();
        ActiveTimeConfig config = controller.getConfig();
        String auto = config.isEnabled() && config.isFreezeWhenEmpty()
                ? ChatColor.GREEN + "ENABLED"
                : ChatColor.RED + "DISABLED";

        sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "ActiveTime");
        sender.sendMessage(ChatColor.GRAY + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "State: " + color + state.name());
        sender.sendMessage(ChatColor.GRAY + "Players online: " + ChatColor.WHITE + count);
        sender.sendMessage(ChatColor.GRAY + "Auto-freeze: " + auto);
    }

    private void sendStatus(CommandSender sender) {
        int count = plugin.getServer().getOnlinePlayers().size();
        ActiveTimeState state = controller.getState();
        String color = state == ActiveTimeState.RUNNING ? ChatColor.GREEN.toString() : ChatColor.GOLD.toString();
        ActiveTimeConfig config = controller.getConfig();

        sender.sendMessage(ChatColor.DARK_AQUA + "=== " + ChatColor.AQUA + "ActiveTime Status" + ChatColor.DARK_AQUA + " ===");
        sender.sendMessage(ChatColor.GRAY + "Plugin Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "State: " + color + state.name());
        sender.sendMessage(ChatColor.GRAY + "Native Tick Frozen: " + ChatColor.WHITE + controller.isFrozen());
        sender.sendMessage(ChatColor.GRAY + "Frozen by ActiveTime: " + ChatColor.WHITE + controller.isFrozenByActiveTime());
        sender.sendMessage(ChatColor.GRAY + "Online Players: " + ChatColor.WHITE + count);
        sender.sendMessage(ChatColor.GRAY + "Master Enabled: " + (config.isEnabled() ? ChatColor.GREEN + "true" : ChatColor.RED + "false"));
        sender.sendMessage(ChatColor.GRAY + "Freeze When Empty: " + (config.isFreezeWhenEmpty() ? ChatColor.GREEN + "true" : ChatColor.RED + "false"));
    }

    private void handleFreeze(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to manually freeze simulation.");
            return;
        }

        if (controller.isFrozen()) {
            sender.sendMessage(ChatColor.YELLOW + "Server simulation is already frozen.");
            return;
        }

        controller.freeze(true);
        sender.sendMessage(ChatColor.GREEN + "Server simulation frozen manually.");
    }

    private void handleUnfreeze(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to manually unfreeze simulation.");
            return;
        }

        if (!controller.isFrozen()) {
            sender.sendMessage(ChatColor.YELLOW + "Server simulation is already running.");
            return;
        }

        controller.unfreeze(true);
        sender.sendMessage(ChatColor.GREEN + "Server simulation resumed manually.");
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to reload configuration.");
            return;
        }

        plugin.loadConfiguration();
        int count = plugin.getServer().getOnlinePlayers().size();
        controller.reconcile(count);

        sender.sendMessage(ChatColor.GREEN + "ActiveTime configuration reloaded and state reconciled.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("status");
            if (sender.hasPermission(ADMIN_PERMISSION)) {
                list.add("freeze");
                list.add("unfreeze");
                list.add("reload");
            }
            String cur = args[0].toLowerCase(Locale.ROOT);
            List<String> res = new ArrayList<>();
            for (String s : list) {
                if (s.startsWith(cur)) {
                    res.add(s);
                }
            }
            return res;
        }
        return Collections.emptyList();
    }
}

