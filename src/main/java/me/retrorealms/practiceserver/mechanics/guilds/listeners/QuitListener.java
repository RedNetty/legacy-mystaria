package me.retrorealms.practiceserver.mechanics.guilds.listeners;

import me.retrorealms.practiceserver.mechanics.guilds.guild.Guild;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildManager;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        GuildPlayer guildPlayer = GuildPlayers.getInstance().get(event.getPlayer().getUniqueId());
        if (guildPlayer != null && guildPlayer.isInGuild()) {
            Guild guild = GuildManager.getInstance().get(guildPlayer.getGuildName());
            guild.sendMessage(ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + guild.getTag() + ChatColor.DARK_AQUA + "> " + event.getPlayer().getName() + ChatColor.DARK_AQUA + " has logged out");
        }
        GuildPlayers.getInstance().save(GuildPlayers.getInstance().get(event.getPlayer().getUniqueId()));
    }
}
