package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.mechanics.mobs.Spawners;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowMSCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
            if (p.isOp()) {
                if (args.length != 1) {
                    p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD)
                            .append("Incorrect Syntax. ").append(ChatColor.RED).append("/showms <radius>")
                            .toString());
                    return true;
                }
                int radius = 0;
                try {
                    radius = Integer.parseInt(args[0]);
                } catch (Exception e2) {
                    radius = 0;
                }
                Location loc = p.getLocation();
                final World w = loc.getWorld();
                final int x = (int) loc.getX();
                final int y = (int) loc.getY();
                final int z = (int) loc.getZ();
                int count = 0;
                for (int i = -radius; i <= radius; ++i) {
                    for (int j = -radius; j <= radius; ++j) {
                        for (int k = -radius; k <= radius; ++k) {
                            loc = w.getBlockAt(x + i, y + j, z + k).getLocation();
                            if (Spawners.spawners.containsKey(loc)) {
                                ++count;
                                loc.getBlock().setType(Material.MOB_SPAWNER);
                            }
                        }
                    }
                }
                p.sendMessage(ChatColor.YELLOW + "Displaying " + count + " mob spawners in a " + radius
                        + " block radius...");
                p.sendMessage(ChatColor.GRAY + "Break them to unregister the spawn point.");
            }
        }
        return false;
    }

}