package com.saiky.activetime;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens to player presence events and notifies the TimeController.
 */
public class PlayerListener implements Listener {

    private final TimeController timeController;

    public PlayerListener(TimeController timeController) {
        this.timeController = timeController;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Player is online; calculate current player count
        int count = event.getPlayer().getServer().getOnlinePlayers().size();
        timeController.reconcile(count);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // At the time PlayerQuitEvent fires, the quitting player is still included in getOnlinePlayers()
        int remainingCount = Math.max(0, event.getPlayer().getServer().getOnlinePlayers().size() - 1);
        timeController.reconcile(remainingCount);
    }
}

