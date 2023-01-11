package me.retrorealms.practiceserver.mechanics.money;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Money {
    public static boolean hasEnoughGems(Player p, int amt) {
        int gems = 0;
        ItemStack[] arritemStack = p.getInventory().getContents();
        int n = arritemStack.length;
        int n2 = 0;
        while (n2 < n) {
            ItemStack is = arritemStack[n2];
            if (Money.isGem(is)) {
                gems += is.getAmount();
            }
            if (Money.isBankNote(is)) {
                gems += Money.getGems(is);
            }
            if (GemPouches.isGemPouch(is)) {
                gems += GemPouches.getCurrentValue(is);
            }
            ++n2;
        }
        if (gems >= amt) {
            return true;
        }
        return false;
    }

    public static int getGems(Player p) {
        int gems = 0;
        ItemStack[] arritemStack = p.getInventory().getContents();
        int n = arritemStack.length;
        int n2 = 0;
        while (n2 < n) {
            ItemStack is = arritemStack[n2];
            if (Money.isGem(is)) {
                gems += is.getAmount();
            }
            if (Money.isBankNote(is)) {
                gems += Money.getGems(is);
            }
            if (GemPouches.isGemPouch(is)) {
                gems += GemPouches.getCurrentValue(is);
            }
            ++n2;
        }
        return gems;
    }

    public static void takeGems(Player p, int amt) {
        if (Money.hasEnoughGems(p, amt)) {
            int i = 0;
            while (i < p.getInventory().getSize()) {
                ItemStack is = p.getInventory().getItem(i);
                if (amt > 0) {
                    int val;
                    if (Money.isGem(is)) {
                        if (amt >= is.getAmount()) {
                            amt -= is.getAmount();
                            p.getInventory().setItem(i, null);
                        } else {
                            is.setAmount(is.getAmount() - amt);
                            amt = 0;
                        }
                    }
                    if (Money.isBankNote(is)) {
                        val = Money.getGems(is);
                        if (amt >= val) {
                            amt -= val;
                            p.getInventory().setItem(i, null);
                        } else {
                            ItemMeta im = is.getItemMeta();
                            im.setLore(Arrays.asList(ChatColor.WHITE.toString() + ChatColor.BOLD + "Value: " + ChatColor.WHITE + (val - amt) + " Gems", ChatColor.GRAY + "Exchange at any bank for GEM(s)"));
                            is.setItemMeta(im);
                            amt = 0;
                        }
                    }
                    if (GemPouches.isGemPouch(is)) {
                        val = GemPouches.getCurrentValue(is);
                        if (amt >= val) {
                            amt -= val;
                            GemPouches.setPouchBal(is, 0);
                        } else {
                            GemPouches.setPouchBal(is, val - amt);
                            amt = 0;
                        }
                    }
                }
                ++i;
            }
        }
    }

    public static int getShards(Player player) {
        HashMap<Integer, ? extends ItemStack> itemData = player.getInventory().all(Material.PRISMARINE_SHARD);

        return itemData.size();
    }

    public static boolean isGem(ItemStack is) {
        if (is != null && is.getType() != Material.AIR && is.getType() == Material.EMERALD && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().toLowerCase().contains("gem")) {
            return true;
        }
        return false;
    }

    public static boolean isBankNote(ItemStack is) {
        if (is != null && is.getType() != Material.AIR && is.getType() == Material.PAPER && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().toLowerCase().contains("bank note")) {
            return true;
        }
        return false;
    }

    public static ItemStack createBankNote(int amt) {
        ItemStack is = new ItemStack(Material.PAPER);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.GREEN + "Bank Note");
        im.setLore(Arrays.asList(ChatColor.WHITE.toString() + ChatColor.BOLD + "Value: " + ChatColor.WHITE + amt + " Gems", ChatColor.GRAY + "Exchange at any bank for GEM(s)"));
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack makeGems(int amt) {
        ItemStack is = new ItemStack(Material.EMERALD, amt);
        ItemMeta im = is.getItemMeta();
        im.setLore(Arrays.asList(String.valueOf(ChatColor.GRAY.toString()) + "The currency of Andalucia"));
        im.setDisplayName(String.valueOf(ChatColor.WHITE.toString()) + "Gem");
        is.setItemMeta(im);
        is.setAmount(amt);
        return is;
    }

    public static int getGems(ItemStack is) {
        List<String> lore;
        if (is != null && is.getType() != Material.AIR && is.getType() == Material.PAPER && is.getItemMeta().hasLore() && (lore = is.getItemMeta().getLore()).size() > 0 && ((String) lore.get(0)).contains("Value")) {
            try {
                String line = ChatColor.stripColor(lore.get(0));
                return Integer.parseInt(line.split(": ")[1].split(" Gems")[0]);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
}

