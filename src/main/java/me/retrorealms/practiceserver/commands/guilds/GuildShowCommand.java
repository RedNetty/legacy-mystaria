package me.retrorealms.practiceserver.commands.guilds;

import me.retrorealms.practiceserver.mechanics.guilds.guild.Guild;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildManager;
import me.retrorealms.practiceserver.mechanics.guilds.guild.Role;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Matthew E on 8/4/2017.
 */
public class GuildShowCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                GuildPlayer guildPlayer = GuildPlayers.getInstance().get(player.getUniqueId());
                if (guildPlayer.getGuildName() == null) {
                    player.sendMessage(ChatColor.RED + "You're not in a guild!");
                    return true;
                }
                Guild guild = GuildManager.getInstance().get(guildPlayer.getGuildName());
                sendGuildInfo(player, guild);
            } else if (args.length == 1) {
                if (Bukkit.getPlayer(args[0]) != null) {
                    Player player1 = Bukkit.getPlayer(args[0]);
                    GuildPlayer guildPlayer = GuildPlayers.getInstance().get(player1.getUniqueId());
                    if (guildPlayer != null) {
                        if (guildPlayer.getGuildName() == null) {
                            player.sendMessage(ChatColor.RED + args[0] + " isn't in a guild.");
                            return true;
                        }
                        Guild guild = GuildManager.getInstance().getIgnoreCase(guildPlayer.getGuildName());
                        sendGuildInfo(player, guild);
                    }
                    return true;
                }
                String guildName = args[0];
                Guild guild = GuildManager.getInstance().getIgnoreCase(guildName);
                if (guild == null) {
                    player.sendMessage(ChatColor.RED + "The guild " + guildName + " doesn't exist.");
                    return true;
                }
                sendGuildInfo(player, guild);
                return true;
            }
        }
        return true;
    }

    private void sendGuildInfo(Player player, Guild guild) {
        player.sendMessage(ChatColor.DARK_AQUA + "----------------[" + ChatColor.AQUA + guild.getName() + ChatColor.DARK_AQUA + "]----------------");
        player.sendMessage(ChatColor.DARK_AQUA + "Tag: " + ChatColor.WHITE + "[" + guild.getTag() + "]");
        player.sendMessage(ChatColor.DARK_AQUA + "Name: " + ChatColor.AQUA + guild.getName());
        String onlineString = "";
        String officerString = "";
        for (UUID uuid : guild.getOnlineList()) {
            String playerString = guild.getPlayerString(uuid);
            if (guild.getPlayerRoleMap().get(uuid) == Role.OFFICER) {
                officerString += playerString + ChatColor.AQUA + ", ";
            } else if (guild.getPlayerRoleMap().get(uuid) == Role.MEMBER) {
                onlineString += playerString + ChatColor.AQUA + ", ";

            }
        }
        if (onlineString.length() > 3) {
            onlineString = onlineString.substring(0, onlineString.length() - 2);
        }
        if (officerString.length() > 3) {
            officerString = officerString.substring(0, officerString.length() - 2);
        }
        player.sendMessage(ChatColor.DARK_AQUA + "Leader: " + ChatColor.AQUA + Bukkit.getOfflinePlayer(guild.getOwner()).getName());
        player.sendMessage(ChatColor.DARK_AQUA + "Online Officers: " + officerString);
        player.sendMessage(ChatColor.DARK_AQUA + "Online Members: " + onlineString);
        player.sendMessage(ChatColor.DARK_AQUA + "----------------[" + ChatColor.AQUA + guild.getName() + ChatColor.DARK_AQUA + "]----------------");
    }
}
