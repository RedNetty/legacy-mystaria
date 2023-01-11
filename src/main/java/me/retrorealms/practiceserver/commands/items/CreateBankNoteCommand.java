package me.retrorealms.practiceserver.commands.items;

import me.retrorealms.practiceserver.mechanics.money.Money;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Jaxon on 8/5/2017.
 */
public class CreateBankNoteCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender.isOp() && commandSender instanceof Player) {
            if (args.length == 1) {
                Player player = (Player) commandSender;
                if(!player.isOp()) return false;
                int noteAmount = Integer.parseInt(args[0]);
                player.getInventory().addItem(Money.createBankNote(noteAmount));
                player.sendMessage(ChatColor.GREEN + "Bank note added to your inventory.");
            } else {
                commandSender.sendMessage(ChatColor.RED + "Error: Please specify a number.");
            }
        }
        return false;
    }
}
