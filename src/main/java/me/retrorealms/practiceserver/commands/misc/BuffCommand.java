package me.retrorealms.practiceserver.commands.misc;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.drops.buff.BuffHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BuffCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (PracticeServer.buffHandler().isActive()) {
            BuffHandler lootBuff = PracticeServer.buffHandler();
            sender.sendMessage(ChatColor.GREEN + lootBuff.getActiveBuff().getOwnerName() + " has activated a " + lootBuff.getActiveBuff().getUpdate() + "% Lootbuff");
            sender.sendMessage(ChatColor.GREEN + lootBuff.getActiveBuff().getOwnerName() + "'s Lootbuff is active for " + lootBuff.getActiveBuff().getTimeLeft() / 60 + " more minute(s).");
        } else {
            sender.sendMessage(ChatColor.RED + "There is no active Lootbuff at this time.");
        }
        return true;
    }
}

