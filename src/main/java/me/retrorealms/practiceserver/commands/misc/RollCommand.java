package me.retrorealms.practiceserver.commands.misc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Random;

public class RollCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length < 1 || args.length > 1) {
                p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Incorrect Syntax." + ChatColor.GRAY + " /roll <1 - 10000>");
            } else if (args.length == 1) {
                int max = 0;
                try {
                    max = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Non-Numeric Max Number. /roll <1 - 10000>");
                    return true;
                }
                if (max < 1 || max > 10000) {
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Incorrect Syntax." + ChatColor.GRAY + " /roll <1 - 10000>");
                } else {
                    Random random = new Random();
                    int roll = random.nextInt(max + 1);
                    p.sendMessage(String.valueOf(p.getDisplayName()) + ChatColor.GRAY + " has rolled a " + ChatColor.GRAY + ChatColor.BOLD + ChatColor.UNDERLINE + roll + ChatColor.GRAY + " out of " + ChatColor.GRAY + ChatColor.BOLD + ChatColor.UNDERLINE + max + ".");
                    ArrayList<Player> to_send = new ArrayList<Player>();
                    for (Player pl2 : p.getWorld().getPlayers()) {
                        if (pl2 == null || pl2 == p || pl2.getLocation().distance(p.getLocation()) >= 50.0)
                            continue;
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
