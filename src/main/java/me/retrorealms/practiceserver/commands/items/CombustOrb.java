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


    private final String orbName = Items.orb(false).getItemMeta().getDisplayName() != null ? Items.orb(false).getItemMeta().getDisplayName() : "";


    private final String legOrbName = Items.legendaryOrb(false).getItemMeta().getDisplayName() != null ? Items.legendaryOrb(false).getItemMeta().getDisplayName() : "";
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

        int orbsToGive = sortOrb(invOrbs, 64, player);
        IntStream.range(0, orbsToGive).forEach(consumer ->
            player.getInventory().addItem(Items.legendaryOrb(false)));
        player.sendMessage(ChatColor.GREEN + "You have created " + orbsToGive + " " + legOrbName + "(s)");



    }
    public int sortOrb(HashMap<Integer, ? extends ItemStack> oreMap, int removeAmt, Player p) {
        int ore = 0;
        for (ItemStack itemStack : oreMap.values()) {
            if (itemStack.getItemMeta().getDisplayName().contains(ChatColor.LIGHT_PURPLE + "Orb of Alteration")) {
                if (itemStack.getAmount() >= 64) {
                    ore += 64 * (itemStack.getAmount() / 64);
                    int remaining = itemStack.getAmount() % 64;
                    if (remaining > 0) {
                        itemStack.setAmount(remaining);
                        p.getInventory().addItem(itemStack);
                    } else {
                        p.getInventory().setItem(p.getInventory().first(itemStack), null);
                    }
                } else {
                    ore += itemStack.getAmount();
                    p.getInventory().setItem(p.getInventory().first(itemStack), null);
                }
            }
        }
        int legendaryOrbs = ore / removeAmt;
        int remainingOrbs = ore % removeAmt;
        if (remainingOrbs > 0) {
            ItemStack orbsLeft = Items.orb(false);
            orbsLeft.setAmount(remainingOrbs);
            IntStream.range(0, remainingOrbs).forEach(consumer ->
                    p.getInventory().addItem(Items.orb(false)));
            p.sendMessage(ChatColor.GREEN + "You have " + remainingOrbs + " remaining " + orbName + "(s)");
        }
        return legendaryOrbs;
    }
}
