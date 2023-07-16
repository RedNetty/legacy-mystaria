package me.retrorealms.practiceserver.commands.misc;

import me.retrorealms.practiceserver.mechanics.drops.Mobdrops;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DropRatesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            StringUtil.sendCenteredMessage(player, "&e-- Drop Rates --");
            StringUtil.sendCenteredMessage(player, "&bTier 1 &7 - &3" + Mobdrops.getT1RATES() + "%");
            StringUtil.sendCenteredMessage(player, "&bTier 2 &7 - &3" + Mobdrops.getT2RATES() + "%");
            StringUtil.sendCenteredMessage(player, "&bTier 3 &7 - &3" + Mobdrops.getT3RATES() + "%");
            StringUtil.sendCenteredMessage(player, "&bTier 4 &7 - &3" + Mobdrops.getT4RATES() + "%");
            StringUtil.sendCenteredMessage(player, "&bTier 5 &7 - &3" + Mobdrops.getT5RATES() + "%");
        }
        return false;
    }

}
