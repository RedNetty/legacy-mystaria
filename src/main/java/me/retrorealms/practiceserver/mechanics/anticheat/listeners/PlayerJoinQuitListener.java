package me.retrorealms.practiceserver.mechanics.anticheat.listeners;

import me.retrorealms.practiceserver.mechanics.anticheat.AdvancedAntiCheat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuitListener implements Listener {
    private final AdvancedAntiCheat antiCheat;

    public PlayerJoinQuitListener(AdvancedAntiCheat antiCheat) {
        this.antiCheat = antiCheat;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        antiCheat.getPacketAnalyzer().injectPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        antiCheat.getPacketAnalyzer().uninjectPlayer(event.getPlayer());
    }
}