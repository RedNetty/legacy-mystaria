package me.retrorealms.practiceserver.commands.toggles;

import me.retrorealms.practiceserver.mechanics.player.Toggles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Jaxon on 8/19/2017.
 */
public class TogglePVPCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player) {
            Player player = (Player)commandSender;
            Toggles.changeToggle(player, "Anti PVP");
        }
        return false;
    }
}
