package me.retrorealms.practiceserver.commands.moderation;

import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
            if (p.isOp()) {
                p.teleport(TeleportBooks.DeadPeaks);
            }
        }
        return false;
    }

}