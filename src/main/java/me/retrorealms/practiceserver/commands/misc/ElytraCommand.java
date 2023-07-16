package me.retrorealms.practiceserver.commands.misc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;

public class ElytraCommand implements CommandExecutor {
    /**
     * Created by Kaveen K (https://digistart.ca) on 08/09/2018
     */

    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("retrorealms.betaelytra")) {
                ItemStack elytra = new ItemStack(Material.ELYTRA);
                ItemMeta elytrameta = elytra.getItemMeta();
                elytrameta.setDisplayName(ChatColor.AQUA + "Elytra Mount");
                elytrameta.setLore(Collections.singletonList(ChatColor.GRAY + "This is a beta item in testing!"));
                elytra.setItemMeta(elytrameta);
                if (player.getInventory().containsAtLeast(elytra, 1)) {
                    player.sendMessage(ChatColor.RED + "You already have an elytra mount in your inventory!");
                    return true;
                }

                player.getInventory().addItem(elytra);
                player.sendMessage(ChatColor.AQUA + "You have recieved a BETA elytra mount!");

            }

        }

        return true;
    }

}
