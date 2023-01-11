package me.retrorealms.practiceserver.commands.guilds;

import me.retrorealms.practiceserver.mechanics.guilds.guild.Guild;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildManager;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Matthew E on 8/7/2017.
 */
public class GuildCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            GuildPlayer guildPlayer = GuildPlayers.getInstance().get(player.getUniqueId());
            if (!guildPlayer.isInGuild()) {
                player.sendMessage(ChatColor.RED + "You're not in a guild.");
                return true;
            }
            Guild guild = GuildManager.getInstance().get(guildPlayer.getGuildName());
            String message = "";
            if (args.length > 0) {
                for (String arg : args) {
                    message += arg + " ";
                }
            } else {
                player.sendMessage(ChatColor.RED + "Usage: /g <message>");
                return true;
            }
            if (message.length() > 3) {
                message = message.substring(0, message.length() - 1);
            }
            //GuildChat.sendCrossShardMessage(player, guild, message);
            guild.sendMessage(ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + guild.getTag() + ChatColor.DARK_AQUA + "> " + player.getName() + ChatColor.GRAY + ": " + message);
            return true;
        }
        return true;
    }
}
