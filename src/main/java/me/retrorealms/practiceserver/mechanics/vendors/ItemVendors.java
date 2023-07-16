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

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.misc.LeaderboardCommand;
import me.retrorealms.practiceserver.commands.moderation.DeployCommand;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.item.scroll.ScrollGUI;
import me.retrorealms.practiceserver.mechanics.loot.contract.ContractHandler;
import me.retrorealms.practiceserver.mechanics.loot.contract.ContractMenu;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayer;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayers;
import me.retrorealms.practiceserver.mechanics.player.Speedfish;
import me.retrorealms.practiceserver.mechanics.profession.Fishing;
import me.retrorealms.practiceserver.mechanics.profession.Mining;
import me.retrorealms.practiceserver.mechanics.profession.ProfessionMechanics;
import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
import me.retrorealms.practiceserver.mechanics.world.MinigameState;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
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
    private static final String ITEM_VENDOR = "Item Vendor";
    private static final String FISHERMAN = "Fisherman";
    public static HashMap<String, ItemStack> buyingitem = new HashMap<String, ItemStack>();
    public static HashMap<String, Integer> buyingprice = new HashMap<String, Integer>();
    public static ArrayList<Player> fixList = new ArrayList<>();

    public static Integer getGuildPriceFromLore(ItemStack is) {
        int price = 0;
        if (is != null && is.getType() != Material.AIR && is.getItemMeta().hasLore()) {
            for (String line : is.getItemMeta().getLore()) {
                if (!line.contains("Price: ")) continue;
                String val = line;
                val = ChatColor.stripColor(val);
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

    public static ItemStack createFishingPole(int tier) {
        ItemStack rawStack = null;
        String name = "";

        ArrayList<String> lore = new ArrayList<>();
        rawStack = new ItemStack(Material.FISHING_ROD, 1);

        ItemMeta meta = rawStack.getItemMeta();
        meta.addEnchant(Enchantment.LURE, 3, false);
        String expBar = ChatColor.RED + "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
        int lvl = 100;
        lore.add(ChatColor.GRAY + "Level: " + ChatColor.GREEN + lvl);
        lore.add(ChatColor.GRAY.toString() + 0 + ChatColor.GRAY + " / " + ChatColor.GRAY + Mining.getEXPNeeded(lvl));
        lore.add(ChatColor.GRAY + "EXP: " + expBar);

        switch (tier) {
            case 1:
                name = ChatColor.WHITE + "Basic Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of wood and thread.");
                break;
            case 2:
                name = ChatColor.GREEN + "Advanced Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of oak wood and thread.");
                break;
            case 3:
                name = ChatColor.AQUA + "Expert Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of ancient oak wood and spider silk.");
                break;
            case 4:
                name = ChatColor.LIGHT_PURPLE + "Supreme Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of jungle bamboo and spider silk.");
                break;
            case 5:
                name = ChatColor.YELLOW + "Master Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of rich mahogany and enchanted silk");
                break;
            case 6:
                name = ChatColor.BLUE + "Grand Master Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of frozen birch and enchanted silk");
                break;
            default:
                break;
        }

        meta.setDisplayName(name);
        meta.setLore(lore);
        rawStack.setItemMeta(meta);
        rawStack.addEnchantment(Enchantment.LURE, 3);
        net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
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
        lore.add(ChatColor.GRAY + "Level: " + ChatColor.GREEN + lvl);
        lore.add(ChatColor.GRAY.toString() + 0 + ChatColor.GRAY + " / " + ChatColor.GRAY + Mining.getEXPNeeded(lvl));
        lore.add(ChatColor.GRAY + "EXP: " + expBar);

        switch (tier) {
            case 1:
                name = ChatColor.WHITE + "Basic Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of wood and thread.");
                break;
            case 2:
                name = ChatColor.GREEN + "Advanced Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of oak wood and thread.");
                break;
            case 3:
                name = ChatColor.AQUA + "Expert Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of ancient oak wood and spider silk.");
                break;
            case 4:
                name = ChatColor.LIGHT_PURPLE + "Supreme Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of jungle bamboo and spider silk.");
                break;
            case 5:
                name = ChatColor.YELLOW + "Master Fishingrod";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of rich mahogany and enchanted silk");
                break;
            case 6:
                name = ChatColor.BLUE + "Grand Master Fishingrod";
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
        net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.setString("type", "rod");
        tag.setInt("itemTier", 6);
        tag.setInt("level", 120);
        tag.setInt("XP", 0);
        tag.setInt("maxXP", Fishing.getEXPNeeded(lvl));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public void onEnable() {
        PracticeServer.log.info("[ItemVendors] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    public void onDisable() {
        PracticeServer.log.info("[ItemVendors] has been disabled.");
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
        im.setLore(Collections.singletonList(ChatColor.GREEN + "Price: " + ChatColor.WHITE + price + "g"));
        is.setItemMeta(im);
        return is;
    }

    private Hologram getHologramAtLocation(Location location) {
        for (Hologram hologram : HologramsAPI.getHolograms(PracticeServer.getInstance())) {
            Location hologramLocation = hologram.getLocation();
            if (hologramLocation.getWorld() != null && hologramLocation.getWorld().getUID().equals(location.getWorld().getUID())) {
                if (hologram.getLocation().distance(location) < 0.1) {
                    return hologram;
                }
            }
        }
        return null;
    }

    private boolean hasHologramAtLocation(Location location) {
        return getHologramAtLocation(location) != null;
    }

    @EventHandler
    public void onNPCRemove(NPCRemoveEvent e) {
        NPC npc = e.getNPC();
        Location npcLocation = e.getNPC().getEntity().getLocation();
        Location hologramLocation = new Location(npc.getEntity().getWorld(), npcLocation.getX(), npcLocation.getY() + 2.8, npcLocation.getZ(), npcLocation.getYaw(), npcLocation.getPitch());
        Hologram hologram = getHologramAtLocation(hologramLocation);
        if (hologram != null) {
            hologram.delete();
        }
    }

    @EventHandler
    public void onNPCSpawn(NPCSpawnEvent e) {
        NPC npc = e.getNPC();
        Location npcLocation = e.getNPC().getEntity().getLocation();
        Location hologramLocation = new Location(npc.getEntity().getWorld(), npcLocation.getX(), npcLocation.getY() + 2.8, npcLocation.getZ(), npcLocation.getYaw(), npcLocation.getPitch());

        if (hasHologramAtLocation(hologramLocation)) {
            return;
        }

        String hologramString = "";
        String npcName = npc.getName();
        switch (npcName) {
            case "Item Vendor":
            case "Merchant":
            case "Food Vendor":
            case "Guild Registrar":
            case "Fisherman":
            case "Animal Tamer":
            case "Skill Trainer":
            case "Dungeoneer":
            case "Book Vendor":
            case "Isaam":
                hologramString = ChatColor.GOLD + ChatColor.ITALIC.toString() + "Vendor";
                break;
            case "Upgrade Vendor":
                hologramString = ChatColor.YELLOW + ChatColor.ITALIC.toString() + "Permanent Upgrades";
                break;
            case "Chronicler":
                hologramString = ChatColor.LIGHT_PURPLE + ChatColor.ITALIC.toString() + "Leaderboards Menu";
                break;
            default:
                hologramString = ChatColor.GRAY + ChatColor.ITALIC.toString() + "NPC";
                break;
        }
        Hologram hologram = HologramsAPI.createHologram(PracticeServer.getInstance(), hologramLocation);
        hologram.appendTextLine(hologramString);
    }

    @EventHandler
    public void onBankClick(PlayerInteractEntityEvent e) {
        if (DeployCommand.patchlockdown) {
            e.setCancelled(true);
            return;
        }

        if (e.getRightClicked() instanceof HumanEntity) {
            HumanEntity clickedEntity = (HumanEntity) e.getRightClicked();
            if (!isNPC(clickedEntity)) {
                return;
            }
            if (isRecentlyInteracted(e.getPlayer())) {
                return;
            }
            addToRecentlyInteracted(e.getPlayer());

            String npcName = clickedEntity.getName();
            Player player = e.getPlayer();
            Inventory inventory = null;
            if(npcName.equalsIgnoreCase("Task Master")) {
                player.playSound(clickedEntity.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 10, 10);
                ContractMenu.openContractMenu(player);
                return;
            }
            switch (npcName) {
                case "Chronicler":
                    PracticeServer.getLeaderboard().openCategoryMenu(player);
                    break;
                case "Banker":
                    player.sendMessage(ChatColor.GRAY + "Banker: " + ChatColor.WHITE + "Use these bank chests to store your precious items.");
                    break;
                case "Medic":
                    if(PracticeServer.getRaceMinigame().getGameState() == MinigameState.SHRINK) break;
                    player.sendMessage(ChatColor.GREEN + "You are all healed up, get in there!");
                    player.setHealth(player.getMaxHealth());
                    player.playSound(clickedEntity.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 10, 10);
                    break;
                case "Book Vendor":
                    player.sendMessage(ChatColor.GRAY + "Book-Vendor: " + ChatColor.WHITE + "I will take your gems in return for Teleport Books.");
                    inventory = openBookVendorInventory(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);
                    break;
                case "Fisherman":
                    player.sendMessage(ChatColor.GRAY + "Fisherman: " + ChatColor.WHITE + "I will take your gems in return for Magical Fish.");
                    inventory = openFishermanInventory(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);
                    break;
                case "Dungeoneer":
                    player.sendMessage(ChatColor.GRAY + "Dungeoneer: " + ChatColor.WHITE + "I will take your gems in return for mystical items.");
                    ScrollGUI scrollGUI = new ScrollGUI(e.getPlayer());
                    scrollGUI.openFor(e.getPlayer());
                    player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 1.0f);
                    break;
                case "Item Vendor":
                    player.sendMessage(ChatColor.GRAY + "Item Vendor: " + ChatColor.WHITE + "I will take your gems in return for special items.");
                    inventory = openItemVendorInventory(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);
                    break;
                case "Upgrade Vendor":
                    player.sendMessage(ChatColor.GRAY + "Upgrade Vendor: " + ChatColor.WHITE + "I will take your tokens in exchange for permanent upgrades.");
                    inventory = openUpgradeVendorInventory(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);
                    break;
            }
            if (inventory != null) player.openInventory(inventory);
        }
    }

    private boolean isNPC(HumanEntity entity) {
        return entity.hasMetadata("NPC");
    }

    public static boolean isRecentlyInteracted(Player player) {
        return fixList.contains(player);
    }

    public static void addToRecentlyInteracted(Player player) {
        fixList.add(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                fixList.remove(player);
            }
        }.runTaskLaterAsynchronously(PracticeServer.getInstance(), 5L);
    }

    private Inventory openItemVendorInventory(Player player) {
        Inventory inv = Bukkit.getServer().createInventory(null, 18, "Item Vendor");
        inv.addItem(Items.orb(true));
        inv.addItem(Items.legendaryOrb(true));
        inv.addItem(Items.enchant(1, 0, true).clone());
        inv.addItem(Items.enchant(1, 1, true).clone());
        inv.addItem(Items.enchant(2, 0, true).clone());
        inv.addItem(Items.enchant(2, 1, true).clone());
        inv.addItem(Items.enchant(3, 0, true).clone());
        inv.addItem(Items.enchant(3, 1, true).clone());
        inv.addItem(Items.enchant(4, 0, true).clone());
        inv.addItem(Items.enchant(4, 1, true).clone());
        inv.addItem(Items.enchant(5, 0, true).clone());
        inv.addItem(Items.enchant(5, 1, true).clone());
        if (PracticeServer.t6) {
            inv.addItem(Items.enchant(6, 0, true));
            inv.addItem(Items.enchant(6, 1, true));
        }
        return inv;
    }

    private Inventory openFishermanInventory(Player player) {
        Inventory inv = Bukkit.getServer().createInventory(null, 18, "Item Vendor");
        inv.addItem(Speedfish.fish(2, true).clone());
        inv.addItem(Speedfish.fish(3, true).clone());
        inv.addItem(Speedfish.fish(4, true).clone());
        inv.addItem(Speedfish.fish(5, true).clone());
        return inv;
    }

    private Inventory openBookVendorInventory(Player player) {
        Inventory inv = Bukkit.getServer().createInventory(null, 18, "Book Vendor");
        inv.addItem(TeleportBooks.deadpeaksBook(true).clone());
        inv.addItem(TeleportBooks.tripoliBook(true).clone());
        inv.addItem(TeleportBooks.avalonBook(true).clone());
        return inv;
    }

    private Inventory openUpgradeVendorInventory(Player player) {
        Inventory inv = Bukkit.getServer().createInventory(null, 18, "Upgrade Vendor");
        PersistentPlayer pp = PersistentPlayers.persistentPlayers.get(player.getUniqueId());
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
        return inv;
    }

    @EventHandler
    public void onInvClick1(InventoryClickEvent event) {
        if (DeployCommand.patchlockdown) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if (inventory.getTitle().contains("ArmorSee")) {
            event.setCancelled(true);
            return;
        }

        if (inventory.getTitle().equalsIgnoreCase("Item Vendor") || inventory.getTitle().equalsIgnoreCase("Fisherman") || inventory.getTitle().equalsIgnoreCase("Book Vendor")) {
            handleInventoryClick(event, player);
        }
    }


    private void handleInventoryClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        List<String> lore = meta.getLore();

        if (lore == null) {
            return;
        }

        int price = getPriceFromLore(item);

        if (price < 0) {
            return;
        }

        if (!Money.hasEnoughGems(player, price)) {
            player.sendMessage(ChatColor.RED + "You do NOT have enough gems to purchase this " + item.getItemMeta().getDisplayName());
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COST: " + ChatColor.RED + price + ChatColor.BOLD + "G");
            player.closeInventory();
            return;
        }
        lore.remove(lore.size() - 1);

        meta.setLore(lore);
        item.setItemMeta(meta);
        buyingitem.put(player.getName(), item);
        buyingprice.put(player.getName(), price);
        player.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "QUANTITY" + ChatColor.GREEN + " you'd like to purchase.");
        player.sendMessage(ChatColor.GRAY + "MAX: 64X (" + price * 64 + "g), OR " + price + "g/each.");
        event.setCancelled(true);
        player.closeInventory();
    }

    public static int getPriceFromLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        for (String str : lore) {
            if (str.contains("Price:")) {
                String priceString = str.substring(str.indexOf(":") + 2).replaceFirst("[^\\d]+", "");
                priceString = priceString.substring(0, priceString.length() - 1);
                return Integer.parseInt(priceString);
            }
        }
        return -1;
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
            if (is.getType() == Material.WOOD_PICKAXE) {
                PersistentPlayer pp = PersistentPlayers.get(p.getUniqueId());
                for (int i = 0; i < (pp.pickaxe * 20) - 1; i++) {
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
                    p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 1F);
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

