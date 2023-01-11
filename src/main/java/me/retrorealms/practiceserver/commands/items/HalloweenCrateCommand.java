package me.retrorealms.practiceserver.commands.items;

import me.retrorealms.practiceserver.mechanics.donations.Crates.CratesMain;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HalloweenCrateCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(!sender.isOp()) return false;
        Player p = Bukkit.getPlayer(args[0]);
        if (p != null && p.isOnline()) {
            if (args.length < 3) {
                p.getInventory().addItem(CratesMain.createCrate(Integer.parseInt(args[1]), true));
            }else{
                for(int i=0 ; i < Integer.parseInt(args[2]); i++){
                    p.getInventory().addItem(CratesMain.createCrate(Integer.parseInt(args[1]), true));
                }
            }
        }


        return false;
    }


}