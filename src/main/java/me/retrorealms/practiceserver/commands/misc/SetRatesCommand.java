package me.retrorealms.practiceserver.commands.misc;

import me.retrorealms.practiceserver.mechanics.drops.Mobdrops;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetRatesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!sender.isOp()) return false;
        if (args.length == 2) {
            try {
                Mobdrops.setRATES(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Correct Usage /setrates <tier> <percentage>");
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Correct Usage /setrates <tier> <percentage>");

            return false;
        }
    }


}
