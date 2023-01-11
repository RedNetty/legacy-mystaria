package me.retrorealms.practiceserver.commands.misc;

import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class FakeRollCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if(!ModerationMechanics.isStaff(p)) return false;
            if (args.length < 2 || args.length > 2) {
                p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Incorrect Syntax." + ChatColor.GRAY + " /fakeroll <1 - 10000> <1 - 10000>");
            } else if (args.length == 2) {
                int roll = 0;
                int max = 0;
                try {
                    max = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Non-Numeric Max Number. /fakeroll <1 - 10000> <1 - 10000>");
                    return true;
                }
                try {
                    roll = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Non-Numeric Roll Number. /fakeroll <1 - 10000> <1 - 10000>");
                    return true;
                }
                if (max < 1 || max > 10000) {
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Incorrect Syntax." + ChatColor.GRAY + " /fakeroll <1 - 10000> <1 - 10000>");
                } else {
                    p.sendMessage(String.valueOf(p.getDisplayName()) + ChatColor.GRAY + " has rolled a " + ChatColor.GRAY + ChatColor.BOLD + ChatColor.UNDERLINE + roll + ChatColor.GRAY + " out of " + ChatColor.GRAY + ChatColor.BOLD + ChatColor.UNDERLINE + max + ".");
                    ArrayList<Player> to_send = new ArrayList<Player>();
                    for (Player pl2 : p.getWorld().getPlayers()) {
                        if (pl2 == null || pl2 == p || pl2.getLocation().distance(p.getLocation()) >= 50.0) continue;
                        to_send.add(pl2);
                    }
                    if (to_send.size() > 0) {
                        for (Player pl2 : to_send) {
                            pl2.sendMessage(String.valueOf(p.getDisplayName()) + ChatColor.GRAY + " has rolled a " + ChatColor.GRAY + ChatColor.BOLD + ChatColor.UNDERLINE + roll + ChatColor.GRAY + " out of " + ChatColor.GRAY + ChatColor.BOLD + ChatColor.UNDERLINE + max + ".");
                        }
                    }
                }
            }
        }
        return false;
    }
}