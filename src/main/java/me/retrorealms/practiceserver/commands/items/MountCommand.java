package me.retrorealms.practiceserver.commands.items;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
import me.retrorealms.practiceserver.mechanics.world.MinigameState;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MountCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            Inventory inv = p.getInventory();
            if (inv.contains(Material.SADDLE)) {
                inv.remove(Material.SADDLE);
            }
            if(PracticeServer.getRaceMinigame().getGameState() == MinigameState.NONE) inv.addItem(Horses.createMount(Horses.horseTier.get(p), false));
        }
        return false;
    }
}

