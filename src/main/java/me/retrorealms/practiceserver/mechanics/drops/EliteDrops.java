/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package me.retrorealms.practiceserver.mechanics.drops;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.itemapi.NBTAccessor;
import me.retrorealms.practiceserver.mechanics.enchants.Enchants;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.mobs.MobHandler;
import me.retrorealms.practiceserver.mechanics.mobs.elite.SkeletonElite;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.inventivetalent.glow.GlowAPI;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EliteDrops {

    @EventHandler
    public void onDaemonDeath(final EntityDamageByEntityEvent e) {
        final LivingEntity s = (LivingEntity) e.getEntity();
        if (MobHandler.isDaemon(s)) {
            SkeletonElite.getNearbyPlayers(s).forEach(player -> {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 10F, 0.1F);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Daemon Lord has been defeated"));
            });
        }
    }

    @EventHandler
    public void onDeathlordDeath(final EntityDamageByEntityEvent e) {
        final LivingEntity s = (LivingEntity) e.getEntity();
        if (MobHandler.isDeathlord(s)) {
            SkeletonElite.getNearbyPlayers(s).forEach(player -> {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 10F, 0.1F);
                player.sendMessage(ChatColor.DARK_RED + "The Infernal Abyss: My deathlord.. " + player.getName()
                        + ".. I'm coming for you.");
            });
        }
    }

    //random.nextInt(8) + 1;
    public static ItemStack createCustomDungeonDrop(String dungeonBoss, int item) {
        String name = "";
        String llore = ChatColor.GRAY.toString();
        ItemStack is = new ItemStack(Material.AIR);
        ArrayList<String> lore = new ArrayList<String>();
        Random random = new Random();
        int tier = 0;
        String rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
        int rarityId = 0;
        int armdps = 0;
        int nrghp = 0;
        int elem = 0;
        boolean pure = false;
        boolean life = false;
        boolean crit = false;
        boolean acc = false;
        boolean dodge = false;
        boolean block = false;
        boolean vit = false;
        boolean dex = false;
        boolean str = false;
        boolean intel = false;
        int hp = 0;
        int mindmg = 0;
        int maxdmg = 0;
        int dpsamt = 0;
        int dodgeamt = 0;
        int blockamt = 0;
        int vitamt = 0;
        int dexamt = 0;
        int stramt = 0;
        int intamt = 0;
        int elemamt = 0;
        int pureamt = 0;
        int lifeamt = 0;
        int critamt = 0;
        int accamt = 0;
        int hps = 0;
        int nrg = 0;


        // xd
        switch (dungeonBoss) {
            case "warden":
                nrghp = 2;
                armdps = 1;
                str = true;
                block = true;

                if (item <= 4) {

                    Random random1 = new Random();

                    int id = random1.nextInt(100);

                    crit = true;

                    if (id >= 50) {
                        mindmg = ThreadLocalRandom.current().nextInt(340, 400);
                        maxdmg = ThreadLocalRandom.current().nextInt(460,  530);

                        critamt = ThreadLocalRandom.current().nextInt(12, 15);
                        elem = 1;
                        elemamt = ThreadLocalRandom.current().nextInt(30, 49);

                        name = ChatColor.YELLOW + "Tortmenting Axe of Slaughter";
                        is.setType(Material.GOLD_AXE);
                        llore = String.valueOf(llore) + "The Warden's great axe.";
                        rarityId = 3;
                    } else {
                        mindmg = ThreadLocalRandom.current().nextInt(340, 400);
                        maxdmg = ThreadLocalRandom.current().nextInt(450,  490);

                        critamt = ThreadLocalRandom.current().nextInt(10, 12);
                        acc = true;
                        accamt = ThreadLocalRandom.current().nextInt(20, 35);

                        name = ChatColor.YELLOW + "Tortmenting Sword of Slaughter";
                        is.setType(Material.GOLD_SWORD);
                        llore = String.valueOf(llore) + "The Warden's great sword.";
                        rarityId = 3;
                    }
                }
                if (item == 5) {
                    stramt = ThreadLocalRandom.current().nextInt(175, 225);
                    nrg = ThreadLocalRandom.current().nextInt(6, 8);
                    hp = ThreadLocalRandom.current().nextInt(2500, 2570);
                    blockamt = ThreadLocalRandom.current().nextInt(12, 14);
                    dpsamt = ThreadLocalRandom.current().nextInt(10, 12);
                    name = ChatColor.YELLOW + "The Warden's Mask";
                    is.setType(Material.GOLD_HELMET);
                    llore = String.valueOf(llore) + "The mask of a demonic warden.";
                    rarityId = 3;
                }
                if (item == 6) {
                    stramt = ThreadLocalRandom.current().nextInt(200, 250);
                    nrg = ThreadLocalRandom.current().nextInt(7, 10);
                    hp = ThreadLocalRandom.current().nextInt(4750, 5500);
                    blockamt = ThreadLocalRandom.current().nextInt(15, 18);
                    dpsamt = ThreadLocalRandom.current().nextInt(15, 20);
                    name = ChatColor.YELLOW + "The Warden's Chestplate";
                    is.setType(Material.GOLD_CHESTPLATE);
                    llore = String.valueOf(llore) + "The breastplate worn by The Warden himself.";
                    rarityId = 3;
                }
                if (item == 7) {
                    stramt = ThreadLocalRandom.current().nextInt(200, 250);
                    nrg = ThreadLocalRandom.current().nextInt(7, 10);
                    hp = ThreadLocalRandom.current().nextInt(4500, 5350);
                    blockamt = ThreadLocalRandom.current().nextInt(15, 18);
                    dpsamt = ThreadLocalRandom.current().nextInt(15, 20);
                    name = ChatColor.YELLOW + "The Warden's Leggings";
                    is.setType(Material.GOLD_LEGGINGS);
                    llore = String.valueOf(llore) + "The leggings worn by The Warden himself.";
                    rarityId = 3;
                }
                if (item == 8) {
                    stramt = ThreadLocalRandom.current().nextInt(100, 150);
                    nrg = ThreadLocalRandom.current().nextInt(6, 8);
                    hp = ThreadLocalRandom.current().nextInt(2550, 2750);
                    blockamt = ThreadLocalRandom.current().nextInt(12, 14);
                    dpsamt = ThreadLocalRandom.current().nextInt(12, 15);
                    name = ChatColor.YELLOW + "The Warden's Boots";
                    is.setType(Material.GOLD_BOOTS);
                    llore = String.valueOf(llore) + "The boots worn by The Warden himself.";
                    rarityId = 3;
                }
                break;
            case "krampus":
                tier = 5;
                rarityId = 3;
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";

                if (item <= 4) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Krampus' Accurate Sword of Ice" : ChatColor.YELLOW + "Krampus' Accurate Sword of Ice";
                    llore = ChatColor.GRAY.toString() + "Forged out of pure gold, used by Krampus himself.";

                    is.setType(Material.GOLD_SWORD);

                    acc = true;
                    accamt = ThreadLocalRandom.current().nextInt(25, 35);

                    elem = 3;
                    elemamt = ThreadLocalRandom.current().nextInt(20, 40);

                    mindmg = ThreadLocalRandom.current().nextInt(300, 375);
                    maxdmg = ThreadLocalRandom.current().nextInt(499, 535);
                }

                if (item == 5) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Krampus' Forged Helmet" : ChatColor.YELLOW + "Krampus' Worn Helmet";
                    llore = ChatColor.GRAY.toString() + "Forged out of pure gold, worn by Krampus himself.";

                    is.setType(Material.GOLD_HELMET);

                    armdps = 2;
                    dpsamt = 9;

                    hp = ThreadLocalRandom.current().nextInt(2250, 2750);
                    nrg = ThreadLocalRandom.current().nextInt(4, 6);
                    nrghp = 2;

                    str = true;
                    stramt = ThreadLocalRandom.current().nextInt(250, 300);
                }
                if (item == 6) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Krampus' Forged Chestplate" : ChatColor.YELLOW + "Krampus' Worn Chestplate";
                    llore = ChatColor.GRAY.toString() + "Forged out of pure gold, worn by Krampus himself.";

                    is.setType(Material.GOLD_CHESTPLATE);

                    armdps = 2;
                    dpsamt = 19;

                    hp = ThreadLocalRandom.current().nextInt(4250, 5500);
                    nrg = ThreadLocalRandom.current().nextInt(6, 8);
                    nrghp = 2;

                    str = true;
                    stramt = ThreadLocalRandom.current().nextInt(250, 300);
                }
                if (item == 7) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Krampus' Forged Leggings" : ChatColor.YELLOW + "Krampus' Worn Leggings";
                    llore = ChatColor.GRAY.toString() + "Forged out of pure gold, worn by Krampus himself.";

                    is.setType(Material.GOLD_LEGGINGS);

                    armdps = 2;
                    dpsamt = 17;

                    hp = ThreadLocalRandom.current().nextInt(4250, 5500);
                    nrg = ThreadLocalRandom.current().nextInt(6, 8);
                    nrghp = 2;

                    str = true;
                    stramt = ThreadLocalRandom.current().nextInt(250, 300);
                }
                if (item == 8) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Krampus' Forged Boots" : ChatColor.YELLOW + "Krampus' Worn Boots";
                    llore = ChatColor.GRAY.toString() + "Forged out of pure gold, worn by Krampus himself.";

                    is.setType(Material.GOLD_BOOTS);

                    armdps = 2;
                    dpsamt = 9;

                    hp = ThreadLocalRandom.current().nextInt(2250, 2750);
                    nrg = ThreadLocalRandom.current().nextInt(4, 6);
                    nrghp = 2;

                    str = true;
                    stramt = ThreadLocalRandom.current().nextInt(250, 300);
                }
                break;
            case "weakSkeletonEntity":
                tier = 5;
                rarityId = 2;
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";

                if (item <= 4) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Infernal Spear of the Death" : ChatColor.YELLOW + "Skeletal Keeper's Polearm";
                    llore = ChatColor.GRAY.toString() + "Mythical weapon forged out of an unknown material.";

                    is.setType(Material.GOLD_SPADE);

                    elem = 1;
                    elemamt = ThreadLocalRandom.current().nextInt(25, 45);

                    mindmg = ThreadLocalRandom.current().nextInt(250, 280);
                    maxdmg = ThreadLocalRandom.current().nextInt(300, 450);

                    life = true;
                    lifeamt = ThreadLocalRandom.current().nextInt(5, 8);

                    pure = random.nextBoolean();
                    pureamt = ThreadLocalRandom.current().nextInt(20, 40);
                }
                if (item == 5) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Infernal Keeper's Helmet" : ChatColor.YELLOW + "Soulstealer's Helmet";
                    llore = ChatColor.GRAY.toString() + "Mythical helmet forged out of an unknown material.";

                    is.setType(Material.GOLD_HELMET);

                    hp = ThreadLocalRandom.current().nextInt(2100, 3100);
                    hps = ThreadLocalRandom.current().nextInt(210, 250);
                    nrghp = 1;

                    dodge = random.nextBoolean();
                    dodgeamt = ThreadLocalRandom.current().nextInt(10, 15);

                    str = true;
                    stramt = ThreadLocalRandom.current().nextInt(250, 300);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(10, 12);
                }
                if (item == 6) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Infernal Keeper's Chestpiece" : ChatColor.YELLOW + "Soulstealer's Chestplate";
                    llore = ChatColor.GRAY.toString() + "Mythical chestpiece forged out of an unknown material.";

                    is.setType(Material.GOLD_CHESTPLATE);

                    hp = ThreadLocalRandom.current().nextInt(4200, 6300);
                    hps = ThreadLocalRandom.current().nextInt(210, 250);
                    nrghp = 1;

                    dodge = random.nextBoolean();
                    dodgeamt = ThreadLocalRandom.current().nextInt(10, 15);

                    str = true;
                    stramt = ThreadLocalRandom.current().nextInt(250, 300);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(20, 24);
                }
                if (item == 7) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Infernal Keeper's Leggings" : ChatColor.YELLOW + "Soulstealer's Leggings";
                    llore = ChatColor.GRAY.toString() + "Mythical leggings forged out of an unknown material.";

                    is.setType(Material.GOLD_LEGGINGS);

                    hp = ThreadLocalRandom.current().nextInt(4200, 6300);
                    hps = ThreadLocalRandom.current().nextInt(210, 250);
                    nrghp = 1;

                    dodge = random.nextBoolean();
                    dodgeamt = ThreadLocalRandom.current().nextInt(10, 15);

                    str = true;
                    stramt = ThreadLocalRandom.current().nextInt(250, 300);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(20, 24);
                }
                if (item == 8) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Infernal Keeper's Boots" : ChatColor.YELLOW + "Soulstealer's Boots";
                    llore = ChatColor.GRAY.toString() + "Mythical boots forged out of an unknown material.";

                    is.setType(Material.GOLD_BOOTS);

                    hp = ThreadLocalRandom.current().nextInt(2100, 3100);
                    hps = ThreadLocalRandom.current().nextInt(210, 250);
                    nrghp = 1;

                    dodge = random.nextBoolean();
                    dodgeamt = ThreadLocalRandom.current().nextInt(10, 15);

                    str = true;
                    stramt = ThreadLocalRandom.current().nextInt(250, 300);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(10, 12);
                }
                break;
            case "bossSkeletonDungeon":

                tier = 5;
                rarityId = 3;
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";

                if (item <= 4) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Skeletal Death Bringer" : ChatColor.YELLOW + "Skeletal Soul Harvester";
                    llore = ChatColor.GRAY.toString() + "Mythical weapon forged out of an unknown material.";

                    is.setType(Material.GOLD_SWORD);

                    elem = 2;
                    elemamt = ThreadLocalRandom.current().nextInt(30, 60);

                    mindmg = ThreadLocalRandom.current().nextInt(390, 460);
                    maxdmg = ThreadLocalRandom.current().nextInt(640, 730);

                    life = true;
                    lifeamt = ThreadLocalRandom.current().nextInt(10, 12);

                    pure = random.nextBoolean();
                    pureamt = ThreadLocalRandom.current().nextInt(60, 90);
                }

                if (item == 5) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Helmet Of The Death" : ChatColor.YELLOW + "Mythical Skeletal Helmet";
                    llore = ChatColor.GRAY.toString() + "Worn by The Restless Skeleton Deathlord himself..";

                    is.setType(Material.GOLD_HELMET);

                    hp = ThreadLocalRandom.current().nextInt(2900, 3500);
                    nrg = ThreadLocalRandom.current().nextInt(5, 10);
                    nrghp = 2;

                    dodge = true;
                    dodgeamt = ThreadLocalRandom.current().nextInt(9, 15);

                    vit = random.nextBoolean();
                    vitamt = ThreadLocalRandom.current().nextInt(230, 300);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(10, 12);
                }
                if (item == 6) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Skeletal Soul Protector" : ChatColor.YELLOW + "The Skeleton Deathlord's Chestplate";
                    llore = ChatColor.GRAY.toString() + "Worn by The Restless Skeleton Deathlord himself..";

                    is.setType(Material.GOLD_CHESTPLATE);

                    hp = ThreadLocalRandom.current().nextInt(5800, 7000);
                    nrg = ThreadLocalRandom.current().nextInt(5, 10);
                    nrghp = 2;

                    dodge = random.nextBoolean();
                    dodgeamt = ThreadLocalRandom.current().nextInt(10, 15);

                    str = true;
                    stramt = ThreadLocalRandom.current().nextInt(240, 300);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(16, 24);
                }
                if (item == 7) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.RED + "Skeletal Death Leggings" : ChatColor.RED + "The Skeleton Deathlord's Leggings";
                    llore = ChatColor.GRAY.toString() + "Worn by The Restless Skeleton Deathlord himself..";

                    is.setType(Material.GOLD_LEGGINGS);

                    hp = ThreadLocalRandom.current().nextInt(5800, 7000);
                    nrg = ThreadLocalRandom.current().nextInt(5, 10);
                    nrghp = 2;

                    intel = random.nextBoolean();
                    intamt = ThreadLocalRandom.current().nextInt(240, 300);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(16, 24);
                }
                if (item == 8) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Skeletal Boots of Intellect" : ChatColor.YELLOW + "The Skeleton Deathlord's Boots";
                    llore = ChatColor.GRAY.toString() + "Worn by The Restless Skeleton Deathlord himself..";

                    is.setType(Material.GOLD_BOOTS);

                    hp = ThreadLocalRandom.current().nextInt(2900, 3500);
                    nrg = ThreadLocalRandom.current().nextInt(5, 10);
                    nrghp = 2;

                    intel = true;
                    intamt = ThreadLocalRandom.current().nextInt(250, 300);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(10, 12);
                }

                break;
        }
        /*MULTIPLIER REMOVE NORMAL WIPE*/
        if (!PracticeServer.OPEN_BETA_STATS) {
            maxdmg = (int) (maxdmg * PracticeServer.MAX_DAMAGE_MULTIPLIER);
            mindmg = (int) (mindmg * PracticeServer.MIN_DAMAGE_MULTIPLIER);
            hp = (int) (hp * PracticeServer.HP_MULTIPLIER);
            hps = (int) (hps * PracticeServer.HPS_MULTIPLIER);
        }
        /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
        if (item <= 4) {
            lore.add(ChatColor.RED + "DMG: " + mindmg + " - " + maxdmg);
            if (pure) {
                lore.add(ChatColor.RED + "PURE DMG: +" + pureamt);
            }
            if (acc) {
                lore.add(ChatColor.RED + "ACCURACY: " + accamt + "%");
            }
            if (life) {
                lore.add(ChatColor.RED + "LIFE STEAL: " + lifeamt + "%");
            }
            if (crit) {
                lore.add(ChatColor.RED + "CRITICAL HIT: " + critamt + "%");
            }
            if (elem == 3) {
                lore.add(ChatColor.RED + "ICE DMG: +" + elemamt);
            }
            if (elem == 2) {
                lore.add(ChatColor.RED + "POISON DMG: +" + elemamt);
            }
            if (elem == 1) {
                lore.add(ChatColor.RED + "FIRE DMG: +" + elemamt);
            }
        }
        if (item == 5 || item == 6 || item == 7 || item == 8) {
            if (armdps == 1) {
                lore.add(ChatColor.RED + "ARMOR: " + dpsamt + " - " + dpsamt + "%");
            }
            if (armdps == 2) {
                lore.add(ChatColor.RED + "DPS: " + dpsamt + " - " + dpsamt + "%");
            }
            lore.add(ChatColor.RED + "HP: +" + hp);
            if (nrghp == 2) {
                lore.add(ChatColor.RED + "ENERGY REGEN: +" + nrg + "%");
            }
            if (nrghp == 1) {
                lore.add(ChatColor.RED + "HP REGEN: +" + hps + "/s");
            }
            if (intel) {
                lore.add(ChatColor.RED + "INT: +" + intamt);
            }
            if (str) {
                lore.add(ChatColor.RED + "STR: +" + stramt);
            }
            if (vit) {
                lore.add(ChatColor.RED + "VIT: +" + vitamt);
            }
            if (dodge) {
                lore.add(ChatColor.RED + "DODGE: " + dodgeamt + "%");
            }
            if (block) {
                lore.add(ChatColor.RED + "BLOCK: " + blockamt + "%");
            }
        }
        lore.add(llore);
        lore.add(rarity);
        ItemMeta im = is.getItemMeta();

        // Remove native Minecraft lore
        for (ItemFlag itemFlag : ItemFlag.values()) {
            im.addItemFlags(itemFlag);
        }
        im.setDisplayName(name);
        im.setLore(lore);
        is.setItemMeta(im);

        GlowAPI.Color color = null;

        switch (rarityId) {
            case 0:
                color = GlowAPI.Color.WHITE;
                break;
            case 1:
                color = GlowAPI.Color.GREEN;
                break;
            case 2:
                color = GlowAPI.Color.AQUA;
                break;
            case 3:
                color = GlowAPI.Color.YELLOW;
                break;
        }
        if (dungeonBoss.equals("bossSkeletonDungeon"))
            is.addEnchantment(Enchants.glow, 1);

        NBTAccessor nbtAccessor = new NBTAccessor(is).check();
        nbtAccessor.setString("rarityType", color.name());

        return nbtAccessor.update();
    }

    public static ItemStack createCustomEliteDrop(String mobname) {
        String name = "";
        String llore = ChatColor.GRAY.toString();
        ItemStack is = new ItemStack(Material.AIR);
        ArrayList<String> lore = new ArrayList<String>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int item = random.nextInt(8) + 1;
        int tier = 0;
        String rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
        int rarityId = 0;
        int armdps = 0;
        int nrghp = 0;
        int elem = 0;
        boolean pure = false;
        boolean life = false;
        boolean vsplayers = false;
        boolean crit = false;
        boolean acc = false;
        boolean dodge = false;
        boolean block = false;
        boolean vit = false;
        boolean dex = false;
        boolean str = false;
        boolean intel = false;
        int hp = 0;
        int mindmg = 0;
        int maxdmg = 0;
        int dpsamt = 0;
        int dodgeamt = 0;
        int blockamt = 0;
        int vitamt = 0;
        int dexamt = 0;
        int stramt = 0;
        int intamt = 0;
        int elemamt = 0;
        int pureamt = 0;
        int lifeamt = 0;
        int critamt = 0;
        int accamt = 0;
        int vsplayersamt = 0;
        int hps = 0;
        int nrg = 0;

        //Tier 1
        if (mobname.equalsIgnoreCase("mitsuki")) {
            nrghp = 2;
            armdps = 1;
            block = true;
            str = true;
            elem = 1;
            block = true;
            life = true;
            elemamt = 5;
            lifeamt = random.nextInt(16) + 30;
            mindmg = random.nextInt(18, 22);
            maxdmg = random.nextInt(23, 30);
            if (item <= 4) {
                name = "Mitsukis Sword of Bloodthirst";
                is.setType(Material.WOOD_SWORD);
                llore = String.valueOf(llore) + "The Master of Ruins blood-stained ridged Sword.";
            }
            if (item == 5) {
                nrg = random.nextInt(2) + 2;
                blockamt = random.nextInt(2) + 2;
                dpsamt = 1;
                stramt = 10;
                hp = random.nextInt(120, 143);
                name = "Mitsukis Leather Coif";
                is.setType(Material.LEATHER_HELMET);
                llore = String.valueOf(llore) + "A ripped remains of a Leather Coif far from industry standards.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                nrg = random.nextInt(2) + 4;
                blockamt = random.nextInt(2) + 5;
                stramt = 25;
                dpsamt = random.nextInt(2) + 2;
                hp = random.nextInt(270, 300);
                name = "Mitsukis Dirty Leather Rags";
                is.setType(Material.LEATHER_CHESTPLATE);
                llore = String.valueOf(llore) + "Blood stained rags that reek of Zombie flesh";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                nrg = random.nextInt(2) + 4;
                blockamt = random.nextInt(2) + 5;
                stramt = 25;
                dpsamt = random.nextInt(2) + 2;
                hp = random.nextInt(270, 300);
                name = "Mitsukis Ripped Leather Pants";
                is.setType(Material.LEATHER_LEGGINGS);
                llore = String.valueOf(llore) + "Can be referred to as 'shorts' due to intensive ripping.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                nrg = random.nextInt(2) + 2;
                blockamt = random.nextInt(2) + 2;
                dpsamt = 1;
                stramt = 10;
                hp = random.nextInt(120, 143);
                name = "Mitsukis Leather Sandals";
                is.setType(Material.LEATHER_BOOTS);
                llore = String.valueOf(llore) + "Blood stained sandals. Not very comfortable.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            tier = 1;
        }
        //Tier2
        if (mobname.equalsIgnoreCase("copjak")) {
            nrghp = 2;
            armdps = 1;
            str = true;
            elem = 2;
            elemamt = 12;
            crit = true;
            critamt = random.nextInt(3) + 8;
            mindmg = random.nextInt(23, 26);
            maxdmg = random.nextInt(29, 38);
            if (item <= 4) {
                name = "Cop'Jaks Deadly Poleaxe";
                is.setType(Material.STONE_SPADE);
                llore = String.valueOf(llore) + "A long wicked Poleaxe of Trollish design.";
                rarityId = 2;
            }
            if (item == 5) {
                stramt = 25;
                nrg = random.nextInt(3) + 3;
                hp = random.nextInt(220, 250);
                dpsamt = random.nextInt(2) + 3;
                name = "Cop'Jaks Shaman Headgear";
                is.setType(Material.CHAINMAIL_HELMET);
                llore = String.valueOf(llore) + "A standard Shamans headgear consisting of a bears head.";
                rarityId = 2;
            }
            if (item == 6) {
                stramt = 45;
                nrg = random.nextInt(2) + 7;
                hp = random.nextInt(460, 520);
                dpsamt = random.nextInt(2) + 6;
                name = "Cop'Jaks greased Chainmail Chestpiece";
                is.setType(Material.CHAINMAIL_CHESTPLATE);
                llore = String.valueOf(llore) + "A bad fit made for the broad chests of Trolls.";
                rarityId = 2;
            }
            if (item == 7) {
                stramt = 45;
                nrg = random.nextInt(2) + 7;
                hp = random.nextInt(460, 520);
                dpsamt = random.nextInt(2) + 6;
                name = "Cop'Jaks Chainlinked Pants";
                is.setType(Material.CHAINMAIL_LEGGINGS);
                llore = String.valueOf(llore) + "Large greased and ready for action.";
                rarityId = 2;
            }
            if (item == 8) {
                stramt = 25;
                nrg = random.nextInt(3) + 3;
                hp = random.nextInt(220, 250);
                dpsamt = random.nextInt(2) + 3;
                name = "Cop'Jaks Chainmail Boots";
                is.setType(Material.CHAINMAIL_BOOTS);
                llore = String.valueOf(llore) + "Spiked Chainmail boots.";
                rarityId = 2;
            }
            tier = 2;
        }
        if (mobname.equalsIgnoreCase("risk_Elite")) {
            nrghp = 2;
            armdps = 1;
            str = true;
            elem = 1;
            life = true;
            elemamt = 7;
            crit = true;
            intel = true;
            critamt = random.nextInt(4) + 8;
            lifeamt = random.nextInt(3) + 9;
            mindmg = random.nextInt(30, 45);
            maxdmg = random.nextInt(50,65);
            if (item <= 4) {
                name = "Riskan's Deadly Battle Axe";
                is.setType(Material.STONE_AXE);
                llore = String.valueOf(llore) + "Riskan’s battle axe of fury.";
                rarityId = 2;
            }
            if (item == 5) {
                stramt = 25;
                intamt = 20;
                nrg = random.nextInt(3) + 3;
                hp = random.nextInt(210, 240);
                dpsamt = random.nextInt(2) + 3;
                name = "Riskan's Chainlinked Headgear";
                is.setType(Material.CHAINMAIL_HELMET);
                llore = String.valueOf(llore) + "the lustrous glory helmchen.";
                rarityId = 2;
            }
            if (item == 6) {
                stramt = 45;
                intamt = 40;
                nrg = random.nextInt(2) + 5;
                hp = random.nextInt(460, 520);
                dpsamt = random.nextInt(2) + 6;
                name = "Riskan's Lapus Encrusted Chest Piece";
                is.setType(Material.CHAINMAIL_CHESTPLATE);
                llore = String.valueOf(llore) + "The glimmering breastplate of the lapis lord.";
                rarityId = 2;
            }
            if (item == 7) {
                stramt = 45;
                intamt = 40;
                nrg = random.nextInt(2) + 5;
                hp = random.nextInt(460, 520);
                dpsamt = random.nextInt(2) + 6;
                name = "Riskan's Belted Trousers";
                is.setType(Material.CHAINMAIL_LEGGINGS);
                llore = String.valueOf(llore) + "the tightly fastened leggings of Riskan himself.";
                rarityId = 2;
            }
            if (item == 8) {
                stramt = 25;
                intamt = 20;
                nrg = random.nextInt(3) + 3;
                hp = random.nextInt(210, 240);
                dpsamt = random.nextInt(2) + 3;
                name = "Riskan's Bejeweled Boots";
                is.setType(Material.CHAINMAIL_BOOTS);
                llore = String.valueOf(llore) + "Riskan’s crowned boots of fire.";
                rarityId = 2;
            }
            tier = 2;
        }
        //Tier 3
        if (mobname.equalsIgnoreCase("impa")) {
            nrghp = 2;
            armdps = 1;
            str = true;
            block = true;
            elem = 2;
            crit = true;
            elemamt = 15;
            critamt = random.nextInt(2) + 8;
            mindmg = random.nextInt(46, 55);
            maxdmg = random.nextInt(60, 72);
            if (item <= 4) {
                name = "Impa's Dreaded Polearm";
                is.setType(Material.IRON_SPADE);
                llore = String.valueOf(llore) + "The spearhead of the initial attack on Avalon.";
                rarityId = 2;
            }
            if (item == 5) {
                stramt = 75;
                blockamt = 8;
                nrg = random.nextInt(3) + 3;
                hp = random.nextInt(470, 520);
                dpsamt = random.nextInt(2) + 5;
                name = "Crooked Battle Mask";
                is.setType(Material.IRON_HELMET);
                llore = String.valueOf(llore) + "A skeleton general's black mask";
                rarityId = 2;
            }
            if (item == 6) {
                stramt = 75;
                blockamt = 8;
                nrg = random.nextInt(2) + 5;
                hp = random.nextInt(1000, 1150);
                dpsamt = random.nextInt(2) + 5;
                name = "Haunting Platemail of Avalons Fright";
                is.setType(Material.IRON_CHESTPLATE);
                llore = String.valueOf(llore) + "A breastplate with the symbol of Impas army carved into it.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                stramt = 75;
                blockamt = 8;
                nrg = random.nextInt(2) + 5;
                hp = random.nextInt(1000, 1150);
                dpsamt = random.nextInt(2) + 5;
                name = "Warding Skeletal Leggings";
                is.setType(Material.IRON_LEGGINGS);
                llore = String.valueOf(llore) + "Spiked bone leggings of greater skeleton invaders.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                stramt = 75;
                blockamt = 8;
                nrg = random.nextInt(3) + 3;
                hp = random.nextInt(470, 520);
                dpsamt = random.nextInt(2) + 5;
                name = "Skeletal Death Walkers";
                is.setType(Material.IRON_BOOTS);
                llore = String.valueOf(llore) + "The boots with which Impa treaded into this land.";
                rarityId = 2;
            }
            tier = 3;
        }
        if (mobname.equalsIgnoreCase("kingofgreed")) {
            nrghp = 2;
            armdps = 1;
            str = true;
            block = true;
            elem = 1;
            life = true;
            lifeamt = 12;
            elemamt = 20;
            critamt = random.nextInt(2) + 8;
            mindmg = random.nextInt(98, 105);
            maxdmg = random.nextInt(110, 125);
            if (item <= 4) {
                name = "The Thieving Axe of the Greed King";
                is.setType(Material.IRON_AXE);
                llore = String.valueOf(llore) + "Extremely sharp with a hilt encrusted with gems.";
                rarityId = 3;
            }
            if (item == 5) {
                stramt = 75;
                blockamt = 8;
                hps = random.nextInt(11) + 30;
                hp = random.nextInt(590, 650);
                dpsamt = random.nextInt(2) + 5;
                name = "The King of Greeds Golden Helm";
                is.setType(Material.IRON_HELMET);
                llore = String.valueOf(llore) + "Iron helm plated with gold";
                rarityId = 3;
            }
            if (item == 6) {
                stramt = 75;
                blockamt = 8;
                nrg = random.nextInt(3) + 4;
                hp = random.nextInt(1200, 1350);
                dpsamt = random.nextInt(2) + 5;
                name = "The Gem Encrusted Plate of the Greed King";
                is.setType(Material.IRON_CHESTPLATE);
                llore = String.valueOf(llore) + "A broad chestplate fit with rubies and diamonds.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                rarityId = 3;
            }
            if (item == 7) {
                stramt = 75;
                blockamt = 8;
                nrg = random.nextInt(3) + 4;
                hp = random.nextInt(1200, 1350);
                dpsamt = random.nextInt(2) + 5;
                name = "The Gem Encrusted Legs of the Greed King";
                is.setType(Material.IRON_LEGGINGS);
                llore = String.valueOf(llore) + "Iron leggings fit with emeralds and amethysts.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                rarityId = 3;
            }
            if (item == 8) {
                stramt = 75;
                blockamt = 8;
                nrg = random.nextInt(3) + 3;
                hp = random.nextInt(590, 650);
                dpsamt = random.nextInt(2) + 5;
                name = "The King of Greeds Golden Boots";
                is.setType(Material.IRON_BOOTS);
                llore = String.valueOf(llore) + "Golden boots that are completely covered in mud.";
                rarityId = 3;
            }
            tier = 3;
        }
        if (mobname.equalsIgnoreCase("skeletonking")) {
            nrghp = 1;
            armdps = 1;
            vit = true;
            pure = true;
            acc = true;
            pureamt = 25;
            accamt = random.nextInt(8) + 7;
            mindmg = random.nextInt(12) + 85;
            maxdmg = random.nextInt(44) + 100;
            if (item <= 4) {
                name = "The Skeleton Kings Sword of Banishment";
                is.setType(Material.IRON_SWORD);
                llore = String.valueOf(llore) + "A powerful sword enhanced with the soul of the Skeleton King.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 5) {
                vitamt = 49;
                hps = random.nextInt(11) + 30;
                hp = random.nextInt(400, 500);
                dpsamt = random.nextInt(2) + 4;
                name = "The Skeleton Kings Soul Helmet";
                is.setType(Material.IRON_HELMET);
                llore = String.valueOf(llore) + "A shadowy transparent helmet.";
                rarityId = 2;
            }
            if (item == 6) {
                vitamt = 99;
                nrg = random.nextInt(4) + 4;
                hp = random.nextInt(950, 1050);
                dpsamt = 8;
                name = "The Skeleton Kings Soul Armour";
                is.setType(Material.IRON_CHESTPLATE);
                llore = String.valueOf(llore) + "Armor imbued with the power of the Skeleton king.";
                rarityId = 2;
            }
            if (item == 7) {
                vitamt = 99;
                nrg = random.nextInt(4) + 4;
                hp = random.nextInt(950, 1050);
                dpsamt = 8;
                name = "The Skeleton Kings Soul Leggings";
                is.setType(Material.IRON_LEGGINGS);
                llore = String.valueOf(llore) + "Resistant to the most powerful of Physical Damage.";
                rarityId = 2;
            }
            if (item == 8) {
                vitamt = 49;
                nrg = random.nextInt(4) + 4;
                hp = random.nextInt(400, 500);
                dpsamt = random.nextInt(2) + 4;
                name = "The Skeleton Kings Soul Boots";
                is.setType(Material.IRON_BOOTS);
                llore = String.valueOf(llore) + "The shining boots of a king.";
                rarityId = 2;
            }
            tier = 3;
        }
        //Tier 4
        if (mobname.equalsIgnoreCase("duranor")) {
            nrghp = 1;
            armdps = 1;
            str = true;
            block = true;
            elem = 2;
            elemamt = random.nextInt(10) + 15;
            pure = true;
            pureamt = random.nextInt(10) + 10;
            crit = true;
            critamt = random.nextInt(5) + 3;

            mindmg = random.nextInt(100, 125);
            maxdmg = random.nextInt(140, 170);
            if (item <= 4) {
                name = "Duranor's Cruel Staff";
                is.setType(Material.DIAMOND_SPADE);
                llore = String.valueOf(llore) + "The cruel Staff of an ancient warrior.";
                rarityId = 3;
            }
            if (item == 5) {
                stramt = 75;
                blockamt = 8;
                nrg = random.nextInt(3) + 3;
                hp = random.nextInt(1200, 1400);
                dpsamt = random.nextInt(5) + 9;
                name = "Duranor's Melted Mask";
                is.setType(Material.DIAMOND_HELMET);
                llore = String.valueOf(llore) + "An ancient helmet forged from the remains of kings.";
                rarityId = 3;
            }
            if (item == 6) {
                stramt = 75;
                blockamt = 8;
                hps = random.nextInt(25) + 80;
                hp = random.nextInt(2820, 3020);
                dpsamt = random.nextInt(5) + 9;
                name = "Duranor's Chestguard of Ancient Power";
                is.setType(Material.DIAMOND_CHESTPLATE);
                llore = String.valueOf(llore) + "A chestguard imbued with unsettling power.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                rarityId = 3;
            }
            if (item == 7) {
                stramt = 75;
                blockamt = 8;
                hps = random.nextInt(25) + 80;
                hp = random.nextInt(2820, 3020);
                dpsamt = random.nextInt(5) + 9;
                name = "Duranor's Gore-Soaked Platelegs";
                is.setType(Material.DIAMOND_LEGGINGS);
                llore = String.valueOf(llore) + "Platelegs soaked in the blood of the infidels.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                rarityId = 3;
            }
            if (item == 8) {
                stramt = 75;
                blockamt = 8;
                hps = random.nextInt(25) + 80;
                hp = random.nextInt(1200, 1400);
                dpsamt = random.nextInt(5) + 9;
                name = "Duranor's Warboots of Absolute Eradication";
                is.setType(Material.DIAMOND_BOOTS);
                llore = String.valueOf(llore) + "Sabatons passed down through generations of ancient warriors.";
                rarityId = 3;
            }
            tier = 4;
        }

        if (mobname.equalsIgnoreCase("spiderQueen")) {
            // HPS --- DPS
            nrghp = 1;
            armdps = 1;
            // still don't know which is deeps leaving as is :shrug:

            elem = 2;
            vsplayers = true;

            intel = true;
            dodge = true;
            acc = true;

            intamt = random.nextInt(26) + 49;
            dodgeamt = random.nextInt(3) + 5;

            elemamt = random.nextInt(11) + 35;
            vsplayersamt = random.nextInt(4) + 4;
            accamt = ThreadLocalRandom.current().nextInt(10, 16);

            mindmg = random.nextInt(16) + 80;
            maxdmg = random.nextInt(170, 190);
            if (item <= 4) {
                name = "The Spider Queen's Fang";
                is.setType(Material.IRON_SWORD);
                llore = String.valueOf(llore) + "A fang still dripping with poison.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 5) {
                dpsamt = random.nextInt(3) + 3;
                hp = random.nextInt(900, 1000);
                hps = random.nextInt(16) + 49;
                name = "The Spider Queen's Cowl";
                is.setType(Material.IRON_HELMET);
                llore = String.valueOf(llore) + "A Cowl protected by carapace";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(3) + 3;
                hp = random.nextInt(2000, 2400);
                hps = random.nextInt(16) + 49;
                name = "Spider Queen's Carapace Chestplate";
                is.setType(Material.IRON_CHESTPLATE);
                llore = String.valueOf(llore) + "A Chestplate protected by thick carapace.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(3) + 3;
                hp = random.nextInt(2000, 2400);
                nrg = random.nextInt(4) + 4;
                name = "Spider Queen's Carapce Leggings";
                is.setType(Material.IRON_LEGGINGS);
                llore = String.valueOf(llore) + "Legs protected by thick carapace.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(3) + 3;
                hp = random.nextInt(900, 1000);
                nrg = random.nextInt(4) + 4;
                name = "Spider Queens Carapace Boots";
                is.setType(Material.IRON_BOOTS);
                llore = String.valueOf(llore) + "Boots protected by thick carapace.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            tier = 3;
        }
        if (mobname.equalsIgnoreCase("bloodbutcher")) {
            nrghp = 2;
            armdps = 1;
            elem = 3;
            vit = true;
            elemamt = 30;
            crit = true;
            acc = true;
            accamt = random.nextInt(5) + 20;
            critamt = random.nextInt(4) + 10;
            life = true;
            lifeamt = random.nextInt(4) + 10;
            mindmg = random.nextInt(180, 195);
            maxdmg = random.nextInt(210, 230);
            if (item <= 4) {
                name = "The Butchers Bloody Cleaver";
                is.setType(Material.DIAMOND_SWORD);
                llore = String.valueOf(llore) + "This cleaver was used for much more than cutting beef.";
                rarityId = 2;
            }
            if (item == 5) {
                dpsamt = random.nextInt(3) + 5;
                vitamt = 80;
                hp = random.nextInt(1000, 1100);
                hps = random.nextInt(16) + 200;
                name = "The Butchers Bloody Helm";
                is.setType(Material.DIAMOND_HELMET);
                llore = String.valueOf(llore) + "The bloody dented headpiece of the butcher.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(2) + 6;
                vitamt = 150;
                hp = random.nextInt(2100, 2200);
                nrg = random.nextInt(4) + 4;
                name = "The Butchers Tattered Apron";
                is.setType(Material.DIAMOND_CHESTPLATE);
                llore = String.valueOf(llore) + "An apron covered in bits of gut and flesh.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(2) + 6;
                vitamt = 150;
                hp = random.nextInt(2100, 2200);
                nrg = random.nextInt(3) + 4;
                name = "The Butchers Bloody Chaps";
                is.setType(Material.DIAMOND_LEGGINGS);
                llore = String.valueOf(llore) + "Covered in streaks of blood from wiping his blade.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(3) + 5;
                vitamt = 80;
                hp = random.nextInt(1000, 1100);
                nrg = random.nextInt(3) + 4;
                name = "The Butchers Blood Soaked Boots";
                is.setType(Material.DIAMOND_BOOTS);
                llore = String.valueOf(llore) + "Boots soaked in the blood of his enemies.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            tier = 4;

        }
        if (mobname.equalsIgnoreCase("blayshan")) {
            nrghp = 2;
            armdps = 2;
            elem = 3;
            str = true;
            elemamt = random.nextInt(20) + 45;
            crit = true;
            critamt = random.nextInt(3) + 8;
            mindmg = random.nextInt(200, 215);
            maxdmg = random.nextInt(230, 260);
            if (item <= 4) {
                name = "Blayshans Wicked Axe";
                is.setType(Material.DIAMOND_AXE);
                llore = String.valueOf(llore) + "An Axe with the face of the cursed Blayshan carved into it.";
                rarityId = 3;
            }
            if (item == 5) {
                dpsamt = random.nextInt(3) + 4;
                stramt = 145;
                hp = random.nextInt(1000, 1200);
                nrg = random.nextInt(2) + 4;
                name = "Blayshans Accursed Helmet";
                is.setType(Material.DIAMOND_HELMET);
                llore = String.valueOf(llore) + "A weirdly shaped aqua blue helmet.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(3) + 5;
                stramt = 245;
                hp = random.nextInt(2000, 2150);
                nrg = random.nextInt(3) + 8;
                name = "Blayshans Wicked Horned Platemail";
                is.setType(Material.DIAMOND_CHESTPLATE);
                llore = String.valueOf(llore) + "Not well made but light with studded mail fists.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(3) + 5;
                stramt = 245;
                hp = random.nextInt(2000, 2150);
                nrg = random.nextInt(3) + 8;
                name = "Blayshans Wicked Horned Leggings";
                is.setType(Material.DIAMOND_LEGGINGS);
                llore = String.valueOf(llore) + "Glistening with the blood of fallen enemies.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(3) + 4;
                stramt = 145;
                hp = random.nextInt(1000, 1200);
                nrg = random.nextInt(2) + 4;
                name = "Blayshans Platemail Boots";
                is.setType(Material.DIAMOND_BOOTS);
                llore = String.valueOf(llore) + "A pair of boots shaped to fit a Naga.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            tier = 4;
        }
        if (mobname.equalsIgnoreCase("spectralKnight")) {
            nrghp = 2;
            armdps = 1;
            crit = true;
            pure = true;
            vit = true;
            vitamt = random.nextInt(54) + 100;
            critamt = random.nextInt(5) + 10;
            pureamt = random.nextInt(15) + 45;
            mindmg = random.nextInt(30) + 120;
            maxdmg = random.nextInt(40) + 200;
            if (item <= 4) {
                name = "Spectral Rapier";
                is.setType(Material.DIAMOND_SWORD);
                llore = String.valueOf(llore) + "A Rapier that can pierce through the thickest of armor with ease.";
                rarityId = 3;
            }
            if (item == 5) {
                dpsamt = random.nextInt(6) + 5;
                hp = random.nextInt(950, 1150);
                nrg = random.nextInt(3) + 3;
                name = "Spectral Helmet";
                is.setType(Material.DIAMOND_HELMET);
                llore = String.valueOf(llore) + "A Helmet that can fade in and out of reality in the blink of an eye.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(6) + 10;
                hp = random.nextInt(2000, 2120);
                nrg = random.nextInt(3) + 4;
                name = "Spectral Platemail";
                is.setType(Material.DIAMOND_CHESTPLATE);
                llore = String.valueOf(llore) + "A large Platemail that can fade in and out of reality in the blink of an eye.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(6) + 10;
                hp = random.nextInt(2000, 2120);
                nrg = random.nextInt(3) + 4;
                name = "Spectral Leggings";
                is.setType(Material.DIAMOND_LEGGINGS);
                llore = String.valueOf(llore) + "A Pair of Pants that fade in and out of reality in the blink of an eye.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(6) + 5;
                hp = random.nextInt(950, 1150);
                nrg = random.nextInt(3) + 3;
                name = "Spectral Boots";
                is.setType(Material.DIAMOND_BOOTS);
                llore = String.valueOf(llore) + "A Pair of Boots that fade in and out of reality in the blink of an eye.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            tier = 4;
        }
        if (mobname.equalsIgnoreCase("watchMaster")) {
            nrghp = 2;
            armdps = 2;
            crit = true;
            elem = 3;

            dex = true;
            block = true;

            dexamt = random.nextInt(25) + 75;
            blockamt = random.nextInt(3) + 9;

            critamt = random.nextInt(3) + 7;
            elemamt = random.nextInt(20) + 30;

            mindmg = random.nextInt(115, 125);
            maxdmg = random.nextInt(145, 179);

            if (item <= 4) {
                name = "Watchmaster's Poleaxe";
                is.setType(Material.DIAMOND_SPADE);
                llore = String.valueOf(llore) + "The Watchmaster's massive poleaxe.";
                rarityId = 2;
            }
            if (item == 5) {
                dpsamt = random.nextInt(3) + 9;
                hp = random.nextInt(1100, 1200);
                nrg = random.nextInt(3) + 3;
                name = "Watchmaster's Greathelm";
                is.setType(Material.DIAMOND_HELMET);
                llore = String.valueOf(llore) + "The Watchmaster's large helmet.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(3) + 12;
                hp = random.nextInt(2100, 2400);
                nrg = random.nextInt(3) + 4;
                name = "Watchmaster's Pride";
                is.setType(Material.DIAMOND_CHESTPLATE);
                llore = String.valueOf(llore) + "The Watchmaster's decorated chestplate.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(3) + 12;
                hp = random.nextInt(2100, 2400);
                nrg = random.nextInt(3) + 4;
                name = "Watchmaster's Platelegs";
                is.setType(Material.DIAMOND_LEGGINGS);
                llore = String.valueOf(llore) + "The Watchmaster's platemail leggings.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(3) + 9;
                hp = random.nextInt(1100, 1200);
                nrg = random.nextInt(3) + 3;
                name = "Watchmaster's Boots";
                is.setType(Material.DIAMOND_BOOTS);
                llore = String.valueOf(llore) + "The Watchmaster's large boots.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            tier = 4;
        }
        if (mobname.equalsIgnoreCase("jayden")) {
            nrghp = 2;
            armdps = 2;
            elem = 3;
            vit = true;
            dodge = true;
            elemamt = random.nextInt(10) + 60;
            crit = true;
            critamt = random.nextInt(8, 12);
            mindmg = random.nextInt(450, 480);
            maxdmg = random.nextInt(500, 570);
            if (item <= 4) {
                name = "Jayden's Swift War Axe";
                is.setType(Material.GOLD_AXE);
                llore = String.valueOf(llore) + "King Jayden's sturdy axe encrusted with diamonds.";
                rarityId = 2;
            }
            if (item == 5) {
                dpsamt = random.nextInt(2) + 6;
                vitamt = 145;
                hp = random.nextInt(2200, 2400);
                nrg = random.nextInt(4) + 3;
                dodgeamt = random.nextInt(3) + 5;
                name = "Jayden's Beautiful Crown";
                is.setType(Material.GOLD_HELMET);
                llore = String.valueOf(llore) + "A golden crown forged by Maltai's great smith.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(4) + 12;
                vitamt = 345;
                hp = random.nextInt(4800, 5100);
                nrg = random.nextInt(3) + 5;
                dodgeamt = random.nextInt(11) + 10;
                name = "Jayden's Stolen Chest Guard";
                is.setType(Material.GOLD_CHESTPLATE);
                llore = String.valueOf(llore) + "The protection from the last blow of the fallen knight.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(4) + 12;
                vitamt = 345;
                hp = random.nextInt(4800, 5100);
                nrg = random.nextInt(3) + 5;
                dodgeamt = random.nextInt(11) + 10;
                name = "Jayden's Godlike Leggings";
                is.setType(Material.GOLD_LEGGINGS);
                llore = String.valueOf(llore) + "The legendary leggings of a fallen lord.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(2) + 6;
                vitamt = 145;
                hp = random.nextInt(2200, 2400);
                nrg = random.nextInt(4) + 3;
                dodgeamt = random.nextInt(3) + 5;
                name = "Jayden's Legendary Footwear";
                is.setType(Material.GOLD_BOOTS);
                llore = String.valueOf(llore) + "Sturdy golden footwear owner by the king himself.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            tier = 5;
        }
        if (mobname.equalsIgnoreCase("kilatan")) {
            nrghp = 2;
            armdps = 2;
            elem = 1;
            intel = true;
            dodge = true;
            elemamt = 30;
            mindmg = random.nextInt(260, 290);
            maxdmg = random.nextInt(300, 360);
            if (item <= 4) {
                name = "Kilatans Staff of Destruction";
                is.setType(Material.GOLD_HOE);
                llore = String.valueOf(llore) + "A powerful staff imbued with the magics of Kilatan";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                rarityId = 3;
            }
            if (item == 5) {
                dpsamt = random.nextInt(2) + 6;
                intamt = 145;
                hp = random.nextInt(2200, 2400);
                nrg = random.nextInt(5) + 4;
                dodgeamt = random.nextInt(3) + 5;
                name = "Kilatans Crown of Death";
                is.setType(Material.GOLD_HELMET);
                llore = String.valueOf(llore) + "A golden crown of tyranny and power.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(4) + 12;
                intamt = 345;
                hp = random.nextInt(4900, 5100);
                nrg = random.nextInt(5) + 6;
                dodgeamt = random.nextInt(11) + 10;
                name = "Kilatans Legendary Platemail";
                is.setType(Material.GOLD_CHESTPLATE);
                llore = String.valueOf(llore) + "The Legendary platemail piece of the Demon Lord Kilatan.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(4) + 12;
                intamt = 345;
                hp = random.nextInt(4900, 5100);
                nrg = random.nextInt(5) + 6;
                dodgeamt = random.nextInt(11) + 10;
                name = "Kilatans Legendary Leggings";
                is.setType(Material.GOLD_LEGGINGS);
                llore = String.valueOf(llore) + "You can feel the power emanating from this armor piece.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(2) + 6;
                intamt = 145;
                hp = random.nextInt(2200, 2400);
                nrg = random.nextInt(5) + 4;
                dodgeamt = random.nextInt(3) + 5;
                name = "Kilatans Legendary Boots";
                is.setType(Material.GOLD_BOOTS);
                llore = String.valueOf(llore) + "Boots that carried the weight of the underworld.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            tier = 5;
        }
        if (mobname.equalsIgnoreCase("frostKing")) {

            nrghp = 2;
            armdps = 1;
            crit = true;
            elem = 3;

            vit = true;
            dodge = true;

            vitamt = random.nextInt(50) + 275;
            dodgeamt = random.nextInt(3) + 12;

            pureamt = random.nextInt(35) + 30;
            critamt = random.nextInt(5) + 7;
            elemamt = random.nextInt(35) + 30;

            mindmg = random.nextInt(460, 470);
            maxdmg = random.nextInt(500, 540);
            if (item <= 4) {
                name = ChatColor.YELLOW + "Axe of the Frost Dominion";
                is.setType(Material.GOLD_AXE);
                llore = String.valueOf(llore) + "The axe wielded by only the strongest of champions.";
                rarityId = 3;
            }
            if (item == 5) {
                dpsamt = random.nextInt(3) + 12;
                hp = random.nextInt(2500, 3000);
                nrg = random.nextInt(2) + 4;
                name = ChatColor.YELLOW + "The King's Crown";
                is.setType(Material.GOLD_HELMET);
                llore = String.valueOf(llore) + "The king's icy crown.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                rarityId = 3;
            }
            if (item == 6) {
                dpsamt = random.nextInt(3) + 12;
                hp = random.nextInt(5600, 6000);
                nrg = random.nextInt(2) + 5;
                name = ChatColor.YELLOW + "The King's Chestplate of Frost";
                is.setType(Material.GOLD_CHESTPLATE);
                llore = String.valueOf(llore) + "The king's legendary chestplate.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                rarityId = 3;
            }
            if (item == 7) {
                dpsamt = random.nextInt(3) + 12;
                hp = random.nextInt(5600, 6000);
                nrg = random.nextInt(2) + 5;
                name = ChatColor.YELLOW + "The King's Platelegs of Frost";
                is.setType(Material.GOLD_LEGGINGS);
                llore = String.valueOf(llore) + "The king's armored platelegs.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                rarityId = 3;
            }
            if (item == 8) {
                dpsamt = random.nextInt(3) + 9;
                hp = random.nextInt(2500, 3000);
                nrg = random.nextInt(2) + 4;
                name = ChatColor.YELLOW + "The King's Boots of Frost";
                is.setType(Material.GOLD_BOOTS);
                llore = String.valueOf(llore) + "The king's large boots.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            tier = 5;
        }
        if (mobname.equalsIgnoreCase("frozenGolem")) {
            nrghp = 2;
            armdps = 2;
            block = true;
            elem = 3;
            str = true;
            vit = true;
            intel = true;
            life = true;
            crit = true;
            acc = true;
            if(PracticeServer.t6){
                vitamt = random.nextInt(100) + 300;
                intamt = random.nextInt(100) + 300;
                stramt = random.nextInt(75) + 250;
                blockamt = random.nextInt(4) + 10;
                critamt = random.nextInt(6) + 8;
                elemamt = random.nextInt(40) + 60;
                lifeamt = random.nextInt(2) + 4;
                accamt = random.nextInt(11) + 25;
                mindmg = random.nextInt(750, 789);
                maxdmg = random.nextInt(820, 950);
            }else {
                vitamt = random.nextInt(100) + 200;
                intamt = random.nextInt(100) + 200;
                stramt = random.nextInt(75) + 150;
                blockamt = random.nextInt(4) + 7;
                critamt = random.nextInt(5) + 5;
                elemamt = random.nextInt(21) + 40;
                lifeamt = random.nextInt(3) + 3;
                accamt = random.nextInt(11) + 25;
                mindmg = random.nextInt(101) + 400;
                maxdmg = random.nextInt(101) + 525;
            }
            if (item <= 4) {
                name = "Frostfalls Demise";
                if(PracticeServer.t6){
                    is.setType(Material.DIAMOND_SWORD);
                }else {
                    is.setType(Material.GOLD_SWORD);
                }
                llore = String.valueOf(llore) + "A Legendary Sword forged in the deepest depths of the Frozen Heart.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                rarityId = 3;
            } else if (item == 5) {
                name = "Frost-Forged Crown";
                llore = String.valueOf(llore) + "A Legendary Helmet made of the purest of ice from the North.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                if(PracticeServer.t6){
                    dpsamt = random.nextInt(4) + 12;
                    hp = random.nextInt(6000, 6200);
                    nrg = random.nextInt(3) + 6;
                    is.setType(Material.LEATHER_HELMET);
                }else{
                    dpsamt = random.nextInt(3) + 11;
                    hp = random.nextInt(751) + 3000;
                    nrg = random.nextInt(2) + 5;
                    is.setType(Material.GOLD_HELMET);
                }
                rarityId = 3;
            } else if (item == 6) {
                name = "Treasure of the Crypt";
                llore = String.valueOf(llore) + "A heavily armored chestpiece adorned with beautiful blue jewels.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                if(PracticeServer.t6){
                    dpsamt = random.nextInt(5) + 18;
                    hp = random.nextInt(12500, 13500);
                    nrg = random.nextInt(4) + 6;
                    is.setType(Material.LEATHER_CHESTPLATE);
                }else{
                    dpsamt = random.nextInt(4) + 18;
                    hp = random.nextInt(1501) + 6000;
                    nrg = random.nextInt(2) + 5;
                    is.setType(Material.GOLD_CHESTPLATE);
                }
                rarityId = 3;
            } else if (item == 7) {
                name = "Unbroken Greaves";
                llore = String.valueOf(llore) + "Greaves made from the strongest ice imaginable.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                if(PracticeServer.t6){
                    dpsamt = random.nextInt(5) + 18;
                    hp = random.nextInt(12500, 13500);
                    nrg = random.nextInt(4) + 6;
                    is.setType(Material.LEATHER_LEGGINGS);
                }else{
                    dpsamt = random.nextInt(4) + 18;
                    hp = random.nextInt(1501) + 6000;
                    nrg = random.nextInt(2) + 5;
                    is.setType(Material.GOLD_LEGGINGS);
                }
                rarityId = 3;
            } else if (item == 8) {
                name = "Celeritas, Perpetuous Frost";
                llore = String.valueOf(llore) + "Icy cold boots leaving a trail of frost with every step.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                if(PracticeServer.t6){
                    dpsamt = random.nextInt(4) + 12;
                    hp = random.nextInt(6000, 6200);
                    nrg = random.nextInt(3) + 6;
                    is.setType(Material.LEATHER_BOOTS);
                }else{
                    dpsamt = random.nextInt(3) + 11;
                    hp = random.nextInt(751) + 3000;
                    nrg = random.nextInt(2) + 5;
                    is.setType(Material.GOLD_BOOTS);
                }
                rarityId = 3;
            }
            if (item > 4 && PracticeServer.t6) Items.setItemBlueLeather(is);
            tier = PracticeServer.t6 ? 6 : 5;
        }
        if (mobname.equalsIgnoreCase("frozenElite")) {
            nrghp = 1;
            armdps = 1;
            block = true;
            elem = 3;
            str = true;
            intel = true;
            life = true;
            pure = true;
            if(PracticeServer.t6){
                intamt = random.nextInt(101) + 500;
                stramt = random.nextInt(101) + 400;
                blockamt = random.nextInt(4) + 10;
                pureamt = random.nextInt(21) + 30;
                elemamt = random.nextInt(41) + 90;
                lifeamt = random.nextInt(5) + 3;
                mindmg = random.nextInt(670, 700);
                maxdmg = random.nextInt(750, 820);
            }else{
                intamt = random.nextInt(101) + 300;
                stramt = random.nextInt(101) + 200;
                blockamt = random.nextInt(4) + 10;
                pureamt = random.nextInt(21) + 20;
                elemamt = random.nextInt(31) + 30;
                lifeamt = random.nextInt(5) + 3;
                mindmg = random.nextInt(100) + 200;
                maxdmg = random.nextInt(101) + 275;
            }
            if (item <= 4) {
                name = "Frozen Hammer Of The Exiled King";
                if(PracticeServer.t6){
                    is.setType(Material.DIAMOND_SPADE);
                }else{
                    is.setType(Material.GOLD_SPADE);
                }
                llore = String.valueOf(llore) + "A Frozen Hammer Forged In The Ice Lakes Of The Crystal Summit.";
                rarityId = 3;
            } else if (item == 5) {
                name = "Frozen Crown of The Exiled King";
                llore = String.valueOf(llore) + "A blood stained crown made of crystalline ice.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                if(PracticeServer.t6){
                    dpsamt = random.nextInt(3) + 12;
                    hp = random.nextInt(4200, 5000);
                    hps = random.nextInt(200) + 250;
                    is.setType(Material.LEATHER_HELMET);
                }else{
                    dpsamt = random.nextInt(3) + 12;
                    hp = random.nextInt(751) + 2250;
                    hps = random.nextInt(100) + 150;
                    is.setType(Material.GOLD_HELMET);
                }
                rarityId = 2;
            } else if (item == 6) {
                name = "Frozen Platemail of The Exiled King";
                llore = String.valueOf(llore) + "A large platemail made of pure crystalline ice.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                if(PracticeServer.t6){
                    dpsamt = random.nextInt(5) + 18;
                    hp = random.nextInt(9000, 10000);
                    hps = random.nextInt(200) + 250;
                    is.setType(Material.LEATHER_CHESTPLATE);
                }else{
                    dpsamt = random.nextInt(5) + 20;
                    hp = random.nextInt(1501) + 4500;
                    hps = random.nextInt(100) + 150;
                    is.setType(Material.GOLD_CHESTPLATE);
                }
                rarityId = 2;
            } else if (item == 7) {
                name = "Frozen Greaves Of The Exiled King";
                llore = String.valueOf(llore) + "Shattered greaves made of pure ice.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                if(PracticeServer.t6){
                    dpsamt = random.nextInt(5) + 18;
                    hp = random.nextInt(9000, 10000);
                    hps = random.nextInt(200) + 250;
                    is.setType(Material.LEATHER_LEGGINGS);
                }else{
                    dpsamt = random.nextInt(5) + 20;
                    hp = random.nextInt(1501) + 4500;
                    hps = random.nextInt(100) + 150;
                    is.setType(Material.GOLD_LEGGINGS);
                }
                rarityId = 2;
            } else if (item == 8) {
                name = "Frozen Snow Boots Of The Exiled King";
                llore = String.valueOf(llore) + "King Frost's presh frozen Yeezys.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                if(PracticeServer.t6){
                    dpsamt = random.nextInt(3) + 12;
                    hp = random.nextInt(4200, 5000);
                    hps = random.nextInt(200) + 250;
                    is.setType(Material.LEATHER_BOOTS);
                }else{
                    dpsamt = random.nextInt(3) + 12;
                    hp = random.nextInt(751) + 2250;
                    hps = random.nextInt(100) + 150;
                    is.setType(Material.GOLD_BOOTS);
                }
                rarityId = 2;
            }
            if (item > 4 && PracticeServer.t6) Items.setItemBlueLeather(is);
            tier = PracticeServer.t6 ? 6 : 5;
        }
        if (mobname.equalsIgnoreCase("frozenBoss")) {
            nrghp = 2;
            armdps = 1;
            crit = true;
            block = true;
            elem = 3;
            str = true;
            intel = true;
            if(PracticeServer.t6){
                blockamt = random.nextInt(6) + 6;
                intamt = random.nextInt(100) + 350;
                stramt = random.nextInt(100) + 350;
                critamt = random.nextInt(6) + 9;
                elemamt = random.nextInt(20) + 70;
                mindmg = random.nextInt(600, 650);
                maxdmg = random.nextInt(750, 850);
            }else {
                blockamt = random.nextInt(6) + 6;
                intamt = random.nextInt(100) + 250;
                stramt = random.nextInt(100) + 250;
                critamt = random.nextInt(6) + 9;
                elemamt = random.nextInt(20) + 60;
                mindmg = random.nextInt(101) + 400;
                maxdmg = random.nextInt(101) + 525;
            }
            if (item <= 4) {
                name = "The Conquerer's Frozen Greataxe";
                if(PracticeServer.t6){
                    is.setType(Material.DIAMOND_AXE);
                }else{
                    is.setType(Material.GOLD_AXE);
                }
                llore = String.valueOf(llore) + "A large blade made of solid ice.";
                rarityId = 3;
            } else if (item == 5) {
                llore = String.valueOf(llore) + "A frozen crown forged by The Conquerer himself.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                name = "The Conquerer's Icy Crown";
                if(PracticeServer.t6){
                    dpsamt = random.nextInt(4) + 15;
                    hp = random.nextInt(5000, 5900);
                    nrg = random.nextInt(3) + 6;
                    is.setType(Material.LEATHER_HELMET);
                }else{
                    dpsamt = random.nextInt(3) + 11;
                    hp = random.nextInt(751) + 2500;
                    nrg = random.nextInt(2) + 4;
                    is.setType(Material.GOLD_HELMET);
                }
                rarityId = 3;
            } else if (item == 6) {
                llore = String.valueOf(llore) + "A platemail made of solid ice.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                name = "Breastplate of The Conquerer";
                if(PracticeServer.t6){
                    dpsamt = random.nextInt(5) + 12;
                    hp = random.nextInt(10000, 11500);
                    nrg = random.nextInt(4) + 6;
                    is.setType(Material.LEATHER_CHESTPLATE);
                }else{
                    dpsamt = random.nextInt(3) + 18;
                    hp = random.nextInt(1501) + 5000;
                    nrg = random.nextInt(2) + 5;
                    is.setType(Material.GOLD_CHESTPLATE);
                }
                rarityId = 3;
            } else if (item == 7) {
                llore = String.valueOf(llore) + "A pair of leggings carved from ice.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                name = "Frosted Platelegs of The Conquerer";
                if(PracticeServer.t6){
                    dpsamt = random.nextInt(5) + 17;
                    hp = random.nextInt(10000, 11500);
                    nrg = random.nextInt(4) + 6;
                    is.setType(Material.LEATHER_LEGGINGS);
                }else{
                    dpsamt = random.nextInt(3) + 18;
                    hp = random.nextInt(1501) + 5000;
                    nrg = random.nextInt(2) + 5;
                    is.setType(Material.GOLD_LEGGINGS);
                }
                rarityId = 3;
            } else if (item == 8) {
                llore = String.valueOf(llore) + "A pair of spiked boots worn by The Conquerer.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique";
                name = "Spiked Boots of The Conquerer";
                if(PracticeServer.t6){
                    dpsamt = random.nextInt(4) + 12;
                    hp = random.nextInt(5000, 5900);
                    nrg = random.nextInt(3) + 6;
                    is.setType(Material.LEATHER_BOOTS);
                }else{
                    dpsamt = random.nextInt(3) + 11;
                    hp = random.nextInt(751) + 2500;
                    nrg = random.nextInt(2) + 4;
                    is.setType(Material.GOLD_BOOTS);
                }
                rarityId = 3;
            }
            if (item > 4 && PracticeServer.t6) Items.setItemBlueLeather(is);
            tier = PracticeServer.t6 ? 6 : 5;

        }

        /*MULTIPLIER REMOVE NORMAL WIPE*/
        if (!PracticeServer.OPEN_BETA_STATS) {
            maxdmg = (int) (maxdmg * PracticeServer.MAX_DAMAGE_MULTIPLIER);
            mindmg = (int) (mindmg * PracticeServer.MIN_DAMAGE_MULTIPLIER);
            hp = (int) (hp * PracticeServer.HP_MULTIPLIER);
            hps = (int) (hps * PracticeServer.HPS_MULTIPLIER);
        }
        /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
        if (item <= 4) {
            lore.add(ChatColor.RED + "DMG: " + mindmg + " - " + maxdmg);
            if (pure) {
                lore.add(ChatColor.RED + "PURE DMG: +" + pureamt);
            }
            if (acc) {
                lore.add(ChatColor.RED + "ACCURACY: " + accamt + "%");
            }
            if (life) {
                lore.add(ChatColor.RED + "LIFE STEAL: " + lifeamt + "%");
            }
            if (vsplayers) {
                lore.add(ChatColor.RED + "VS PLAYERS: " + vsplayersamt + "%");
            }
            if (crit) {
                lore.add(ChatColor.RED + "CRITICAL HIT: " + critamt + "%");
            }
            if (elem == 3) {
                lore.add(ChatColor.RED + "ICE DMG: +" + elemamt);
            }
            if (elem == 2) {
                lore.add(ChatColor.RED + "POISON DMG: +" + elemamt);
            }
            if (elem == 1) {
                lore.add(ChatColor.RED + "FIRE DMG: +" + elemamt);
            }
        }
        if (item == 5 || item == 6 || item == 7 || item == 8) {
            if (armdps == 1) {
                lore.add(ChatColor.RED + "ARMOR: " + dpsamt + " - " + dpsamt + "%");
            }
            if (armdps == 2) {
                lore.add(ChatColor.RED + "DPS: " + dpsamt + " - " + dpsamt + "%");
            }
            lore.add(ChatColor.RED + "HP: +" + hp);
            if (nrg > 0) {
                lore.add(ChatColor.RED + "ENERGY REGEN: +" + nrg + "%");
            }
            if (hps > 0) {
                lore.add(ChatColor.RED + "HP REGEN: +" + hps + "/s");
            }
            if (intel) {
                lore.add(ChatColor.RED + "INT: +" + intamt);
            }
            if (str) {
                lore.add(ChatColor.RED + "STR: +" + stramt);
            }
            if (vit) {
                lore.add(ChatColor.RED + "VIT: +" + vitamt);
            }
            if (dex) {
                lore.add(ChatColor.RED + "DEX: +" + dexamt);
            }
            if (dodge) {
                lore.add(ChatColor.RED + "DODGE: " + dodgeamt + "%");
            }
            if (block) {
                lore.add(ChatColor.RED + "BLOCK: " + blockamt + "%");
            }
        }
        lore.add(llore);
        lore.add(rarity);
        if (tier == 1) {
            name = ChatColor.WHITE + name;
        }
        if (tier == 2) {
            name = ChatColor.GREEN + name;
        }
        if (tier == 3) {
            name = ChatColor.AQUA + name;
        }
        if (tier == 4) {
            name = ChatColor.LIGHT_PURPLE + name;
        }
        if (tier == 5) {
            name = ChatColor.YELLOW + name;
        }
        if (tier == 6) {
            name = ChatColor.BLUE + name;
        }
        try {
            if(is.getItemMeta() != null) {
                ItemMeta im = is.getItemMeta();

                // Remove native Minecraft lore
                for (ItemFlag itemFlag : ItemFlag.values()) {
                    im.addItemFlags(itemFlag);
                }
                im.setDisplayName(name);
                im.setLore(lore);
                is.setItemMeta(im);
            }

            GlowAPI.Color color = Items.getColorFromTier(rarityId);

            NBTAccessor nbtAccessor = new NBTAccessor(is).check();
            if(nbtAccessor == null) return is;
            if (mobname.equalsIgnoreCase("spectralKnight")) {
                nbtAccessor.setInt("fixedgear", 1);
            }
            nbtAccessor.setString("rarityType", color.name());
            ItemStack update = nbtAccessor.update();
            if (PracticeServer.GLOWING_NAMED_ELITE_DROP) {
                update.addUnsafeEnchantment(Enchants.glow, 1);
            }
            if (PracticeServer.RANDOM_DURA_NAMED_ELITE_DROP) {
                update.setDurability((short) random(new Random(), 0, update.getType().getMaxDurability()));
            }
            return update;
        }catch(NullPointerException e) {
            return is;
        }
    }

    public static int random(Random random, int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

}