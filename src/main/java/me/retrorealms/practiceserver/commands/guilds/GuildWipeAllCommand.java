package me.retrorealms.practiceserver.commands.guilds;

import me.retrorealms.practiceserver.mechanics.guilds.GuildMechanics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Matthew E on 8/7/2017.
 */
public class GuildWipeAllCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.isOp()) {
                GuildMechanics.getInstance().wipe();
                return true;
            }
        } else if (sender instanceof ConsoleCommandSender) {

            GuildMechanics.getInstance().wipe();
            return true;

        }
        return true;
    }
}
