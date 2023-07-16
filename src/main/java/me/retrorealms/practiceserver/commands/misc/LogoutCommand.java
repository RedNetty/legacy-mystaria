package me.retrorealms.practiceserver.commands.misc;

import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.world.Logout;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LogoutCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("logout") && !Logout.logging.containsKey(p.getName())) {
                if (Alignments.isSafeZone(p.getLocation())) {
                    Alignments.tagged.remove(p.getName());
                    p.saveData();
                    p.sendMessage(ChatColor.GREEN.toString() + "You have safely logged out." + "\n\n" + ChatColor.GRAY + "Your player data has been synced.");
                } else if (Alignments.tagged.containsKey(p.getName()) && System.currentTimeMillis() - Alignments.tagged.get(p.getName()) < 10000) {
                    p.sendMessage(ChatColor.RED + "You will be " + ChatColor.BOLD + "LOGGED OUT" + ChatColor.RED + " of the game world shortly.");
                    Logout.logging.put(p.getName(), 10);
                    Logout.loggingloc.put(p.getName(), p.getLocation());
                } else {
                    p.sendMessage(ChatColor.RED + "You will be " + ChatColor.BOLD + "LOGGED OUT" + ChatColor.RED + " of the game world shortly.");
                    Logout.logging.put(p.getName(), 3);
                    Logout.loggingloc.put(p.getName(), p.getLocation());
                }
            }
        }
        return false;
    }
}
