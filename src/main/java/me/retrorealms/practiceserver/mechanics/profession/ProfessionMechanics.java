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
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
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
    public void onPlayerFish(PlayerFishEvent e) {
        final Player pl = e.getPlayer();
        e.setExpToDrop(0);

        if (pl.getEquipment().getItemInMainHand().getType() != Material.FISHING_ROD) {
            e.setCancelled(true);
            return; // Get out of here.
        }

        if (e.getState().equals(PlayerFishEvent.State.FISHING)) {
            if (!Fishing.getInstance().inFishingRegion(pl)) {
                e.getPlayer().sendMessage(ChatColor.RED + "There are " + ChatColor.UNDERLINE + "no" + ChatColor.RED + " populated fishing spots near this location.");
                e.getPlayer().sendMessage(ChatColor.GRAY + "Look for particles above water blocks to signify active fishing spots.");
                e.setCancelled(true);
                return;
            }
        }

        if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            final int spot_tier = Fishing.getInstance().getFishingSpotTier(pl.getLocation());
            if (e.getCaught() != null)
                e.getCaught().remove();

            if (!Fishing.getInstance().inFishingRegion(pl)) {
                pl.sendMessage(ChatColor.RED + "You must be near a Fishing Location to catch fish!");
                return;
            }

            int duraBuff = Fishing.getDurabilityBuff(pl.getEquipment().getItemInMainHand());

            pl.sendMessage(ChatColor.GRAY + "You've caught something, examining..");
            int do_i_get_fish = new Random().nextInt(90);

            int item_tier = Fishing.getRodTier(pl.getEquipment().getItemInMainHand());
            int success_rate = 0;

            if (item_tier > spot_tier) {
                success_rate = 100;
            }
            int lvl = CraftItemStack.asNMSCopy(pl.getEquipment().getItemInMainHand()).getTag().getInt("level");
            success_rate = 50 + (2 * (20 - Math.abs((Fishing.getNextLevelUp(item_tier) - lvl))));


            int success_mod = Fishing.getSuccessChance(pl.getEquipment().getItemInMainHand());
            success_rate += success_mod; // %CHANCE

            if (success_rate <= do_i_get_fish) {
                pl.sendMessage(ChatColor.RED + "Mhm, looks like it got away..");
                return;
            }

            if (Fishing.isDRFishingPole(pl.getEquipment().getItemInMainHand())) {
                // They get fish!
                ItemStack fish = Fishing.getFishDrop(spot_tier, pl);
                fish.setAmount(1);

                if (ModerationMechanics.getRank(pl) == RankEnum.SUPPORTER || ModerationMechanics.getRank(pl) == RankEnum.SUB3) {
                    fish.setAmount(3);
                    pl.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&lSUPPORTER &b>> &e&nx3 FISH"));
                }

                if (ModerationMechanics.getRank(pl) == RankEnum.SUB || ModerationMechanics.getRank(pl) == RankEnum.SUB1 || ModerationMechanics.getRank(pl) == RankEnum.SUB2) {
                    fish.setAmount(2);
                    pl.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lSUB &b>> &e&nx2 FISH"));
                }

                if (pl.getInventory().firstEmpty() != -1) {
                    pl.getInventory().setItem(pl.getInventory().firstEmpty(), fish);
                } else {
                    // Full inventory!
                    pl.getWorld().dropItem(pl.getLocation(), fish);
                }
                if (new Random().nextInt(100) > duraBuff) {

                }
                pl.sendMessage(ChatColor.GREEN + "...you've caught some " + fish.getItemMeta().getDisplayName() + ChatColor.GREEN + "!");

                int exp = Fishing.getFishEXP(spot_tier);
                Fishing.gainExp(pl.getEquipment().getItemInMainHand(), pl, exp);
                int doi_double_drop = new Random().nextInt(100) + 1;
                if (Fishing.getDoubleDropChance(pl.getEquipment().getItemInMainHand()) >= doi_double_drop) {
                    fish = Fishing.getFishDrop(spot_tier, pl);

                    fish.setAmount(2);

                    if (ModerationMechanics.getRank(pl) == RankEnum.SUPPORTER || ModerationMechanics.getRank(pl) == RankEnum.YOUTUBER || ModerationMechanics.getRank(pl) == RankEnum.SUB3) {
                        fish.setAmount(3);
                        pl.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&lSUPPORTER &b>> &e&nx3 FISH"));
                    }

                    if (ModerationMechanics.getRank(pl) == RankEnum.SUB || ModerationMechanics.getRank(pl) == RankEnum.SUB1 || ModerationMechanics.getRank(pl) == RankEnum.SUB2) {
                        fish.setAmount(2);
                        pl.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lSUB &b>> &e&nx2 FISH"));
                    }

                    if (pl.getInventory().firstEmpty() != -1) {
                        pl.getInventory().setItem(pl.getInventory().firstEmpty(), fish);
                    } else {
                        // Full inventory!
                        pl.getWorld().dropItem(pl.getLocation(), fish);
                    }
                    pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          DOUBLE FISH CATCH" + ChatColor.YELLOW + " (2x)");

                }

                int doi_triple_drop = new Random().nextInt(60) + 1;
                if (Fishing.getTripleDropChance(pl.getEquipment().getItemInMainHand()) >= doi_triple_drop) {
                    fish = Fishing.getFishDrop(spot_tier, pl);

                    fish.setAmount(3);

                    if (ModerationMechanics.getRank(pl) == RankEnum.SUPPORTER || ModerationMechanics.getRank(pl) == RankEnum.YOUTUBER || ModerationMechanics.getRank(pl) == RankEnum.SUB3) {
                        fish.setAmount(3);
                        pl.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&lSUPPORTER &b>> &e&nx3 FISH"));
                    }

                    if (ModerationMechanics.getRank(pl) == RankEnum.SUB || ModerationMechanics.getRank(pl) == RankEnum.SUB1 || ModerationMechanics.getRank(pl) == RankEnum.SUB2) {
                        fish.setAmount(2);
                        pl.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lSUB &b>> &e&nx2 FISH"));
                    }
                    if (pl.getInventory().firstEmpty() != -1) {
                        pl.getInventory().setItem(pl.getInventory().firstEmpty(), fish);
                    } else {
                        // Full inventory!
                        pl.getWorld().dropItem(pl.getLocation(), fish);
                    }

                    fish = Fishing.getFishDrop(spot_tier, pl);
                    if (pl.getInventory().firstEmpty() != -1) {
                        pl.getInventory().setItem(pl.getInventory().firstEmpty(), fish);
                    } else {
                        // Full inventory!
                        pl.getWorld().dropItem(pl.getLocation(), fish);
                    }
                    pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          TRIPLE FISH CATCH" + ChatColor.YELLOW + " (3x)");
                }
                int junk_chance = Fishing.getJunkFindChance(pl.getEquipment().getItemInMainHand());
                if (junk_chance >= (new Random().nextInt(100) + 1)) {
                    int junk_type = new Random().nextInt(100) + 1; // 0, 1, 2
                    ItemStack junk = null;
                    if (junk_type > 70 && junk_type < 95) {
                        if (spot_tier == 1) {
                            junk = MerchantMechanics.T4_scrap;
                            junk.setAmount(5 + new Random().nextInt(3));
                        }
                        if (spot_tier == 2) {
                            junk = MerchantMechanics.T4_scrap;
                            junk.setAmount(4 + new Random().nextInt(3));
                        }
                        if (spot_tier == 3) {
                            junk = MerchantMechanics.T4_scrap;
                            junk.setAmount(2 + new Random().nextInt(3));
                        }
                        if (spot_tier == 4) {
                            junk = MerchantMechanics.T5_scrap;
                            junk.setAmount(1 + new Random().nextInt(3));
                        }
                        if (spot_tier == 5) {
                            junk = MerchantMechanics.T5_scrap;
                            junk.setAmount(1 + new Random().nextInt(3));
                        }
                    }

                    if (junk_type >= 95) {
                        if (spot_tier == 1) {
                            junk = MerchantMechanics.T5_scrap;
                            junk.setAmount(20 + new Random().nextInt(7));
                        }
                        if (spot_tier == 2) {
                            junk = MerchantMechanics.T5_scrap;
                            junk.setAmount(15 + new Random().nextInt(7));
                        }
                        if (spot_tier == 3) {
                            junk = MerchantMechanics.T5_scrap;
                            junk.setAmount(10 + new Random().nextInt(7));
                        }
                        if (spot_tier == 4) {
                            junk = MerchantMechanics.T5_scrap;
                            junk.setAmount(5 + new Random().nextInt(7));
                        }
                        if (spot_tier == 5) {
                            junk = MerchantMechanics.T5_scrap;
                            junk.setAmount(2 + new Random().nextInt(6));
                        }
                    }

                    if (junk != null) {
                        int item_count = junk.getAmount();
                        if (junk.getType() == Material.POTION) {
                            // Not stackable.
                            int amount = junk.getAmount();
                            junk.setAmount(1);
                            while (amount > 0) {
                                amount--;
                                if (pl.getInventory().firstEmpty() != -1) {
                                    pl.getInventory().setItem(pl.getInventory().firstEmpty(), junk);
                                } else {
                                    // Full inventory!
                                    pl.getWorld().dropItem(pl.getLocation(), junk);
                                }
                            }
                        } else {
                            if (pl.getInventory().firstEmpty() != -1) {
                                pl.getInventory().setItem(pl.getInventory().firstEmpty(), junk);
                            } else {
                                // Full inventory!
                                pl.getWorld().dropItem(pl.getLocation(), junk);
                            }
                        }

                        pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "  YOU FOUND SOME JUNK! -- " + item_count + "x "
                                + junk.getItemMeta().getDisplayName());
                    }
                }

                int treasure_chance = Fishing.getTreasureFindChance(pl.getEquipment().getItemInMainHand());
                if (treasure_chance >= (new Random().nextInt(300) + 1)) {
                    // Give em treasure!
                    int treasure_type = new Random().nextInt(3); // 0, 1
                    ItemStack treasure = null;
                    if (treasure_type == 0) {
                        // OOA
                        treasure = Items.orb(false);
                    }

                    if (treasure_type == 1 || treasure_type == 2) {
                        treasure = Items.enchant(5, new Random().nextInt(1), false);
                    }

                    if (treasure != null) {

                        if (pl.getInventory().firstEmpty() != -1) {
                            pl.getInventory().setItem(pl.getInventory().firstEmpty(), treasure);
                        } else {
                            // Full inventory!
                            pl.getWorld().dropItem(pl.getLocation(), treasure);
                        }

                        pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "  YOU FOUND SOME TREASURE! -- a(n) "
                                + treasure.getItemMeta().getDisplayName());
                    }
                }
            }
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

