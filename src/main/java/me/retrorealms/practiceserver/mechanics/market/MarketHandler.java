package me.retrorealms.practiceserver.mechanics.market;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.API;
import me.retrorealms.practiceserver.mechanics.money.Economy.Economy;
import me.retrorealms.practiceserver.mechanics.money.Money;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Khalid on 8/3/2017.
 */
public class MarketHandler implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) e.getWhoClicked();
        if (e.getInventory() == null)
            return;
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;
        Inventory top = p.getOpenInventory().getTopInventory();
        if (top != null && (top.getTitle().contains("Market Actions") || top.getTitle().contains("Your listed items - ") || top.getTitle().contains("Choose an item to list") || ChatColor.stripColor(top.getTitle()).contains("Global Market"))) {
            e.setCancelled(true);
        }

        if (e.getInventory().getTitle().contains("Market Actions")) {
            e.setCancelled(true);
            ItemStack i = e.getCurrentItem();
            if (!(i.hasItemMeta() && i.getItemMeta().hasDisplayName()))
                return;
            String s = ChatColor.stripColor(i.getItemMeta().getDisplayName());
            switch (s) {
                case "View your listed items":
                    API.getGlobalMarket().openListedItemGUI(p, 1);
                    break;
                case "List an item for sale":
                    API.getGlobalMarket().openListGUI(p);
                    break;
                case "View the market-place":
                    API.getGlobalMarket().openMarketGUI(p, 1);
                    break;
            }
        }

        if (e.getInventory().getTitle().contains("Your listed items - ")) {
            e.setCancelled(true);
            if (e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName() && e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next Page")) {
                API.getGlobalMarket().openListedItemGUI(p, API.getGlobalMarket().getCurrentPage(e.getInventory()) + 1);
                return;
            }
            if (e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName() && e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.RED + "Previous Page")) {
                API.getGlobalMarket().openListedItemGUI(p, API.getGlobalMarket().getCurrentPage(e.getInventory()) - 1);
                return;
            }
            ListedItem i = API.getGlobalMarket().getListedItem(e.getCurrentItem());
            API.getGlobalMarket().unlistItem(i);
            p.closeInventory();
            return;
        }

        if (e.getInventory().getTitle().contains("Choose an item to list")) {
            e.setCancelled(true);
            API.getGlobalMarket().addPlayerToSpecify(p, e.getCurrentItem());
            p.closeInventory();
            p.sendMessage(ChatColor.GOLD + "Please specify a price for the listed item\n(type the price as a message, example: 100)\n" + ChatColor.RED + "Type '" + ChatColor.UNDERLINE + "CANCEL" + ChatColor.RED + "' to cancel.");
        }

        if (ChatColor.stripColor(e.getInventory().getTitle()).contains("Global Market")) {
            e.setCancelled(true);
            if (e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName() && e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next Page")) {
                API.getGlobalMarket().openMarketGUI(p, API.getGlobalMarket().getCurrentPage(e.getInventory()) + 1);
                return;
            }
            if (e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName() && e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.RED + "Previous Page")) {
                API.getGlobalMarket().openMarketGUI(p, API.getGlobalMarket().getCurrentPage(e.getInventory()) - 1);
                return;
            }

            ListedItem i = API.getGlobalMarket().getListedItem(e.getCurrentItem());
            int price = i.getPrice();
            if (Money.hasEnoughGems(p, price)) {
                p.closeInventory();
                Money.takeGems(p, price);
                Economy.depositPlayer(i.getOwner(), price);
                buyItem(p, i);
                return;
            }
            if (Economy.getBalance(p.getUniqueId()) >= price) {
                p.closeInventory();
                Economy.withdrawPlayer(p.getUniqueId(), price);
                Economy.depositPlayer(i.getOwner(), price);
                buyItem(p, i);
                return;
            }
            p.sendMessage(ChatColor.GOLD + "You do not have enough Gems to buy this item");
            return;
        }
    }

    @EventHandler
    public void onRight(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof HumanEntity) {
            HumanEntity p = (HumanEntity) e.getRightClicked();

            if (p.getName() == null) {
                return;
            }
            if (!p.hasMetadata("NPC")) {
                return;
            }

            if (p.getName().equals("Auctioneer")) {
                API.getGlobalMarket().openChoiceGUI(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.getMessage().equalsIgnoreCase("serialize")) {
            PracticeServer.getManagerHandler().onDisable();
        }
        Player p = e.getPlayer();
        if (API.getGlobalMarket().getSpecifyPrice().containsKey(p.getUniqueId().toString())) {
            e.setCancelled(true);
            if (!isInteger(e.getMessage())) {
                if (e.getMessage().toLowerCase().equals("cancel")) {
                    p.sendMessage(ChatColor.GOLD + "The listing has been cancelled.");
                    API.getGlobalMarket().removePlayerFromSpecify(p);
                    return;
                }
                p.sendMessage(ChatColor.GOLD + "Please specify a valid integer to list your item");
                return;
            }
            int i = Integer.valueOf(e.getMessage());
            ItemStack itemStack = API.getGlobalMarket().getSpecifyPrice().get(p.getUniqueId().toString());
            if (!p.getInventory().contains(itemStack)) {
                p.sendMessage(ChatColor.GOLD + "You must have the item in your inventory.\nThe listing process has been cancelled.");
                API.getGlobalMarket().removePlayerFromSpecify(p);
                return;
            }
            p.getInventory().removeItem(itemStack);
            API.getGlobalMarket().listItem(p, i, itemStack);
            API.getGlobalMarket().removePlayerFromSpecify(p);
            p.sendMessage(ChatColor.GOLD + "The item has been listed.\nYou can view it in the listed items menu.");
        }
    }

    private void buyItem(Player p, ListedItem i) {
        if (p.getInventory().firstEmpty() == -1) {
            p.getWorld().dropItemNaturally(p.getLocation(), i.getItemStack());
            p.sendMessage(ChatColor.GOLD + "The item has been dropped due to your inventory being full..");
        } else {
            p.getInventory().addItem(i.getItemStack());
        }
        p.closeInventory();
        API.getGlobalMarket().removeListedItem(i);
        p.sendMessage(ChatColor.GOLD + "You have successfully bought the item.");
        if (Bukkit.getPlayer(i.getOwner()) == null) return;
        Bukkit.getPlayer(i.getOwner()).sendMessage(ChatColor.GOLD + "Someone has bought an item that you listed.");

    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }
}
