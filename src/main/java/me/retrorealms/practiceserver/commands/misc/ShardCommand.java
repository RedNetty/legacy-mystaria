package me.retrorealms.practiceserver.commands.misc;

import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import me.retrorealms.practiceserver.mechanics.shard.gui.ServerGUI;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Jaxon on 8/20/2017.
 */
public class ShardCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player) {
            Player p = (Player)commandSender;
            if (!Alignments.isSafeZone(p.getLocation()) && Alignments.tagged.containsKey(p.getName())
                    && System.currentTimeMillis() - Alignments.tagged.get(p.getName()) < 10000) {
                p.sendMessage(ChatColor.RED + "You cannot change servers while in combat!");
                return false;
            }else{
                p.openInventory(ServerGUI.serverGUI(p));
                p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
            }
        }
        return false;
    }
}
