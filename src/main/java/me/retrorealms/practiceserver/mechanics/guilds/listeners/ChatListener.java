package me.retrorealms.practiceserver.mechanics.guilds.listeners;

import me.retrorealms.practiceserver.mechanics.guilds.guild.Guild;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildManager;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

/**
 * Created by Matthew E on 8/7/2017.
 */
public class ChatListener implements Listener {
    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GuildPlayer guildPlayer = GuildPlayers.getInstance().get(player.getUniqueId());
        if (GuildPlayers.getInstance().isGuildQuitPrompt(player.getUniqueId())) {
            event.setCancelled(true);
            if (!guildPlayer.isInGuild()) {
                GuildPlayers.getInstance().removeGuildQuitPrompt(player.getUniqueId());
                return;
            }
            Guild guild = GuildManager.getInstance().get(guildPlayer.getGuildName());
            String message = event.getMessage().toLowerCase();
            if (message.equalsIgnoreCase("y") || (message.equalsIgnoreCase("yes") || (message.equalsIgnoreCase("confirm")))) {
                player.sendMessage(ChatColor.RED + "You have " + ChatColor.BOLD + "QUIT" + ChatColor.RED + " your guild.");
                for (UUID uuid : guild.getOnlineList()) {
                    Player player1 = Bukkit.getPlayer(uuid);
                    if ((player1 != null) && (player1.isOnline())) {
                        player1.sendMessage(ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + guild.getTag() + ChatColor.DARK_AQUA + "> " + ChatColor.DARK_AQUA + player.getName() + ChatColor.GRAY + " has " + ChatColor.UNDERLINE + "left" + ChatColor.GRAY + " the guild.");
                    }
                }
                guild.removePlayer(player);
                if (guild.getOwner().equals(player.getUniqueId())) {
                    GuildManager.getInstance().disbandGuild(guild);
                }
                GuildPlayers.getInstance().removeGuildQuitPrompt(player.getUniqueId());
            } else {
                GuildPlayers.getInstance().removeGuildQuitPrompt(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "/gquit - " + ChatColor.BOLD + "CANCELLED");
            }
        }
    }
}
