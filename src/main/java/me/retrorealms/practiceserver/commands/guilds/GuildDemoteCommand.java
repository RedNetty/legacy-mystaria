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

public class GuildDemoteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            GuildPlayer guildPlayer = GuildPlayers.getInstance().get(player.getUniqueId());
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Correct usage: /gdemote <name>");
                return true;
            }
            if (!guildPlayer.isInGuild()) {
                player.sendMessage(ChatColor.RED + "You're not in a guild!");
                return true;
            }
            Guild guild = GuildManager.getInstance().get(guildPlayer.getGuildName());
            Role role = guild.getRole(player.getUniqueId());
            if (role != null && (role == Role.LEADER)) {
                String name = args[0];
                if (name.equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You can't demote yourself.");
                    return true;
                }
                Player player1 = Bukkit.getPlayer(name);
                if (player1 != null && (player1.isOnline())) {
                    GuildPlayer guildPlayer1 = GuildPlayers.getInstance().get(player1.getUniqueId());
                    if (guildPlayer1.isInGuild() || (guildPlayer1.getGuildName().equals(guildPlayer.getGuildName()))) {
                        Role role1 = guild.getRole(guildPlayer1.getUuid());
                        if (role1 != null) {
                            if (role1 == Role.MEMBER) {
                                player.sendMessage(ChatColor.RED + "You cannot demote a member type /gkick <name>.");
                                return true;
                            } else if (role1 == Role.OFFICER && (role == Role.OFFICER)) {
                                player.sendMessage(ChatColor.RED + "You must be the guild " + ChatColor.BOLD + "OWNER" + ChatColor.RED + " to demote an OFFICER");
                                return true;
                            } else if (role1 == Role.OFFICER) {
                                player1.sendMessage(ChatColor.RED + "You have been " + ChatColor.BOLD + "DEMOTED" + ChatColor.RED + " to MEMBER by " + ChatColor.GRAY + player.getName());
                                guild.sendMessage(ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + guild.getTag() + ChatColor.DARK_AQUA + "> " + ChatColor.DARK_AQUA + player1.getName() + " has been " + ChatColor.UNDERLINE + "demoted" + ChatColor.DARK_AQUA + " to guild member by " + player.getName() + ".");
                                guild.setRole(player1.getUniqueId(), Role.MEMBER);
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + name + " isn't in you're guild.");
                            return true;
                        }
                    }
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + name + " is OFFLINE.");
                    return true;
                }
            } else {
                player.sendMessage(ChatColor.RED + "You must be at least a guild " + ChatColor.BOLD + "LEADER" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gdemote");
                return true;
            }
        }
        return true;
    }
}
