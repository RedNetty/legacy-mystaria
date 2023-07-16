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

public class ShowLootCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!isPlayerWithOpStatus(sender)) {
            return false;
        }

        Player player = (Player) sender;

        int radius = parseRadius(args, player);
        if (radius < 0) {
            return true;
        }

        int count = countAndDisplayLootChests(player, radius);

        sendResultMessage(player, count, radius);

        return true;
    }

    private boolean isPlayerWithOpStatus(CommandSender sender) {
        return sender instanceof Player && sender.isOp();
    }

    private int parseRadius(String[] args, Player player) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Incorrect Syntax. " + ChatColor.RED + "/showloot <radius>");
            return -1;
        }

        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid radius. Please enter a number.");
            return -1;
        }
    }

    private int countAndDisplayLootChests(Player player, int radius) {
        Location location = player.getLocation();
        World world = location.getWorld();

        int startX = location.getBlockX() - radius;
        int endX = location.getBlockX() + radius;
        int startY = location.getBlockY() - radius;
        int endY = location.getBlockY() + radius;
        int startZ = location.getBlockZ() - radius;
        int endZ = location.getBlockZ() + radius;

        int count = 0;
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    if (LootChests.loot.containsKey(world.getBlockAt(x, y, z).getLocation())) {
                        count++;
                        world.getBlockAt(x, y, z).setType(Material.GLOWSTONE);
                    }
                }
            }
        }

        return count;
    }

    private void sendResultMessage(Player player, int count, int radius) {
        StringBuilder message = new StringBuilder();
        message.append(ChatColor.YELLOW)
                .append("Displaying ")
                .append(count)
                .append(" loot chests in a ")
                .append(radius)
                .append(" block radius...");

        player.sendMessage(message.toString());
        player.sendMessage(ChatColor.GRAY + "Break them to unregister the spawn point.");
    }
}