package me.retrorealms.practiceserver.commands.items;

import me.retrorealms.practiceserver.apis.tab.TabUtil;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.item.Durability;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.stream.IntStream;

public class OrbsCommand implements CommandExecutor {

    private int orbs = 0;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            scrapOre(player);
        }
        return true;
    }

    private void removeAmountOfOre(Player p, Material material, int amt) {
        int i = 0;
        while (i < p.getInventory().getSize()) {
            ItemStack is = p.getInventory().getItem(i);
            if (amt > 0) {
                int val;
                if (is != null && is.getType() == material) {
                    if (amt >= is.getAmount()) {
                        amt -= is.getAmount();
                        p.getInventory().setItem(i, null);
                    } else {
                        is.setAmount(is.getAmount() - amt);
                        amt = 0;
                    }
                }
            }
            ++i;
        }
    }

    public void scrapOre(Player player){
        HashMap<Integer, ? extends ItemStack> diamondOre = player.getInventory().all(Material.DIAMOND_ORE);
        HashMap<Integer, ? extends ItemStack> ironOre = player.getInventory().all(Material.IRON_ORE);
        HashMap<Integer, ? extends ItemStack> goldOre = player.getInventory().all(Material.GOLD_ORE);
        HashMap<Integer, ? extends ItemStack> frozenOre = player.getInventory().all(Material.LAPIS_ORE);

        sortOre(ironOre, 60, Material.IRON_ORE, player);
        sortOre(diamondOre, 30, Material.DIAMOND_ORE, player);
        sortOre(goldOre, 10, Material.GOLD_ORE, player);
        sortOre(frozenOre, 5, Material.LAPIS_ORE, player);

        IntStream.range(0, orbs).forEach(consumer -> {
            player.getInventory().addItem(Items.orb(false));
        });

        player.sendMessage(ChatColor.GREEN + "Your created " + orbs + " orbs!");

        orbs = 0;

    }

    public void sortOre(HashMap<Integer, ? extends ItemStack> oreMap, int removeAmt, Material mat, Player p){
        for (ItemStack itemStack : oreMap.values()) {
            int ore= 0;
            ore += itemStack.getAmount();
            while (ore >= removeAmt){
                ore -= removeAmt;
                removeAmountOfOre(p, mat, removeAmt);
                orbs++;
            }
        }
    }

}
