package me.retrorealms.practiceserver.commands.misc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ToggleEnergyCommand implements CommandExecutor {
    /**
     * Created by Kaveen K (https://digistart.ca)
     * 08/06/2018
     */
    public static ArrayList<String> energytoggled = new ArrayList<String>();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (player.isOp()) {
                String name = player.getName();
                if (energytoggled.contains(name)) {
                    energytoggled.remove(name);
                    player.sendMessage(ChatColor.GREEN + "Your energy was turned back on.");
                } else {
                    energytoggled.add(name);
                    player.sendMessage(ChatColor.RED + "Your energy was turned off.");
                }

            } else {
                player.sendMessage(ChatColor.RED + "No permission");

            }

        }
        return true;
    }

}
