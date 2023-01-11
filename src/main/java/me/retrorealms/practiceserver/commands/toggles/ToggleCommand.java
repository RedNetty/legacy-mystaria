package me.retrorealms.practiceserver.commands.toggles;

import me.retrorealms.practiceserver.mechanics.player.Toggles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player) {
            Player player = (Player)commandSender;
            player.openInventory(Toggles.getToggleMenu(player));
        }
        return false;
    }
}
