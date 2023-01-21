package me.retrorealms.practiceserver.commands.party;

import me.retrorealms.practiceserver.mechanics.party.Parties;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PAcceptCommand implements CommandExecutor {
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
                if (args.length == 0) {
                    if (!Parties.invite.containsKey(p)) {
                        p.sendMessage(ChatColor.RED + "No pending party invites.");
                        return true;
                    }
                    final Player leader = Parties.getParty(Parties.invite.get(p));
                    if (Parties.invite.get(p) == null || leader == null) {
                        p.sendMessage(ChatColor.RED + "This party invite is no longer available.");
                        Parties.invite.remove(p);
                        Parties.invite.remove(p);
                        return true;
                    }
                    if (Parties.getParties().get(leader).size() == Parties.maxSize) {
                        p.sendMessage(ChatColor.RED + "This party is currently full.");
                        Parties.invite.remove(p);
                        Parties.invite.remove(p);
                        return true;
                    }
                    final List<Player> mem = Parties.getParties().get(leader);
                    for (final Player pl : mem) {
                        pl.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE.toString()) + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + ">" + ChatColor.GRAY + " " + p.getName() + ChatColor.GRAY.toString() + " has " + ChatColor.LIGHT_PURPLE + ChatColor.UNDERLINE + "joined" + ChatColor.GRAY + " your party.");
                    }
                    Parties.addPlayer(p, leader);
                    p.sendMessage("");
                    p.sendMessage(ChatColor.LIGHT_PURPLE + "You have joined " + ChatColor.BOLD + leader.getName() + "'s" + ChatColor.LIGHT_PURPLE + " party.");
                    p.sendMessage(ChatColor.GRAY + "To chat with your party, use " + ChatColor.BOLD + "/p" + ChatColor.GRAY + " OR " + ChatColor.BOLD + " /p <message>");
                    Parties.invite.remove(p);
                    Parties.invitetime.remove(p);
                    return true;
                } else {
                    p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Syntax. ").append(ChatColor.RED).append("/paccept").toString());
                }
            }
        return false;
    }
}
