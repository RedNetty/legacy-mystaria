package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.AttackTest;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBossHandler;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.bosses.Frostwing;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CloneCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (p.isOp()) {
            if (args.length == 1) {
                int attk = Integer.parseInt(args[0]);
                if (attk != 0) {
                    switch (attk) {
                        case 1:
                            WorldBossHandler.spawnBoss();
                            new AttackTest().frostBreathAttack(p.getLocation(), 10);
                            break;
                        case 3:
                            new AttackTest().iceBlockBarrageAttack(p, 10, 20, 30, 20);
                            break;
                        case 4:
                            new AttackTest().iceSpikeAttack(p.getLocation(), 20);
                            break;
                        case 5:
                            new AttackTest().icyGroundAttack(p.getLocation(), 15, 30, 4, 20, 5);
                            break;
                        default:
                            new AttackTest().iceBlastAttack(p);
                            break;

                    }

                } else {
//                if (Bukkit.getServer().getOfflinePlayer(args[0]).isOnline()) {
//                    SQLMain.updatePlayerStats(Bukkit.getServer().getPlayer(args[0]));
//                }
//                SQLMain.clonePlayer(p, args[0]);
//            } else {
                    p.sendMessage(ChatColor.RED + "/playerclone <player>");
                }
            }
        }
        return false;
    }
}