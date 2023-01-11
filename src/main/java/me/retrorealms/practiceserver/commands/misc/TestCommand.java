package me.retrorealms.practiceserver.commands.misc;

import me.retrorealms.practiceserver.mechanics.profession.ProfessionMechanics;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(!sender.isOp()) return true;
        Player p = (Player) sender;
        ProfessionMechanics.addExp(p, p.getInventory().getItemInMainHand(), Integer.valueOf(args[0]), true);
        return false;
    }
}

