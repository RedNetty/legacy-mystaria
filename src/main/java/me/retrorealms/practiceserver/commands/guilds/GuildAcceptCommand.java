package me.retrorealms.practiceserver.commands.guilds;

import me.retrorealms.practiceserver.mechanics.guilds.guild.Guild;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildManager;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuildAcceptCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            GuildPlayer guildPlayer = GuildPlayers.getInstance().get(player.getUniqueId());
            if (guildPlayer.isInGuild()) {
                player.sendMessage(ChatColor.RED + "You're already in a guild.");
                return true;
            }
            if ((guildPlayer.getGuildInviteName() != null) && (guildPlayer.getGuildInviteTime() > 0)) {
                if (!GuildManager.getInstance().isGuild(guildPlayer.getGuildInviteName())) {
                    player.sendMessage(ChatColor.RED + "No pending guilds invites.");
                    guildPlayer.setGuildInviteTime(0);
                    guildPlayer.setGuildInviteName(null);
                    return true;
                } else {
                    Guild guild = GuildManager.getInstance().get(guildPlayer.getGuildInviteName());
                    guildPlayer.setGuildInviteTime(0);
                    guildPlayer.setGuildInviteName(null);
                    guild.addOnline(player.getUniqueId());
                    guild.addPlayer(player);
                    guildPlayer.setGuildName(guild.getName());
                    guild.save();
                    SQLMain.updateGuild(guildPlayer.getUuid(), guildPlayer.getGuildName());
                    return true;
                }
            } else {
                player.sendMessage(ChatColor.RED + "No pending guilds invites.");
                return true;
            }
        }
        return true;
    }
}
