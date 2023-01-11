package me.retrorealms.practiceserver.commands.items;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveMaskCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(!sender.isOp()) return false;
        Player p = Bukkit.getPlayer(args[0]);
        if (p != null && p.isOnline()) {
            p.getInventory().addItem(PracticeServer.getManagerHandler().getHalloween().halloweenHat());
        }


        return false;
    }


}