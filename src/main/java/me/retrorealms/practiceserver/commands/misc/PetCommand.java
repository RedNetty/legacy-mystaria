package me.retrorealms.practiceserver.commands.misc;

import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.pets.utils.PetGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Khalid on 8/8/2017.
 */
public class PetCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;
        if (ModerationMechanics.isDonator(p) || p.isOp()) {
            PetGUI.openGUI(p);
        }
        return true;
    }
}
