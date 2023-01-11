package me.retrorealms.practiceserver.mechanics.profession;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Giovanni on 26-5-2017.
 */

// look at this lmao.
    // its an npc which was never completed
    // Supposed to give you enchants from pickaxe
    // tbh we can make it so you can either buy enchants or you can scrap picks idk...

public class Enchanter implements Listener {

    private final HashMap<UUID, ItemStack> enchantingSession = Maps.newHashMap();

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        if (entity.getName().toLowerCase().contains("enchanter")) {

            event.setCancelled(true);

            new EnchantGUI(player).openFor(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!event.getInventory().getName().toLowerCase().contains("enchanter")) return;

        if (event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE) {
            event.setCancelled(true);
        } else {

            if (event.getSlot() != 14) return;

            ItemStack itemStack = event.getCursor();

            if (itemStack == null) return;

            if (!itemStack.getType().name().contains("_PICKAXE")) return;

            HashMap<Integer, ? extends ItemStack> shardMap = player.getInventory().all(Material.PRISMARINE_SHARD);

            double requiredShards = (15 * ProfessionMechanics.getPickaxeTier(itemStack)) / 0.2;

            if (shardMap.size() < requiredShards) {

                event.getCursor().setType(Material.AIR);
                player.getInventory().addItem(itemStack);

                player.closeInventory();

                player.sendMessage(ChatColor.RED + "Enchanter: Come back to me when you've got at least " + ChatColor.BOLD + (int) requiredShards + ChatColor.RED + " shards!");

                return;
            }

            this.removeShards(player, (int) requiredShards);

            event.getCursor().setType(Material.AIR);
            player.getInventory().remove(itemStack);

            player.getInventory().addItem(this.clearEnchants(itemStack, player));
            player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 5F, 1F);
        }
    }

    ItemStack clearEnchants(ItemStack itemStack, Player player) {
        List<String> lore = Lists.newArrayList();
        for (String strings : itemStack.getItemMeta().getLore()) {
            if (strings.contains("GEM FIND") || strings.contains("TRIPLE ORE") || strings.contains("DOUBLE ORE")) {

                player.getInventory().addItem(ItemAPI.getScrollGenerator().nextEnchantPickaxe(strings, ProfessionMechanics.getPickEnchants(itemStack, strings)));

                continue;
            }

            lore.add(strings);
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(lore);

        itemStack.setItemMeta(itemMeta);


        return itemStack;
    }

    private void removeShards(Player p, int amt) {
        int i = 0;
        while (i < p.getInventory().getSize()) {
            ItemStack is = p.getInventory().getItem(i);
            if (amt > 0) {
                int val;
                if (is != null && is.getType() == Material.PRISMARINE_SHARD) {
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
}
