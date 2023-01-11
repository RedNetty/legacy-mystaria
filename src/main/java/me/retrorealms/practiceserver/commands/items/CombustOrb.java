package me.retrorealms.practiceserver.commands.items;

import me.retrorealms.practiceserver.mechanics.item.Items;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.stream.IntStream;

public class CombustOrb implements CommandExecutor {

    private int orbs = 0;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UpgradeOrb(player);
        }
        return true;
    }

    public void UpgradeOrb(Player player){

        HashMap<Integer, ? extends ItemStack> invOrbs = player.getInventory().all(Material.MAGMA_CREAM);

        sortOrb(invOrbs, 64, Material.IRON_ORE, player);

        IntStream.range(0, orbs).forEach(consumer -> {
            player.getInventory().addItem(Items.legendaryOrb(false));
        });

        player.sendMessage(ChatColor.GREEN + "Your created " + orbs + " Legendary orbs!");

        orbs = 0;

    }

    public void sortOrb(HashMap<Integer, ? extends ItemStack> oreMap, int removeAmt, Material mat, Player p){
        int ore= 0;
        for (ItemStack itemStack : oreMap.values()) {
            if(itemStack.getItemMeta().getDisplayName().contains(ChatColor.LIGHT_PURPLE + "Orb of Alteration")){
                ore += itemStack.getAmount();

            }
        }
        while (ore >= removeAmt) {
            ore -= removeAmt;
            removeOrbs(p);
            orbs++;
        }
    }

    public void removeOrbs(Player p) {
        int i = 0;
        while (i < p.getInventory().getSize()) {
            ItemStack is = p.getInventory().getItem(i);
            if (is != null && is.getAmount() == 64 && is.getItemMeta().getDisplayName().contains(ChatColor.LIGHT_PURPLE + "Orb of Alteration")) {
                p.getInventory().setItem(i, null);
            }
        ++i;
        }
    }
}
