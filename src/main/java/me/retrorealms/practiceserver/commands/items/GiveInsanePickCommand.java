package me.retrorealms.practiceserver.commands.items;

import me.retrorealms.practiceserver.mechanics.profession.ProfessionMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by Red on 11-8-2017.
 */

public class GiveInsanePickCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args){
        if(!sender.isOp()) return false;
        Player p = Bukkit.getPlayer(args[0]);
        if(p != null && p.isOnline()){
            ProfessionMechanics.getDonorPicked(p);
        }
        return false;
    }
}
