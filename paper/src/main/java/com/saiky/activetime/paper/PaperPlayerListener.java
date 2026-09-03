package com.saiky.activetime.paper;

import com.saiky.activetime.common.ActiveTimeController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Paper listener for player join and quit events.
 */
public class PaperPlayerListener implements Listener {

    private final ActiveTimeController controller;

    public PaperPlayerListener(ActiveTimeController controller) {
        this.controller = controller;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        int count = event.getPlayer().getServer().getOnlinePlayers().size();
        controller.reconcile(count);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        int remaining = Math.max(0, event.getPlayer().getServer().getOnlinePlayers().size() - 1);
        controller.reconcile(remaining);
    }
}
