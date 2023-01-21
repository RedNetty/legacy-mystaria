package me.retrorealms.practiceserver.commands.toggles;

import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleGemCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player) {
            Player player = (Player)commandSender;
            if(ModerationMechanics.isDonator(player) && ModerationMechanics.getRank(player) != RankEnum.SUB) {
                Toggles.changeToggle(player, "Gems");
            }else{
                player.sendMessage(ChatColor.RED + "This command requires (Sub+) and above, donate by messaging red.");
            }
        }
        return false;
    }
}
