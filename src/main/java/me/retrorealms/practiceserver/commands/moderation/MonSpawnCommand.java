package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.mechanics.mobs.Spawners;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class MonSpawnCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
            if (p.isOp()) {
                if (cmd.getName().equals("monspawn")) {
                    if (args.length != 1) {
                        p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD)
                                .append("Incorrect Syntax. ").append(ChatColor.RED).append("/monspawn <mobtype>")
                                .toString());
                        p.sendMessage(ChatColor.YELLOW + "FORMAT: " + ChatColor.GRAY + "mobtype:tier@elite#amount");
                        p.sendMessage(ChatColor.YELLOW + "EX: " + ChatColor.GRAY + "skeleton:5@true#1");
                        return true;
                    }
                    final String data = args[0];
                    final Location loc = p.getTargetBlock( null, 100).getLocation();
                    if (Spawners.isCorrectFormat(data)) {
                        if (data.contains(",")) {
                            String[] split;
                            for (int length = (split = data.split(",")).length, n = 0; n < length; ++n) {
                                final String s = split[n];
                                final String type = s.split(":")[0];
                                final int tier = Integer.parseInt(s.split(":")[1].split("@")[0]);
                                final boolean elite = Boolean.parseBoolean(s.split("@")[1].split("#")[0]);
                                for (int amt = Integer.parseInt(s.split("#")[1]), l = 0; l < amt; ++l) {
                                    Spawners.spawnMob(loc, type, tier, elite);
                                }
                            }
                        } else {
                            final String type2 = data.split(":")[0];
                            final int tier2 = Integer.parseInt(data.split(":")[1].split("@")[0]);
                            final boolean elite2 = Boolean.parseBoolean(data.split("@")[1].split("#")[0]);
                            for (int amt2 = Integer.parseInt(data.split("#")[1]), m = 0; m < amt2; ++m) {
                                Spawners.spawnMob(loc, type2, tier2, elite2);
                            }
                        }
                        p.sendMessage(ChatColor.GRAY + "Spawned " + ChatColor.YELLOW + data + ChatColor.GRAY + " at "
                                + ChatColor.YELLOW + loc.toVector().toString());
                    } else {
                        p.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD)
                                .append("Incorrect Syntax. ").append(ChatColor.RED).append("/monspawn <mobtype>")
                                .toString());
                    }
                }
            }
        }
        return false;
    }
}
