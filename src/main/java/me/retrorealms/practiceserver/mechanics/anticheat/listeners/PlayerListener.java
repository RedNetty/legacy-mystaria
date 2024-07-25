package me.retrorealms.practiceserver.mechanics.anticheat.listeners;

import me.retrorealms.practiceserver.mechanics.anticheat.ACPlayerData;
import me.retrorealms.practiceserver.mechanics.anticheat.AdvancedAntiCheat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {
    private final AdvancedAntiCheat antiCheat;

    public PlayerListener(AdvancedAntiCheat antiCheat) {
        this.antiCheat = antiCheat;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        antiCheat.getPlayerData(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        antiCheat.removePlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        ACPlayerData data = antiCheat.getPlayerData(player);
        data.setLastTeleportTime(System.currentTimeMillis());
        data.setLastTeleportLocation(event.getTo());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        antiCheat.getCheckManager().runChecks(event, event.getPlayer());
    }
}
