package me.retrorealms.practiceserver.mechanics.guilds.listeners;

import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class JoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        GuildPlayers.getInstance().loadProfile(event.getPlayer());
    }
}
