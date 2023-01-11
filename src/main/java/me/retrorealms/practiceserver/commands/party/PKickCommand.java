package me.retrorealms.practiceserver.commands.party;

import me.retrorealms.practiceserver.mechanics.party.Parties;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PKickCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
            if (args.length == 1) {
                final String player = args[0];
                if (!Parties.isPartyLeader(p)) {
                    p.sendMessage(String.valueOf(ChatColor.RED.toString()) + "You are NOT the leader of your party.");
                    p.sendMessage(String.valueOf(ChatColor.GRAY.toString()) + "Type " + ChatColor.BOLD.toString() + "/pquit" + ChatColor.GRAY + " to quit your current party.");
                    return true;
                }
                if (Bukkit.getPlayer(player) == null) {
                    p.sendMessage(String.valueOf(ChatColor.RED.toString()) + ChatColor.BOLD + player + " is not in your party.");
                    return true;
                }
                if (Parties.getParty(Bukkit.getPlayer(player)) != p) {
                    p.sendMessage(String.valueOf(ChatColor.RED.toString()) + ChatColor.BOLD + player + " is not in your party.");
                    return true;
                }
                Bukkit.getPlayer(player).sendMessage(String.valueOf(ChatColor.RED.toString()) + ChatColor.BOLD.toString() + "You have been kicked out of the party.");
                Parties.removePlayer(Bukkit.getPlayer(player));
            } else {
                p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Syntax. ").append(ChatColor.RED).append("/pkick <player>").toString());
            }
        }
        return false;
    }
}