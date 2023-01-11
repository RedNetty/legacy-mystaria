package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.mechanics.loot.LootChests;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HideLootCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p;
        if (sender instanceof Player && (p = (Player) sender).isOp()) {
            World w;
            int z;
            int k;
            int count;
            int y;
            Location loc;
            int radius;
            int x;
            int j;
            int i;
            if (cmd.getName().equalsIgnoreCase("hideloot")) {
                if (args.length != 1) {
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Incorrect Syntax. " + ChatColor.RED + "/hideloot <radius>");
                    return true;
                }
                try {
                    radius = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    radius = 0;
                }
                loc = p.getLocation();
                w = loc.getWorld();
                x = (int) loc.getX();
                y = (int) loc.getY();
                z = (int) loc.getZ();
                count = 0;
                i = -radius;
                while (i <= radius) {
                    j = -radius;
                    while (j <= radius) {
                        k = -radius;
                        while (k <= radius) {
                            loc = w.getBlockAt(x + i, y + j, z + k).getLocation();
                            if (LootChests.loot.containsKey(loc) && loc.getBlock().getType() == Material.GLOWSTONE) {
                                loc.getBlock().setType(Material.AIR);
                                ++count;
                            }
                            ++k;
                        }
                        ++j;
                    }
                    ++i;
                }
                p.sendMessage(ChatColor.YELLOW + "Hiding " + count + " loot chests in a " + radius + " block radius...");
            }
        }
        return false;
    }
}
