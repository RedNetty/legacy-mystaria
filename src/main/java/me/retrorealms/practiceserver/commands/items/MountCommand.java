package me.retrorealms.practiceserver.commands.items;

import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
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
            inv.addItem(Horses.mount(Horses.horseTier.get(p), false));
        }
        return false;
    }
}

