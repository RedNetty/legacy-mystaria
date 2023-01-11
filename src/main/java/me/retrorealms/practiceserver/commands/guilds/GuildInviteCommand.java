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

import java.util.concurrent.TimeUnit;

public class GuildInviteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            GuildPlayer guildPlayer = GuildPlayers.getInstance().get(player.getUniqueId());
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Correct usage: /ginvite <name>");
                return true;
            }
            if (!guildPlayer.isInGuild()) {
                player.sendMessage(ChatColor.RED + "You're not in a guild!");
                return true;
            }
            Guild guild = GuildManager.getInstance().get(guildPlayer.getGuildName());
            Role role = guild.getRole(player.getUniqueId());
            if (role != null && (role != Role.MEMBER)) {
                String name = args[0];
                Player player1 = Bukkit.getPlayer(name);
                if (player1 != null && (player1.isOnline())) {
                    GuildPlayer guildPlayer1 = GuildPlayers.getInstance().get(player1.getUniqueId());
                    if (guildPlayer1.isInGuild()) {
                        player.sendMessage(ChatColor.RED + name + " is already in a guild!");
                        return true;
                    }
                    if (guildPlayer1.hasPendingInvite()) {
                        player.sendMessage(ChatColor.RED + player1.getName() + " has a pending guild invite.");
                        return true;
                    }
                    guildPlayer1.setGuildInviteName(guild.getName());
                    guildPlayer1.setGuildInviteTime(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(15));
                    player1.sendMessage("");
                    player1.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + player.getName() + ChatColor.GRAY + " has invited you to join their guild, " + ChatColor.DARK_AQUA + guild.getName() + ChatColor.GRAY + ". To accept, type " + ChatColor.DARK_AQUA.toString() + "/gaccept" + ChatColor.GRAY + " to decline, type " + ChatColor.DARK_AQUA.toString() + "/gdecline");
                    player1.sendMessage("");
                    player.sendMessage(ChatColor.GRAY + "You have invited " + ChatColor.BOLD.toString() + ChatColor.DARK_AQUA + player1.getName() + ChatColor.GRAY + " to join your guild.");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + name + " is OFFLINE.");
                    return true;
                }
            } else {
                player.sendMessage(ChatColor.RED + "You must be at least a guild " + ChatColor.BOLD + "OFFICER" + ChatColor.RED + " to use " + ChatColor.BOLD + "/ginvite");
                return true;
            }
        }
        return true;
    }
}
