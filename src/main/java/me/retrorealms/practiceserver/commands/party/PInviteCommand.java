package me.retrorealms.practiceserver.commands.party;

import me.retrorealms.practiceserver.mechanics.party.Parties;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PInviteCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
                if (args.length == 1) {
                    final String player = args[0];
                    if (Bukkit.getPlayer(player) != null) {
                        Parties.inviteToParty(Bukkit.getPlayer(player), p);
                    } else {
                        p.sendMessage(String.valueOf(ChatColor.RED.toString()) + ChatColor.BOLD + player + ChatColor.RED + " is OFFLINE");
                    }
                } else {
                    p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Syntax. ").append(ChatColor.RED).append("/pinvite <player>").toString());
                    p.sendMessage(ChatColor.GRAY + "You can also " + ChatColor.UNDERLINE + "LEFT CLICK" + ChatColor.GRAY + " players with your " + ChatColor.ITALIC + "Character Journal" + ChatColor.GRAY + " to invite them.");
                }
            }
        return false;
    }
}
