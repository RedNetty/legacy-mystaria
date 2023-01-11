package me.retrorealms.practiceserver.commands.items;

import me.retrorealms.practiceserver.mechanics.drops.CreateDrop;
import me.retrorealms.practiceserver.mechanics.drops.EliteDrops;
import me.retrorealms.practiceserver.mechanics.mobs.MobHandler;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CreateDropCommand implements CommandExecutor {

    public boolean isDungeonElite(String type) {
        if (type.equalsIgnoreCase("krampus") || type.equalsIgnoreCase("warden") || type.equalsIgnoreCase("weakSkeletonEntity") || type.equalsIgnoreCase("bossSkeletonDungeon")) {
            return true;
        }
        return false;
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if(!sender.isOp() || sender instanceof ConsoleCommandSender) return false;
        ModerationMechanics.ImportantStaff().forEach(staffMember -> {
            if (!sender.getName().equalsIgnoreCase(staffMember) || !(sender instanceof ConsoleCommandSender)) return;
        });
        if(args.length == 2) {
            try {
                String eliteName = args[0];
                if(!MobHandler.isCustomNamedElite(eliteName)) {
                    p.sendMessage(String.valueOf(ChatColor.RED) + ChatColor.BOLD +
                            "Incorrect Syntax: " + ChatColor.RED +
                            "/createdrop <elite> <item>");
                }
                if(isDungeonElite(eliteName)) {
                p.getInventory().addItem(new ItemStack(EliteDrops.createCustomDungeonDrop(eliteName,
                        Integer.parseInt(args[1]))));
                }else{
                    p.getInventory().addItem(new ItemStack(EliteDrops.createCustomEliteDrop(eliteName)));
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                p.sendMessage(String.valueOf(ChatColor.RED) + ChatColor.BOLD +
                        "Incorrect Syntax: " + ChatColor.RED +
                        "/createdrop <elite> <item>");

            }
        }
        if (args.length == 3) {
            try {
                p.getInventory().addItem(new ItemStack(CreateDrop.createDrop(Integer.parseInt(args[0]),
                        Integer.parseInt(args[1]), Integer.parseInt(args[2]))));
            } catch (Exception e2) {
                e2.printStackTrace();
                p.sendMessage(String.valueOf(ChatColor.RED) + ChatColor.BOLD +
                        "Incorrect Syntax: " + ChatColor.RED +
                        "/createdrop <tier> <item> <rarity>");

            }
        } else {
            p.sendMessage(
                    String.valueOf(ChatColor.RED) + ChatColor.BOLD + "Incorrect Syntax: " +
                            ChatColor.RED + "/createdrop <tier> <item> <rarity>");
        }

        return false;
    }
}