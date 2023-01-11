package me.retrorealms.practiceserver.commands.misc;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// created by your boy Subby <----------- I MADE THIS :)    http://Subby.xyz
public class FeedCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        Player p = (Player) sender;
        if (ModerationMechanics.isDonator(p) || ModerationMechanics.isStaff(p)) {
            ItemStack wrapper = new ItemStack(Material.BREAD, 10, (short)6);
            p.getInventory().addItem(wrapper);
            p.sendMessage(ChatColor.GRAY + "You were given bread.");
        }
        else {
            p.sendMessage(ChatColor.RED + "This command can only be executed by Sub++ and above.");
        }
        return false;
    }
}