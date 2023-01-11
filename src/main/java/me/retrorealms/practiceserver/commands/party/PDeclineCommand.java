package me.retrorealms.practiceserver.commands.party;

import me.retrorealms.practiceserver.mechanics.party.Parties;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PDeclineCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("pdecline")) {
                if (args.length != 0) {
                    p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Syntax. ").append(ChatColor.RED).append("/pdecline").toString());
                    return true;
                }
                if (!Parties.invite.containsKey(p)) {
                    p.sendMessage(ChatColor.RED + "No pending party invites.");
                    return true;
                }
                p.sendMessage(ChatColor.RED + "Declined " + ChatColor.BOLD + Parties.invite.get(p).getName() + "'s" + ChatColor.RED + " party invitation.");
                if (Parties.invite.get(p).isOnline()) {
                    Parties.invite.get(p).sendMessage(String.valueOf(ChatColor.RED.toString()) + ChatColor.BOLD + p.getName() + ChatColor.RED.toString() + " has " + ChatColor.UNDERLINE + "DECLINED" + ChatColor.RED + " your party invitation.");
                }
                Parties.invite.remove(p);
                Parties.invitetime.remove(p);
            }
        }
        return true;
    }
}
