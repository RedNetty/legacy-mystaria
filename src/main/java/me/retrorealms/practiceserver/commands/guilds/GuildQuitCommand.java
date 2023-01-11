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

public class GuildQuitCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            GuildPlayer guildPlayer = GuildPlayers.getInstance().get(player.getUniqueId());
            if (!guildPlayer.isInGuild()) {
                player.sendMessage(ChatColor.RED + "You're not in a guild!");
                return true;
            }
            Guild guild = GuildManager.getInstance().get(guildPlayer.getGuildName());
            player.sendMessage(ChatColor.GRAY + "Are you sure you want to QUIT the guild '" + ChatColor.DARK_AQUA + guild.getName() + ChatColor.GRAY + "' - This cannot be undone. " + "(" + ChatColor.GREEN.toString() + ChatColor.BOLD + "Y" + ChatColor.GRAY + " / " + ChatColor.RED.toString() + ChatColor.BOLD + "N" + ChatColor.GRAY + ")");
            if (guild.getOwner().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.GRAY + "You are the " + ChatColor.UNDERLINE + "GUILD LEADER" + ChatColor.GRAY + ", if you leave this guild it will be " + ChatColor.BOLD + "PERMANENTLY DELETED" + ChatColor.GRAY + ". All members will be kicked, and you will lose your 5,000g deposit.");
            }
            GuildPlayers.getInstance().addGuildQuitPrompt(player.getUniqueId());

            return true;
        }
        return true;
    }
}
