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
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.player.PlayerInteractEntityEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.InventoryView
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 */
package me.retrorealms.practiceserver.mechanics.profession;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.vendors.ItemVendors;
import me.retrorealms.practiceserver.mechanics.vendors.MerchantMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class  ProfessionMechanics implements Listener {
    public void onEnable() {
        PracticeServer.log.info("[ProfessionMechanics] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
    }

    public void onDisable() {
        PracticeServer.log.info("[ProfessionMechanics] has been disabled.");
    }

    public static boolean isSkillItem(final ItemStack is) {
        return ((is.getType() == Material.WOOD_PICKAXE || is.getType() == Material.STONE_PICKAXE || is.getType() == Material.IRON_PICKAXE || is.getType() == Material.DIAMOND_PICKAXE || is.getType() == Material.GOLD_PICKAXE) && is.hasItemMeta() && is.getItemMeta().hasDisplayName()) || (is.getType() == Material.FISHING_ROD && is.hasItemMeta() && is.getItemMeta().hasDisplayName());
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        event.setExpToDrop(0);

        if (!isValidFishingRod(player)) {
            event.setCancelled(true);
            return;
        }

        if (event.getState() == PlayerFishEvent.State.FISHING && !Fishing.getInstance().inFishingRegion(player)) {
            player.sendMessage(ChatColor.RED + "There are " + ChatColor.UNDERLINE + "no" + ChatColor.RED +
                    " populated fishing spots near this location.");
            player.sendMessage(ChatColor.GRAY + "Look for particles above water blocks to signify active fishing spots.");
            event.setCancelled(true);
            return;
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            int spotTier = Fishing.getInstance().getFishingSpotTier(player.getLocation());
            if (event.getCaught() != null) {
                event.getCaught().remove();
            }

            if (!Fishing.getInstance().inFishingRegion(player)) {
                player.sendMessage(ChatColor.RED + "You must be near a Fishing Location to catch fish!");
                return;
            }

            int durabilityBuff = Fishing.getDurabilityBuff(player.getEquipment().getItemInMainHand());

            player.sendMessage(ChatColor.GRAY + "You've caught something, examining..");

            if (!canGetFish(player, spotTier)) {
                player.sendMessage(ChatColor.RED + "Mhm, looks like it got away..");
                return;
            }

            if (Fishing.isDRFishingPole(player.getEquipment().getItemInMainHand())) {
                handleFishCatch(player, spotTier, durabilityBuff);
            }
        }
    }

    private boolean isValidFishingRod(Player player) {
        return player.getEquipment().getItemInMainHand().getType() == Material.FISHING_ROD;
    }

    private boolean canGetFish(Player player, int spotTier) {
        int itemTier = Fishing.getRodTier(player.getEquipment().getItemInMainHand());
        int level = CraftItemStack.asNMSCopy(player.getEquipment().getItemInMainHand()).getTag().getInt("level");
        int successRate = calculateSuccessRate(player, itemTier, spotTier, level);
        int randomValue = new Random().nextInt(90);

        return successRate > randomValue;
    }

    private int calculateSuccessRate(Player player, int itemTier, int spotTier, int level) {
        int successRate = 50 + (2 * (20 - Math.abs((Fishing.getNextLevelUp(itemTier) - level))));
        int successMod = Fishing.getSuccessChance(player.getEquipment().getItemInMainHand());
        successRate += successMod;

        if (itemTier > spotTier) {
            successRate = 100;
        }

        return successRate;
    }

    private void handleFishCatch(Player player, int spotTier, int durabilityBuff) {
        ItemStack fish = Fishing.getFishDrop(spotTier, player);
        int fishAmount = calculateFishAmount(player);

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().setItem(player.getInventory().firstEmpty(), fish);
        } else {
            Item item = player.getWorld().dropItem(player.getLocation(), fish);
            item.setPickupDelay(0);
        }

        player.sendMessage(ChatColor.GREEN + "...you've caught some " + fish.getItemMeta().getDisplayName() + ChatColor.GREEN + "!");

        int exp = Fishing.getFishEXP(spotTier);
        Fishing.gainExp(player.getEquipment().getItemInMainHand(), player, exp);

        if (shouldDoubleDrop(player.getEquipment().getItemInMainHand())) {
            handleDoubleDrop(player, spotTier);
        }

        if (shouldTripleDrop(player.getEquipment().getItemInMainHand())) {
            handleTripleDrop(player, spotTier);
        }

        if (shouldFindJunk(player.getEquipment().getItemInMainHand())) {
            handleJunkFind(player, spotTier);
        }

        if (shouldFindTreasure(player.getEquipment().getItemInMainHand())) {
            handleTreasureFind(player);
        }
    }

    private int calculateFishAmount(Player player) {
        int fishAmount = 1;

        RankEnum rank = ModerationMechanics.getRank(player);
        if (rank == RankEnum.SUPPORTER || rank == RankEnum.SUB3) {
            fishAmount = 3;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&lSUPPORTER &b>> &e&nx3 FISH"));
        } else if (rank == RankEnum.SUB || rank == RankEnum.SUB1 || rank == RankEnum.SUB2) {
            fishAmount = 2;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lSUB &b>> &e&nx2 FISH"));
        }

        return fishAmount;
    }

    private boolean shouldDoubleDrop(ItemStack fishingRod) {
        int doubleDropChance = Fishing.getDoubleDropChance(fishingRod);
        int randomValue = new Random().nextInt(100) + 1;

        return doubleDropChance >= randomValue;
    }

    private void handleDoubleDrop(Player player, int spotTier) {
        ItemStack fish = Fishing.getFishDrop(spotTier, player);
        fish.setAmount(2);

        int fishAmount = calculateFishAmount(player);

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().setItem(player.getInventory().firstEmpty(), fish);
        } else {
            Item item = player.getWorld().dropItem(player.getLocation(), fish);
            item.setPickupDelay(0);
        }

        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          DOUBLE FISH CATCH" + ChatColor.YELLOW + " (2x)");
    }

    private boolean shouldTripleDrop(ItemStack fishingRod) {
        int tripleDropChance = Fishing.getTripleDropChance(fishingRod);
        int randomValue = new Random().nextInt(60) + 1;

        return tripleDropChance >= randomValue;
    }

    private void handleTripleDrop(Player player, int spotTier) {
        ItemStack fish = Fishing.getFishDrop(spotTier, player);
        fish.setAmount(3);

        int fishAmount = calculateFishAmount(player);

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().setItem(player.getInventory().firstEmpty(), fish);
        } else {
            Item item = player.getWorld().dropItem(player.getLocation(), fish);
            item.setPickupDelay(0);
        }

        fish = Fishing.getFishDrop(spotTier, player);

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().setItem(player.getInventory().firstEmpty(), fish);
        } else {
            Item item = player.getWorld().dropItem(player.getLocation(), fish);
            item.setPickupDelay(0);
        }

        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          TRIPLE FISH CATCH" + ChatColor.YELLOW + " (3x)");
    }

    private boolean shouldFindJunk(ItemStack fishingRod) {
        int junkChance = Fishing.getJunkFindChance(fishingRod);
        int randomValue = new Random().nextInt(100) + 1;

        return junkChance >= randomValue;
    }

    private void handleJunkFind(Player player, int spotTier) {
        int junkType = new Random().nextInt(100) + 1;
        ItemStack junk = null;

        if (junkType > 70 && junkType < 95) {
            if (spotTier == 1) {
                junk = MerchantMechanics.T4_scrap;
                junk.setAmount(5 + new Random().nextInt(3));
            } else if (spotTier == 2) {
                junk = MerchantMechanics.T4_scrap;
                junk.setAmount(4 + new Random().nextInt(3));
            } else if (spotTier == 3) {
                junk = MerchantMechanics.T4_scrap;
                junk.setAmount(2 + new Random().nextInt(3));
            } else if (spotTier == 4) {
                junk = MerchantMechanics.T5_scrap;
                junk.setAmount(1 + new Random().nextInt(3));
            } else if (spotTier == 5) {
                junk = MerchantMechanics.T5_scrap;
                junk.setAmount(1 + new Random().nextInt(3));
            }
        } else if (junkType >= 95) {
            if (spotTier == 1) {
                junk = MerchantMechanics.T5_scrap;
                junk.setAmount(20 + new Random().nextInt(7));
            } else if (spotTier == 2) {
                junk = MerchantMechanics.T5_scrap;
                junk.setAmount(15 + new Random().nextInt(7));
            } else if (spotTier == 3) {
                junk = MerchantMechanics.T5_scrap;
                junk.setAmount(10 + new Random().nextInt(7));
            } else if (spotTier == 4) {
                junk = MerchantMechanics.T5_scrap;
                junk.setAmount(5 + new Random().nextInt(7));
            } else if (spotTier == 5) {
                junk = MerchantMechanics.T5_scrap;
                junk.setAmount(2 + new Random().nextInt(6));
            }
        }

        if (junk != null) {
            int itemCount = junk.getAmount();
            if (junk.getType() == Material.POTION) {
                // Not stackable.
                int amount = junk.getAmount();
                junk.setAmount(1);
                while (amount > 0) {
                    amount--;
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().setItem(player.getInventory().firstEmpty(), junk);
                    } else {
                        Item item = player.getWorld().dropItem(player.getLocation(), junk);
                        item.setPickupDelay(0);
                    }
                }
            } else {
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().setItem(player.getInventory().firstEmpty(), junk);
                } else {
                    Item item = player.getWorld().dropItem(player.getLocation(), junk);
                    item.setPickupDelay(0);
                }
            }

            player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "  YOU FOUND SOME JUNK! -- " + itemCount + "x " +
                    junk.getItemMeta().getDisplayName());
        }
    }

    private boolean shouldFindTreasure(ItemStack fishingRod) {
        int treasureChance = Fishing.getTreasureFindChance(fishingRod);
        int randomValue = new Random().nextInt(300) + 1;

        return treasureChance >= randomValue;
    }

    private void handleTreasureFind(Player player) {
        int treasureType = new Random().nextInt(3);
        ItemStack treasure = null;

        if (treasureType == 0) {
            treasure = Items.orb(false);
        } else if (treasureType == 1 || treasureType == 2) {
            treasure = Items.enchant(5, new Random().nextInt(1), false);
        }

        if (treasure != null) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().setItem(player.getInventory().firstEmpty(), treasure);
            } else {
                Item item = player.getWorld().dropItem(player.getLocation(), treasure);
                item.setPickupDelay(0);
            }

            player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "  YOU FOUND SOME TREASURE! -- a(n) " +
                    treasure.getItemMeta().getDisplayName());
        }
    }

    @EventHandler
    public void onBankClick(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof HumanEntity) {
            HumanEntity p = (HumanEntity) e.getRightClicked();
            if (p.getName() == null) {
                return;
            }
            if (!p.hasMetadata("NPC")) {
                return;
            }
            if (p.getName().equals("Skill Trainer")) {
                Inventory inv = Bukkit.getServer().createInventory(null, 9, "Skill Trainer");
                ItemStack P = new ItemStack(Material.WOOD_PICKAXE);
                ItemMeta pickmeta = P.getItemMeta();
                pickmeta.setDisplayName(ChatColor.WHITE + "Novice Pickaxe");
                ArrayList<String> lore = new ArrayList<String>();
                lore.add(ChatColor.GRAY + "Level: " + ChatColor.WHITE + "1");
                lore.add(ChatColor.GRAY + "0 / 100");
                lore.add(ChatColor.GRAY + "EXP: " + ChatColor.RED + "||||||||||||||||||||||||||||||||||||||||||||||||||");
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of wood.");
                lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + "100g");
                pickmeta.setLore(lore);
                P.setItemMeta(pickmeta);
                inv.addItem(P);
                e.getPlayer().openInventory(inv);
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 1.0f);

                //inv.addItem(ItemVendors.createFishingPoleShop(5));
            }
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() != null && e.getInventory().getTitle().equals("Skill Trainer")) {
            List<String> lore;
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.WOOD_PICKAXE && e.getCurrentItem().getItemMeta().hasLore() && (lore = e.getCurrentItem().getItemMeta().getLore()).get(lore.size() - 1).contains("Price:")) {
                int price = ItemVendors.getPriceFromLore(e.getCurrentItem());
                if (Money.hasEnoughGems(p, price)) {
                    ItemStack is = new ItemStack(e.getCurrentItem().getType());
                    ItemMeta im = is.getItemMeta();
                    im.setDisplayName(e.getCurrentItem().getItemMeta().getDisplayName());
                    lore.remove(lore.size() - 1);
                    im.setLore(lore);
                    is.setItemMeta(im);
                    ItemVendors.buyingitem.put(p.getName(), is);
                    ItemVendors.buyingprice.put(p.getName(), price);
                    p.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "QUANTITY" + ChatColor.GREEN + " you'd like to purchase.");
                    p.sendMessage(ChatColor.GRAY + "MAX: 64X (" + price * 64 + "g), OR " + price + "g/each.");
                    p.closeInventory();
                } else {
                    p.sendMessage(ChatColor.RED + "You do NOT have enough gems to purchase this " + e.getCurrentItem().getItemMeta().getDisplayName());
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COST: " + ChatColor.RED + price + ChatColor.BOLD + "G");
                    p.closeInventory();
                }
            }
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.FISHING_ROD && e.getCurrentItem().getItemMeta().hasLore() && (lore = e.getCurrentItem().getItemMeta().getLore()).get(lore.size() - 1).contains("Price:")) {
                int price = ItemVendors.getPriceFromLore(e.getCurrentItem());
                if (Money.hasEnoughGems(p, price)) {
                    ItemStack is = ItemVendors.createFishingPole(1);
                    ItemVendors.buyingitem.put(p.getName(), is);
                    ItemVendors.buyingprice.put(p.getName(), price);
                    p.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "QUANTITY" + ChatColor.GREEN + " you'd like to purchase.");
                    p.sendMessage(ChatColor.GRAY + "MAX: 64X (" + price * 64 + "g), OR " + price + "g/each.");
                    p.closeInventory();
                } else {
                    p.sendMessage(ChatColor.RED + "You do NOT have enough gems to purchase this " + e.getCurrentItem().getItemMeta().getDisplayName());
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COST: " + ChatColor.RED + price + ChatColor.BOLD + "G");
                    p.closeInventory();
                }
            }
        }
    }

    public static ArrayList<Integer> getExp(ItemStack is) {
        ArrayList<Integer> exp = new ArrayList<Integer>();
        exp.add(0);
        exp.add(0);
        if (is != null && is.getType().name().contains("_PICKAXE") && is.getItemMeta().hasLore() && is.getItemMeta().getLore().size() > 1 && is.getItemMeta().getLore().get(1).contains(" / ")) {
            String line = ChatColor.stripColor(is.getItemMeta().getLore().get(1));
            try {
                exp.set(0, Integer.parseInt(line.split(" / ")[0]));
                exp.set(1, Integer.parseInt(line.split(" / ")[1]));
            } catch (Exception e) {
                return exp;
            }
        }
        return exp;
    }

    public static int getPickaxeLevel(ItemStack is) {
        int level = 0;
        if (is != null && is.getType().name().contains("_PICKAXE") && is.getItemMeta().hasLore() && is.getItemMeta().getLore().size() > 0 && is.getItemMeta().getLore().get(0).contains("Level: ")) {
            String line = ChatColor.stripColor(is.getItemMeta().getLore().get(0));
            try {
                level = Integer.parseInt(line.split("Level: ")[1]);
            } catch (Exception e) {
                return level;
            }
        }
        return level;
    }

    public static int getExpPerLevel(int level) {
        int xp = 100;
        int divide = 5;
        int i = 0;
        while (i < level) {
            if (i < 120) {
                divide = 50;
            }
            if (i < 100) {
                divide = 45;
            }
            if (i < 80) {
                divide = 35;
            }
            if (i < 60) {
                divide = 15;
            }
            if (i < 40) {
                divide = 20;
            }
            if (i < 20) {
                divide = 15;
            }
            xp += xp / divide;
            ++i;
        }
        if (level == 1) {
            xp = 100;
        }
        return xp;
    }

    public static void getDonorPicked(Player p) {
        int dblAmt = 15, gemAmt = 10, trplAmt = 10, succAmt = 20, tresAmt = 5, duraAmt = 15;
        boolean mag = false;
        RankEnum rank = ModerationMechanics.getRank(p);
        ItemStack P = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta pickmeta = P.getItemMeta();
        pickmeta.setDisplayName(ChatColor.BLUE + "Donator Pickaxe");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Level: " + ChatColor.BLUE + "120");
        lore.add(ChatColor.GRAY + "0 / 0");
        lore.add(ChatColor.GRAY + "EXP: " + ChatColor.BLUE + "||||||||||||||||||||||||||||||||||||||||||||||||||");

        switch (rank) {
            case SUB1:
                dblAmt = 20;
                gemAmt = 15;
                trplAmt = 15;
                succAmt = 25;
                tresAmt = 8;
                duraAmt = 20;
                break;
            case SUB2:
            case SUPPORTER:
                dblAmt = 25;
                gemAmt = 24;
                trplAmt = 20;
                succAmt = 30;
                tresAmt = 10;
                duraAmt = 30;
                mag = true;
                break;

        }
        lore.add(ChatColor.RED + "DOUBLE ORE: " + dblAmt + "%");
        lore.add(ChatColor.RED + "GEM FIND: " + gemAmt + "%");
        lore.add(ChatColor.RED + "TRIPLE ORE: " + trplAmt + "%");
        lore.add(ChatColor.RED + "TREASURE FIND: " + tresAmt + "%");
        lore.add(ChatColor.RED + "MINING SUCCESS: " + succAmt + "%");
        lore.add(ChatColor.RED + "DURABILITY: " + duraAmt + "%");

        if(mag)  lore.add(ChatColor.RED + "MAGNETISM");
        lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of ice.");
        pickmeta.setLore(lore);
        P.setItemMeta(pickmeta);
        p.getInventory().addItem(P);

    }

    public static ItemStack addExp(Player p, ItemStack is, int xp, boolean alerts) {
        GuildPlayer guildPlayer = GuildPlayers.getInstance().get(p.getUniqueId());
        if (is.getType() == Material.FISHING_ROD) {
            if(alerts) {
                guildPlayer.setFishCaught((guildPlayer.getFishCaught() + 1));
            }
        } else {
            if(alerts) {
                guildPlayer.setOreMined((guildPlayer.getOreMined() + 1));
            }
        }
        int currxp = ProfessionMechanics.getExp(is).get(0);
        int maxxp = ProfessionMechanics.getExp(is).get(1);
        int level = ProfessionMechanics.getPickaxeLevel(is);
        int tier = ProfessionMechanics.getPickaxeTier(is);
        ItemMeta im = is.getItemMeta();
        List<String> lore = im.getLore();
        if (maxxp > 0) {
            p.sendMessage("              " + ChatColor.YELLOW + ChatColor.BOLD + "+" + ChatColor.YELLOW + xp + ChatColor.BOLD + " EXP" + ChatColor.GRAY + " [" + (currxp + xp) + ChatColor.BOLD + "/" + ChatColor.GRAY + maxxp + " EXP]");
            if (currxp + xp >= maxxp) {
                level++;
                if(alerts){
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    p.sendMessage("          " + ChatColor.YELLOW + ChatColor.BOLD + "PICKAXE LEVEL UP! " + ChatColor.YELLOW + ChatColor.UNDERLINE + (level-1) + " -> " + level);
                }
                int newexp = ProfessionMechanics.getExpPerLevel(level);
                if (level == 20 || level == 40 || level == 60 || level == 80 || level == 100 || level == 120) {
                    if (tier < 6) {
                        ++tier;
                    }
                    if (tier == 2) {
                        is.setType(Material.STONE_PICKAXE);
                        im.setDisplayName(ChatColor.GREEN + "Apprentice Pickaxe");
                        lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of stone.");
                    }
                    if (tier == 3) {
                        is.setType(Material.IRON_PICKAXE);
                        im.setDisplayName(ChatColor.AQUA + "Expert Pickaxe");
                        lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of iron.");
                    }
                    if (tier == 4) {
                        is.setType(Material.DIAMOND_PICKAXE);
                        im.setDisplayName(ChatColor.LIGHT_PURPLE + "Supreme Pickaxe");
                        lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of diamond.");
                    }
                    if (tier == 5) {
                        is.setType(Material.GOLD_PICKAXE);
                        im.setDisplayName(ChatColor.YELLOW + "Legendary Pickaxe");
                        lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of gold.");
                    }
                    if (tier == 6) {
                        is.setType(Material.DIAMOND_PICKAXE);
                        if(level == 100) {
                            im.setDisplayName(ChatColor.BLUE + "Master Pickaxe");
                        }
                        if (level == 120) {
                            newexp = 0;
                            im.setDisplayName(ChatColor.BLUE + "Grand Master Pickaxe");
                        }
                        lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of ice.");
                    }
                    is.setDurability((short) 0);
                }
                lore.set(0, ChatColor.GRAY + "Level: " + ProfessionMechanics.getTierColor(tier) + level);
                lore.set(1, ChatColor.GRAY + "0 / " + newexp);
                lore.set(2, ChatColor.GRAY + "EXP: " + ProfessionMechanics.generateBar(0, newexp));
                im.setLore(lore);
                is.setItemMeta(im);
                if (level == 20 || level == 40 || level == 60 || level == 80 || level == 100 || level == 120) {
                    upgradePick(p, is);
                }
                p.getInventory().setItemInMainHand(is);
                return is;
            } else {
                if (maxxp != ProfessionMechanics.getExpPerLevel(level)) {
                    maxxp = ProfessionMechanics.getExpPerLevel(level);
                }
                lore.set(1, ChatColor.GRAY.toString() + (currxp + xp) + " / " + maxxp);
                lore.set(2, ChatColor.GRAY + "EXP: " + ProfessionMechanics.generateBar(currxp + xp, maxxp));
                im.setLore(lore);
                is.setItemMeta(im);
                p.getInventory().setItemInMainHand(is);
                return is;
            }
        }
        return is;
    }

    public static String generateBar(int curr, int max) {
        int percent = Math.round(50.0f * ((float) curr / (float) max));
        int barlength = 50;
        String bar = "";
        while (barlength > 0 && percent > 0) {
            --percent;
            --barlength;
            bar = String.valueOf(bar) + "|";
        }
        bar = ChatColor.GREEN + bar;
        bar = String.valueOf(bar) + ChatColor.RED;
        while (barlength > 0) {
            --barlength;
            bar = String.valueOf(bar) + "|";
        }
        if (max == 0) {
            bar = ChatColor.BLUE.toString();
            int i = 0;
            while (i < 50) {
                bar = String.valueOf(bar) + "|";
                ++i;
            }
        }
        return bar;
    }

    public static void upgradePick(Player p, ItemStack is) {
        Random r = new Random();
        int oreamt = r.nextInt(5) + 1;
        int gemamt = r.nextInt(10) + 1;
        int treasureamt = ThreadLocalRandom.current().nextInt(1) + 1;
        int successamt = ThreadLocalRandom.current().nextInt(20 + 1);
        int ench = r.nextInt(5);
        ItemMeta im = is.getItemMeta();
        CopyOnWriteArrayList<String> lore = new CopyOnWriteArrayList<String>(im.getLore());
        String desc = lore.get(lore.size() - 1);
        if (ench == 0) {
            if (ProfessionMechanics.getPickEnchants(is, "GEM FIND") == 10) {
                return;
            }
            if (ProfessionMechanics.getPickEnchants(is, "GEM FIND") >= gemamt && ProfessionMechanics.getPickEnchants(is, "GEM FIND") < 10) {
                gemamt = ProfessionMechanics.getPickEnchants(is, "GEM FIND") + 1;
            }
            for (String s : lore) {
                if (!s.contains("GEM FIND")) continue;
                lore.remove(s);
            }
            lore.set(lore.size() - 1, ChatColor.RED + "GEM FIND: " + gemamt + "%");
            p.sendMessage("          " + ChatColor.YELLOW + ChatColor.BOLD + "PICKAXE UPGRADED: " + ChatColor.RED + "GEM FIND: " + gemamt + "%");
        }
        if (ench == 1) {
            if (ProfessionMechanics.getPickEnchants(is, "DOUBLE ORE") == 5) {
                return;
            }
            if (ProfessionMechanics.getPickEnchants(is, "DOUBLE ORE") >= oreamt && ProfessionMechanics.getPickEnchants(is, "DOUBLE ORE") < 5) {
                oreamt = ProfessionMechanics.getPickEnchants(is, "DOUBLE ORE") + 1;
            }
            for (String s : lore) {
                if (!s.contains("DOUBLE ORE")) continue;
                lore.remove(s);
            }
            lore.set(lore.size() - 1, ChatColor.RED + "DOUBLE ORE: " + oreamt + "%");
            p.sendMessage("          " + ChatColor.YELLOW + ChatColor.BOLD + "PICKAXE UPGRADED: " + ChatColor.RED + "DOUBLE ORE: " + oreamt + "%");
        }
        if (ench == 2) {
            if (ProfessionMechanics.getPickEnchants(is, "TRIPLE ORE") == 5) {
                return;
            }
            if (ProfessionMechanics.getPickEnchants(is, "TRIPLE ORE") >= oreamt && ProfessionMechanics.getPickEnchants(is, "TRIPLE ORE") < 5) {
                oreamt = ProfessionMechanics.getPickEnchants(is, "TRIPLE ORE") + 1;
            }
            for (String s : lore) {
                if (!s.contains("TRIPLE ORE")) continue;
                lore.remove(s);
            }
            lore.set(lore.size() - 1, ChatColor.RED + "TRIPLE ORE: " + oreamt + "%");
            p.sendMessage("          " + ChatColor.YELLOW + ChatColor.BOLD + "PICKAXE UPGRADED: " + ChatColor.RED + "TRIPLE ORE: " + oreamt + "%");
        }

        if(ench == 3) {
            if (ProfessionMechanics.getPickEnchants(is, "MINING SUCCESS") == 20) {
                return;
            }
            if (ProfessionMechanics.getPickEnchants(is, "MINING SUCCESS") >= successamt && ProfessionMechanics.getPickEnchants(is, "MINING SUCCESS") < 20) {
                successamt = ProfessionMechanics.getPickEnchants(is, "MINING SUCCESS") + 1;
            }
            for (String s : lore) {
                if (!s.contains("MINING SUCCESS")) continue;
                lore.remove(s);
            }
            lore.set(lore.size() - 1, ChatColor.RED + "MINING SUCCESS: " + successamt + "%");
            p.sendMessage("          " + ChatColor.YELLOW + ChatColor.BOLD + "PICKAXE UPGRADED: " + ChatColor.RED + "MINING SUCCESS: " + successamt + "%");
        }
        if(ench == 4) {
            if (ProfessionMechanics.getPickEnchants(is, "TREASURE FIND") == 2) {
                return;
            }
            if (ProfessionMechanics.getPickEnchants(is, "TREASURE FIND") >= treasureamt && ProfessionMechanics.getPickEnchants(is, "TREASURE FIND") < 2) {
                treasureamt = ProfessionMechanics.getPickEnchants(is, "TREASURE FIND") + 1;
            }
            for (String s : lore) {
                if (!s.contains("TREASURE FIND")) continue;
                lore.remove(s);
            }
            lore.set(lore.size() - 1, ChatColor.RED + "TREASURE FIND: " + treasureamt + "%");
            p.sendMessage("          " + ChatColor.YELLOW + ChatColor.BOLD + "PICKAXE UPGRADED: " + ChatColor.RED + "TREASURE FIND: " + treasureamt + "%");
        }
        lore.add(desc);
        im.setLore(lore);
        is.setItemMeta(im);
    }
    public static boolean hasMagnetism(ItemStack itemStack){
        boolean hasMag = false;
        if (itemStack != null && itemStack.getType().name().contains("_PICKAXE") && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().getLore().size() > 4){
            for(String line : itemStack.getItemMeta().getLore()){
                if(line.contains("MAGNETISM")){
                    hasMag = true;
                }else continue;
            }
        }
        return hasMag;
    }
    public static int getPickEnchants(ItemStack is, String enchant) {
        int ench = 0;
        if (is != null && is.getType().name().contains("_PICKAXE") && is.getItemMeta().hasLore() && is.getItemMeta().getLore().size() > 4) {
            for (String s : is.getItemMeta().getLore()) {
                if (!s.contains(enchant)) continue;
                try {
                    ench = Integer.parseInt(s.split(String.valueOf(enchant) + ": ")[1].split("%")[0]);
                    continue;
                } catch (Exception e) {
                    return ench;
                }
            }
        }
        return ench;
    }

    public static int getPickaxeTier(ItemStack is) {
        if (is.getType() == Material.WOOD_PICKAXE) {
            return 1;
        }
        if (is.getType() == Material.STONE_PICKAXE) {
            return 2;
        }
        if (is.getType() == Material.IRON_PICKAXE) {
            return 3;
        }
        if (is.getType() == Material.DIAMOND_PICKAXE) {
            if(ProfessionMechanics.getPickaxeLevel(is) < 90){
                return 4;
            }
            return 6;
        }
        if (is.getType() == Material.GOLD_PICKAXE) {
            return 5;
        }
        return 0;
    }

    public static int getOreTier(Material m) {
        if (m == Material.COAL_ORE) {
            return 1;
        }
        if (m == Material.EMERALD_ORE) {
            return 2;
        }
        if (m == Material.IRON_ORE) {
            return 3;
        }
        if (m == Material.DIAMOND_ORE) {
            return 4;
        }
        if (m == Material.GOLD_ORE) {
            return 5;
        }
        if (m == Material.LAPIS_ORE) {
            return 6;
        }
        return 0;
    }

    public static ChatColor getTierColor(int tier) {
        ChatColor cc = ChatColor.WHITE;
        switch (tier) {
            case 1:
                cc = ChatColor.WHITE;
                break;

            case 2:
                cc = ChatColor.GREEN;
                break;

            case 3:
                cc = ChatColor.AQUA;
                break;

            case 4:
                cc = ChatColor.LIGHT_PURPLE;
                break;

            case 5:
                cc = ChatColor.YELLOW;
                break;

            case 6:
                cc = ChatColor.BLUE;
                break;
        }
        return cc;
    }

    public static int getFailPercent(int oretier, int level) {
        if (oretier == 1 && level > 19) {
            return 100;
        }
        if (oretier == 2 && level > 39) {
            return 100;
        }
        if (oretier == 3 && level > 59) {
            return 100;
        }
        if (oretier == 4 && level > 79) {
            return 100;
        }
        if (oretier == 5 && level > 99) {
            return 100;
        }
        if (oretier == 6 && level > 119) {
            return 100;
        }
        if (oretier == 1) {
            return 50 + (level - 1) * 2;
        }
        if (oretier == 2) {
            return 50 + (level - 20) * 2;
        }
        if (oretier == 3) {
            return 50 + (level - 40) * 2;
        }
        if (oretier == 4) {
            return 50 + (level - 60) * 2;
        }
        if (oretier == 5) {
            return 50 + (level - 80) * 2;
        }
        if (oretier == 6) {
            return 50 + (level - 100) * 2;
        }
        return 0;
    }

    public static int getExpFromOre(int tier) {
        if (tier == 1) {
            return 105;
        }
        if (tier == 2) {
            return 245;
        }
        if (tier == 3) {
            return 520;
        }
        if (tier == 4) {
            return 855;
        }
        if (tier == 5) {
            return 1055;
        }
        if (tier == 6) {
            return 1455;
        }
        return 0;
    }
}

