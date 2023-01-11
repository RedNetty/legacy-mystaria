package me.retrorealms.practiceserver.commands.guilds;

import me.retrorealms.practiceserver.mechanics.guilds.guild.Guild;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildBank;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildManager;
import me.retrorealms.practiceserver.mechanics.guilds.guild.Role;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuildKickCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            GuildPlayer guildPlayer = GuildPlayers.getInstance().get(player.getUniqueId());
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Correct usage: /gkick <name>");
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
                if (name.equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You can't kick yourself type /gquit");
                    return true;
                }
                Player player1 = Bukkit.getPlayer(name);
                if (player1 != null && (player1.isOnline())) {
                    GuildPlayer guildPlayer1 = GuildPlayers.getInstance().get(player1.getUniqueId());
                    if (guildPlayer1.isInGuild() || (guildPlayer1.getGuildName().equals(guildPlayer.getGuildName()))) {
                        Role role1 = guild.getRole(guildPlayer1.getUuid());
                        if (role1 != null) {
                            if (role1 == Role.LEADER) {
                                player.sendMessage(ChatColor.RED + "You cannot gkick the leader.");
                                return true;
                            } else if (role1 == Role.OFFICER && (role == Role.OFFICER)) {
                                player.sendMessage(ChatColor.RED + "You must be the guild " + ChatColor.BOLD + "OWNER" + ChatColor.RED + " to kick an OFFICER");
                                return true;
                            } else if(GuildBank.banksee.containsValue(player1)) {
                                player.sendMessage(ChatColor.RED + "Wait for this player to exit the guild bank before kicking them.");
                                return true;
                            }else if (role1 == Role.MEMBER) {
                                player1.sendMessage(ChatColor.RED + "You have been " + ChatColor.BOLD + "KICKED" + ChatColor.RED + " out of your guild.");
                                player1.sendMessage(ChatColor.GRAY + "Kicked by: " + player.getName());
                                guild.sendMessage(ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + guild.getTag() + ChatColor.DARK_AQUA + "> " + ChatColor.DARK_AQUA + player1.getName() + " has been " + ChatColor.UNDERLINE + "kicked" + ChatColor.DARK_AQUA + " by " + player.getName() + ".");
                                guild.removePlayer(player1);
                                return true;
                            } else if (role1 == Role.OFFICER && (role == Role.LEADER)) {
                                player1.sendMessage(ChatColor.RED + "You have been " + ChatColor.BOLD + "KICKED" + ChatColor.RED + " out of your guild.");
                                player1.sendMessage(ChatColor.GRAY + "Kicked by: " + player.getName());
                                guild.sendMessage(ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + guild.getTag() + ChatColor.DARK_AQUA + "> " + ChatColor.BOLD + "* " + ChatColor.DARK_AQUA + player1.getName() + " has been " + ChatColor.UNDERLINE + "kicked" + ChatColor.DARK_AQUA + " by " + player.getName() + ".");
                                guild.removePlayer(player1);
                                guild.save();
                                SQLMain.updateGuild(guildPlayer.getUuid(), guildPlayer.getGuildName());
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
                player.sendMessage(ChatColor.RED + "You must be at least a guild " + ChatColor.BOLD + "OFFICER" + ChatColor.RED + " to use " + ChatColor.BOLD + "/gkick");
                return true;
            }
        }
        return true;
    }
}
