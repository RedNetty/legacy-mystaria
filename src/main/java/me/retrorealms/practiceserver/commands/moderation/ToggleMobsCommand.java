package me.retrorealms.practiceserver.commands.moderation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ToggleMobsCommand implements CommandExecutor {
    public static boolean togglespawners = true;

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player && !sender.isOp()) return false;
        for (LivingEntity l : Bukkit.getServer().getWorld("jew").getLivingEntities()) {
            if (!(l instanceof Player)) {
                l.remove();
            }
        }
        togglespawners = !togglespawners;
        if(togglespawners) sender.sendMessage(ChatColor.GREEN + "Spawners have been enabled.");
        if(!togglespawners) sender.sendMessage(ChatColor.RED + "Spawners have been disabled.");
        return true;
    }
}
