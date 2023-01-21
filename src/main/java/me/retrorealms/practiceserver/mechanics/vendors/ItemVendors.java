/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.player.AsyncPlayerChatEvent
 *  org.bukkit.event.player.PlayerInteractEntityEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.InventoryView
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 */
package me.retrorealms.practiceserver.mechanics.vendors;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.moderation.DeployCommand;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.item.scroll.ScrollGUI;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayer;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayers;
import me.retrorealms.practiceserver.mechanics.player.Speedfish;
import me.retrorealms.practiceserver.mechanics.profession.Fishing;
import me.retrorealms.practiceserver.mechanics.profession.Mining;
import me.retrorealms.practiceserver.mechanics.profession.ProfessionMechanics;
import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ItemVendors
        implements Listener {
    public static HashMap<String, ItemStack> buyingitem = new HashMap<String, ItemStack>();
    public static HashMap<String, Integer> buyingprice = new HashMap<String, Integer>();
    public static ArrayList<Player> fixList = new ArrayList<>();

    public void onEnable() {
        PracticeServer.log.info("[ItemVendors] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents((Listener) this, PracticeServer.plugin);
    }

    public void onDisable() {
        PracticeServer.log.info("[ItemVendors] has been disabled.");
    }

    public static Integer getPriceFromLore(ItemStack is) {
        int price = 0;
        if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()) {
            for (String line : is.getItemMeta().getLore()) {
                if (!line.contains("Price: ")) continue;
                String val = line;
                val = ChatColor.stripColor((String) val);
                val = val.substring(7, val.length() - 1);
                try {
                    price = Integer.parseInt(val);
                    continue;
                } catch (Exception e) {
                    price = 0;
                }
            }
        }
        return price;
    }

    public static Integer getGuildPriceFromLore(ItemStack is) {
        int price = 0;
        if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()) {
            for (String line : is.getItemMeta().getLore()) {
                if (!line.contains("Price: ")) continue;
                String val = line;
                val = ChatColor.stripColor((String) val);
                val = val.substring(7, val.length() - 2);
                try {
                    price = Integer.parseInt(val);
                    continue;
                } catch (Exception e) {
                    price = 0;
                }
            }
        }
        return price;
    }

    ItemStack food(int type) {
        ItemStack is = new ItemStack(Material.BREAD);
        int price = 2;
        if (type == 0) {
            is.setType(Material.MELON);
            price = 2;
        }
        if (type == 1) {
            is.setType(Material.APPLE);
            price = 4;
        }
        if (type == 2) {
            is.setType(Material.BREAD);
            price = 5;
        }
        if (type == 3) {
            is.setType(Material.PUMPKIN_PIE);
            price = 8;
        }
        if (type == 4) {
            is.setType(Material.COOKED_BEEF);
            price = 10;
        }
        ItemMeta im = is.getItemMeta();
        im.setLore(Arrays.asList(ChatColor.GREEN + "Price: " + ChatColor.WHITE + price + "g"));
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack createFishingPole(int tier) {
        ItemStack rawStack = null;
        String name = "";
        ArrayList<String> lore = new ArrayList<>();
        rawStack = new ItemStack(Material.FISHING_ROD, 1);

        ItemMeta meta = rawStack.getItemMeta();
        meta.addEnchant(Enchantment.LURE, 3, false);
        String expBar = ChatColor.RED + "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
        int lvl = 100;
        lore.add(ChatColor.GRAY.toString() + "Level: " + ChatColor.GREEN + lvl);
        lore.add(ChatColor.GRAY.toString() + 0 + ChatColor.GRAY.toString() + " / " + ChatColor.GRAY + Mining.getEXPNeeded(lvl));
        lore.add(ChatColor.GRAY.toString() + "EXP: " + expBar);

        switch (tier) {
            case 1:
                name = ChatColor.WHITE + "Basic Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of wood and thread.");
                break;
            case 2:
                name = ChatColor.GREEN.toString() + "Advanced Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of oak wood and thread.");
                break;
            case 3:
                name = ChatColor.AQUA.toString() + "Expert Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of ancient oak wood and spider silk.");
                break;
            case 4:
                name = ChatColor.LIGHT_PURPLE.toString() + "Supreme Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of jungle bamboo and spider silk.");
                break;
            case 5:
                name = ChatColor.YELLOW.toString() + "Master Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of rich mahogany and enchanted silk");
                break;
            case 6:
                name = ChatColor.BLUE.toString() + "Grand Master Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of frozen birch and enchanted silk");
                break;
            default:
                break;
        }

        meta.setDisplayName(name);
        meta.setLore(lore);
        rawStack.setItemMeta(meta);
        rawStack.addEnchantment(Enchantment.LURE, 3);
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.setString("type", "rod");
        tag.setInt("itemTier", 6);
        tag.setInt("level", 120);
        tag.setInt("XP", 0);
        tag.setInt("maxXP", Fishing.getEXPNeeded(lvl));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static ItemStack createFishingPoleShop(int tier) {
        ItemStack rawStack = null;
        String name = "";
        ArrayList<String> lore = new ArrayList<>();
        rawStack = new ItemStack(Material.FISHING_ROD, 1);

        ItemMeta meta = rawStack.getItemMeta();
        meta.addEnchant(Enchantment.LURE, 3, false);
        String expBar = ChatColor.RED + "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
        int lvl = 100;
        lore.add(ChatColor.GRAY.toString() + "Level: " + ChatColor.GREEN + lvl);
        lore.add(ChatColor.GRAY.toString() + 0 + ChatColor.GRAY.toString() + " / " + ChatColor.GRAY + Mining.getEXPNeeded(lvl));
        lore.add(ChatColor.GRAY.toString() + "EXP: " + expBar);

        switch (tier) {
            case 1:
                name = ChatColor.WHITE + "Basic Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of wood and thread.");
                break;
            case 2:
                name = ChatColor.GREEN.toString() + "Advanced Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of oak wood and thread.");
                break;
            case 3:
                name = ChatColor.AQUA.toString() + "Expert Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of ancient oak wood and spider silk.");
                break;
            case 4:
                name = ChatColor.LIGHT_PURPLE.toString() + "Supreme Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of jungle bamboo and spider silk.");
                break;
            case 5:
                name = ChatColor.YELLOW.toString() + "Master Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of rich mahogany and enchanted silk");
                break;
            case 6:
                name = ChatColor.BLUE.toString() + "Grand Master Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of frozen birch and enchanted silk");
                break;
            default:
                break;
        }
        lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + 2000 + "g");

        meta.setDisplayName(name);
        meta.setLore(lore);
        rawStack.setItemMeta(meta);
        rawStack.addEnchantment(Enchantment.LURE, 3);
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.setString("type", "rod");
        tag.setInt("itemTier", 6);
        tag.setInt("level", 120);
        tag.setInt("XP", 0);
        tag.setInt("maxXP", Fishing.getEXPNeeded(lvl));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }


    @EventHandler
    public void onBankClick(PlayerInteractEntityEvent e) {
    	if (DeployCommand.patchlockdown) {
        	e.setCancelled(true);
        	return;
        }
        if (e.getRightClicked() instanceof HumanEntity) {
            HumanEntity p = (HumanEntity) e.getRightClicked();
            if (p.getName() == null) {
                return;
            }
            if (!p.hasMetadata("NPC")) {
                return;
            }
            if(fixList.contains(e.getPlayer())) {
                return;
            }
            fixList.add(e.getPlayer());
            new BukkitRunnable() {
                @Override
                public void run() {
                    fixList.remove(e.getPlayer());
                }
            }.runTaskLaterAsynchronously(PracticeServer.getInstance(), 5L);

            if (p.getName().equals("Banker")) {
                e.getPlayer().sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "Use these bank chests to store your precious items.");
            } else if(p.getName().equalsIgnoreCase("Medic")) {
                e.getPlayer().sendMessage(ChatColor.GREEN + "You are all healed up, get in there!");
                e.getPlayer().setHealth(e.getPlayer().getMaxHealth());
                e.getPlayer().playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 10, 10);
            } else if (p.getName().equals("Item Vendor")) {
                e.getPlayer().sendMessage(ChatColor.GRAY + "Item Vendor: " + ChatColor.WHITE + "I will take your gems in return for special items.");
                Inventory inv = Bukkit.getServer().createInventory(null, 18, "Item Vendor");
                inv.addItem(new ItemStack[]{Items.orb(true)});
                inv.addItem(new ItemStack[]{Items.legendaryOrb(true)});
                inv.addItem(new ItemStack[]{Items.enchant(1, 0, true)});
                inv.addItem(new ItemStack[]{Items.enchant(1, 1, true)});
                inv.addItem(new ItemStack[]{Items.enchant(2, 0, true)});
                inv.addItem(new ItemStack[]{Items.enchant(2, 1, true)});
                inv.addItem(new ItemStack[]{Items.enchant(3, 0, true)});
                inv.addItem(new ItemStack[]{Items.enchant(3, 1, true)});
                inv.addItem(new ItemStack[]{Items.enchant(4, 0, true)});
                inv.addItem(new ItemStack[]{Items.enchant(4, 1, true)});
                inv.addItem(new ItemStack[]{Items.enchant(5, 0, true)});
                inv.addItem(new ItemStack[]{Items.enchant(5, 1, true)});
                if(PracticeServer.t6) {
                    inv.addItem(new ItemStack[]{Items.enchant(6, 0, true)});
                    inv.addItem(new ItemStack[]{Items.enchant(6, 1, true)});
                }
                e.getPlayer().openInventory(inv);
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);
            } else if (p.getName().equals("Upgrade Vendor")) {
                e.getPlayer().sendMessage(ChatColor.GRAY + "Upgrade Vendor: " + ChatColor.WHITE + "I will take your tokens in exchange for permanent upgrades.");
                Inventory inv = Bukkit.getServer().createInventory(null, 18, "Upgrade Vendor");
                Player pl = e.getPlayer();
                PersistentPlayer pp = PersistentPlayers.persistentPlayers.get(pl.getUniqueId());
                inv.addItem(PersistentPlayers.getItem("Mount", pp.mount));
                inv.addItem(PersistentPlayers.getItem("BankPages", pp.bankpages));
                inv.addItem(PersistentPlayers.getItem("Pickaxe", pp.pickaxe));
                inv.addItem(PersistentPlayers.getItem("Farmer", pp.farmer));
                inv.addItem(PersistentPlayers.getItem("LastStand", pp.laststand));
                inv.addItem(PersistentPlayers.getItem("OrbRolls", pp.orbrolls));
                inv.addItem(PersistentPlayers.getItem("Luck", pp.luck));
                inv.addItem(PersistentPlayers.getItem("Reaper", pp.reaper));
                inv.addItem(PersistentPlayers.getItem("KitWeapon", pp.kitweapon));
                inv.addItem(PersistentPlayers.getItem("KitHelm", pp.kithelm));
                inv.addItem(PersistentPlayers.getItem("KitChest", pp.kitchest));
                inv.addItem(PersistentPlayers.getItem("KitLegs", pp.kitlegs));
                inv.addItem(PersistentPlayers.getItem("KitBoots", pp.kitboots));
                inv.setItem(17, PersistentPlayers.getItem("Tokens", pp.tokens));

                e.getPlayer().openInventory(inv);
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);
            } else if (p.getName().equals("Fisherman")) {
                e.getPlayer().sendMessage(ChatColor.GRAY + "Fisherman: " + ChatColor.WHITE + "These fish can give you special powers.");
                Inventory inv = Bukkit.getServer().createInventory(null, 9, "Fisherman");
                inv.addItem(new ItemStack[]{Speedfish.fish(2, true)});
                inv.addItem(new ItemStack[]{Speedfish.fish(3, true)});
                inv.addItem(new ItemStack[]{Speedfish.fish(4, true)});
                inv.addItem(new ItemStack[]{Speedfish.fish(5, true)});
                e.getPlayer().openInventory(inv);
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);
            } else if (p.getName().equals("Food Vendor")) {
                Inventory inv = Bukkit.getServer().createInventory(null, 9, "Food Vendor");
                inv.addItem(new ItemStack[]{this.food(0)});
                inv.addItem(new ItemStack[]{this.food(1)});
                inv.addItem(new ItemStack[]{this.food(2)});
                inv.addItem(new ItemStack[]{this.food(3)});
                inv.addItem(new ItemStack[]{this.food(4)});
                e.getPlayer().openInventory(inv);
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);
            } else if (p.getName().equals("Book Vendor")) {
                Inventory inv = Bukkit.getServer().createInventory(null, 18, "Book Vendor");
                inv.addItem(new ItemStack[]{TeleportBooks.deadpeaks_book(true)});
                inv.addItem(new ItemStack[]{TeleportBooks.tripoli_book(true)});
                inv.addItem(new ItemStack[]{TeleportBooks.avalonBook(true)});
                e.getPlayer().openInventory(inv);
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);
            }else if(p.getName().equalsIgnoreCase("Trick or Treat")) { //Halloween Vendor
                PracticeServer.getManagerHandler().getHalloween().trickOrTreat(e.getPlayer(), false);
                return;
            } else if (p.getName().equals("Dungeoneer")) {
                ScrollGUI scrollGUI = new ScrollGUI(e.getPlayer());
                scrollGUI.openFor(e.getPlayer());
            }
        }
    }


    /*Halloween Candy Shit*/
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
    	if (DeployCommand.patchlockdown) {
        	e.setCancelled(true);
        	return;
        }
        Player p = (Player) e.getWhoClicked();
        if (e.getInventory().getTitle().contains("ArmorSee")) {
            e.setCancelled(true);
            return;
        }
        if (e.getInventory().getTitle().equals("Item Vendor")) {
            List<String> lore;
            e.setCancelled(true);
            if (e.getCurrentItem() != null && (e.getCurrentItem().getType() == Material.MAGMA_CREAM || e.getCurrentItem().getType() == Material.EMPTY_MAP) && e.getCurrentItem().getItemMeta().hasLore() && ((String) (lore = e.getCurrentItem().getItemMeta().getLore()).get(lore.size() - 1)).contains("Price:")) {
                int price = ItemVendors.getPriceFromLore(e.getCurrentItem());
                if (Money.hasEnoughGems(p, price)) {
                    ItemStack is = new ItemStack(e.getCurrentItem().getType());
                    ItemMeta im = is.getItemMeta();
                    im.setDisplayName(e.getCurrentItem().getItemMeta().getDisplayName());
                    lore.remove(lore.size() - 1);
                    im.setLore(lore);
                    is.setItemMeta(im);
                    buyingitem.put(p.getName(), is);
                    buyingprice.put(p.getName(), price);
                    p.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "QUANTITY" + ChatColor.GREEN + " you'd like to purchase.");
                    p.sendMessage(ChatColor.GRAY + "MAX: 64X (" + price * 64 + "g), OR " + price + "g/each.");
                    p.closeInventory();
                } else {
                    p.sendMessage(ChatColor.RED + "You do NOT have enough gems to purchase this " + e.getCurrentItem().getItemMeta().getDisplayName());
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COST: " + ChatColor.RED + price + ChatColor.BOLD + "G");
                    p.closeInventory();
                }
            }
        } else if (e.getCurrentItem() != null && e.getInventory().getTitle().equals("Fisherman")) {
            List<String> lore;
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.RAW_FISH && e.getCurrentItem().getItemMeta().hasLore() && ((String) (lore = e.getCurrentItem().getItemMeta().getLore()).get(lore.size() - 1)).contains("Price:")) {
                int price = ItemVendors.getPriceFromLore(e.getCurrentItem());
                if (Money.hasEnoughGems(p, price)) {
                    ItemStack is = new ItemStack(e.getCurrentItem().getType());
                    ItemMeta im = is.getItemMeta();
                    im.setDisplayName(e.getCurrentItem().getItemMeta().getDisplayName());
                    lore.remove(lore.size() - 1);
                    im.setLore(lore);
                    is.setItemMeta(im);
                    buyingitem.put(p.getName(), is);
                    buyingprice.put(p.getName(), price);
                    p.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "QUANTITY" + ChatColor.GREEN + " you'd like to purchase.");
                    p.sendMessage(ChatColor.GRAY + "MAX: 64X (" + price * 64 + "g), OR " + price + "g/each.");
                    p.closeInventory();
                } else {
                    p.sendMessage(ChatColor.RED + "You do NOT have enough gems to purchase this " + e.getCurrentItem().getItemMeta().getDisplayName());
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COST: " + ChatColor.RED + price + ChatColor.BOLD + "G");
                    p.closeInventory();
                }
            }
        } else if (e.getCurrentItem() != null && (e.getInventory().getTitle().equals("Food Vendor") || e.getInventory().getTitle().equals("Book Vendor"))) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && (e.getCurrentItem().getType() == Material.BOOK || e.getCurrentItem().getType() == Material.MELON || e.getCurrentItem().getType() == Material.APPLE || e.getCurrentItem().getType() == Material.BREAD || e.getCurrentItem().getType() == Material.PUMPKIN_PIE || e.getCurrentItem().getType() == Material.COOKED_BEEF) && e.getCurrentItem().getItemMeta().hasLore() && ((String) (e.getCurrentItem().getItemMeta().getLore()).get(0)).contains("Price:")) {
                int price = ItemVendors.getPriceFromLore(e.getCurrentItem());
                if (Money.hasEnoughGems(p, price)) {
                    ItemStack is = new ItemStack(e.getCurrentItem().getType());
                    buyingitem.put(p.getName(), is);
                    buyingprice.put(p.getName(), price);
                    p.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "QUANTITY" + ChatColor.GREEN + " you'd like to purchase.");
                    p.sendMessage(ChatColor.GRAY + "MAX: 64X (" + price * 64 + "g), OR " + price + "g/each.");
                    p.closeInventory();
                } else {
                    p.sendMessage(ChatColor.RED + "You do NOT have enough gems to purchase this " + e.getCurrentItem().getType().name());
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COST: " + ChatColor.RED + price + ChatColor.BOLD + "G");
                    p.closeInventory();
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPromptChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (buyingitem.containsKey(p.getName()) && buyingprice.containsKey(p.getName())) {
            e.setCancelled(true);
            int price = buyingprice.get(p.getName());
            ItemStack is = buyingitem.get(p.getName());
            int amt = 0;
            if (e.getMessage().equalsIgnoreCase("cancel")) {
                p.sendMessage(ChatColor.RED + "Purchase of item - " + ChatColor.BOLD + "CANCELLED");
                buyingprice.remove(p.getName());
                buyingitem.remove(p.getName());
                return;
            }
            try {
                amt = Integer.parseInt(e.getMessage());
            } catch (Exception ex) {
                p.sendMessage(ChatColor.RED + "Please enter a valid integer, or type 'cancel' to void this item purchase.");
                return;
            }
            if (amt < 1) {
                p.sendMessage(ChatColor.RED + "You cannot purchase a NON-POSITIVE number.");
                return;
            }
            if (amt > 64) {
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " buy MORE than " + ChatColor.BOLD + "64x" + ChatColor.RED + " of a material per transaction.");
                return;
            }
            if (!Money.hasEnoughGems(p, amt * price)) {
                p.sendMessage(ChatColor.RED + "You do not have enough GEM(s) to complete this purchase.");
                p.sendMessage(ChatColor.GRAY.toString() + amt + " X " + price + " gem(s)/ea = " + amt * price + " gem(s).");
                return;
            }
            int empty = 0;
            ItemStack hand = p.getInventory().getItemInMainHand();
            if(is.getType() == Material.WOOD_PICKAXE){
                PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
                for(int i = 0; i < (pp.pickaxe * 20)-1; i++){
                    ProfessionMechanics.addExp(p, is, 10000, false);
                }
            }
            p.getInventory().setItemInMainHand(hand);
            if (is.getMaxStackSize() == 1) {
                int i = 0;
                while (i < p.getInventory().getSize()) {
                    if (p.getInventory().getItem(i) == null || p.getInventory().getItem(i).getType() == Material.AIR) {
                        ++empty;
                    }
                    ++i;
                }
                if (amt > empty) {
                    p.sendMessage(ChatColor.RED + "No space available in inventory. Type 'cancel' or clear some room.");
                } else {
                    i = 0;
                    while (i < amt) {
                        p.getInventory().setItem(p.getInventory().firstEmpty(), is);
                        ++i;
                    }
                    p.sendMessage(ChatColor.RED + "-" + amt * price + ChatColor.BOLD + "G");
                    p.sendMessage(ChatColor.GREEN + "Transaction successful.");
                    Money.takeGems(p, amt * price);
                    buyingprice.remove(p.getName());
                    buyingitem.remove(p.getName());
                }
            } else {
                if (p.getInventory().firstEmpty() == -1) {
                    p.sendMessage(ChatColor.RED + "No space available in inventory. Type 'cancel' or clear some room.");
                    return;
                }
                p.sendMessage(ChatColor.RED + "-" + amt * price + ChatColor.BOLD + "G");
                p.sendMessage(ChatColor.GREEN + "Transaction successful.");
                Money.takeGems(p, amt * price);
                is.setAmount(amt);
                p.getInventory().setItem(p.getInventory().firstEmpty(), is);
                buyingprice.remove(p.getName());
                buyingitem.remove(p.getName());
            }
        }
    }
}

