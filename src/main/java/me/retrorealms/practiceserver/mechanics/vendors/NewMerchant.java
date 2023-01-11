package me.retrorealms.practiceserver.mechanics.vendors;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import me.retrorealms.practiceserver.commands.moderation.DeployCommand;
import me.retrorealms.practiceserver.mechanics.item.Durability;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.player.Trading;
import me.retrorealms.practiceserver.mechanics.profession.ProfessionMechanics;
import me.retrorealms.practiceserver.utils.item.ItemGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

public class NewMerchant implements Listener {

    public static HashMap<Player, Inventory> tradeMap = new LinkedHashMap<>();

    @EventHandler
    public void onVendorInteract(PlayerInteractAtEntityEvent e) {
        if (DeployCommand.patchlockdown) {
            e.setCancelled(true);
            return;
        }
        if (e.getRightClicked() instanceof HumanEntity) {
            Player player = e.getPlayer();
            HumanEntity npc = (HumanEntity) e.getRightClicked();
            if (npc.getName() == null) {
                return;
            }
            if (!npc.hasMetadata("NPC") || !npc.getName().equalsIgnoreCase("Merchant")) {
                return;
            }
            if (tradeMap.containsKey(player)) {
                player.openInventory(tradeMap.get(player));
            } else {
                openNewMerchant(player);
            }
        }
    }

    public void openNewMerchant(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "[Merchant]");
        ItemStack cancel = new ItemGenerator(Material.BARRIER).setName(ChatColor.RED + "Cancel Trade").build();
        ItemStack glass = new ItemGenerator(Material.STAINED_GLASS_PANE).setDurability((short) 7).setName(" ").build();
        ItemStack accept = new ItemGenerator(Material.EMERALD).setName(ChatColor.GREEN + "Accept Trade").build();
        int i = 45;
        while (i < 54) {
            inventory.setItem(i, glass);
            ++i;
        }
        inventory.setItem(53, accept);
        inventory.setItem(45, cancel);
        player.openInventory(inventory);
        tradeMap.put(player, inventory);

    }

    public void onEnable() {
        System.out.println("[Merchant] has been enabled");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    public void onDisable() {
        System.out.println("[Merchant] has been disabled");
    }

    @EventHandler
    public void confirmMerchant(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (tradeMap.containsKey(p)) {
            if (e.getInventory().getTitle().equals("[Merchant]")) {
                if (e.getSlot() == 53) {
                    acceptTrade(p);
                    e.setCancelled(true);
                }
                if (e.getSlot() == 45) {
                    cancelTrade(p);
                    e.setCancelled(true);
                }
                if (e.getSlot() >= 45) {
                    e.setCancelled(true);
                }
            }
        }
    }

    public void cancelTrade(Player player) {
        Inventory inventory = tradeMap.get(player);
        if (inventory != null) {
            int i = 0;
            while (i < 45) {
                if (inventory != null && inventory.getItem(i) != null && inventory.getItem(i).getType() != Material.AIR) {
                    player.sendMessage(ChatColor.RED + "You must clear the trade menu before cancelling it.");
                    return;
                } else {
                    ++i;
                }
            }
            tradeMap.remove(player);
            player.closeInventory();
            player.sendMessage(ChatColor.GRAY + "You have cancelled your merchant trade.");
        }
    }


    public int calculateTradeValue(Inventory inventory) {
        int totalValue = 0;
        if (inventory != null) {
            int i = 0;
            while (i < 45) {
                if (inventory.getItem(i) != null && inventory.getItem(i).getType() != Material.AIR) {
                    ItemStack is = inventory.getItem(i);
                    double reward = 0;
                    if (is.getType().toString().contains("_ORE") && ProfessionMechanics.getOreTier(is.getType()) > 0) {
                        int oreAmount = is.getAmount();
                        int oreTier = ProfessionMechanics.getOreTier(is.getType());
                        reward = 20;
                        reward = (int) ((reward * oreAmount * oreTier) * 1.23);
                        inventory.setItem(i, null);
                    }
                    if (Durability.isArmor(is) || Listeners.isWeapon(is)) {
                        Random r = new Random();
                        int t = Items.getTierFromColor(is);
                        reward = (((t / 10D) + 1D) * (t * t)) * 12D;
                        reward = r.nextInt((int) reward / 3) + (int) reward;
                        inventory.setItem(i, null);
                    }
                    totalValue += reward;
                }
                ++i;
            }
        }
        return totalValue;
    }

    public void acceptTrade(Player player) {
        Inventory inventory = tradeMap.get(player);
        if (inventory != null) {
            double rew = calculateTradeValue(inventory);
            if ((ModerationMechanics.isDonator(player)) || (ModerationMechanics.isStaff(player))) {
                rew *= 1.20D;
            }
            if (rew != 0) {
                player.getInventory().addItem(Money.createBankNote((int) rew));
            }
        }
        int i = 0;
        while (i < 45) {
            if (inventory.getItem(i) != null && inventory.getItem(i).getType() != Material.AIR) {
                player.getInventory().addItem(inventory.getItem(i));
            }
            i++;
        }
        tradeMap.remove(player);
        player.closeInventory();
        player.sendMessage(ChatColor.GRAY + "You have accepted your merchant trade.");
    }
}

