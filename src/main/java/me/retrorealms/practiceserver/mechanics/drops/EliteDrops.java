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
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                if (item <= 4) {

                    Random random1 = new Random();

                    int id = random1.nextInt(100);

                    crit = true;


                    mindmg = ThreadLocalRandom.current().nextInt(260, 285);
                    maxdmg = ThreadLocalRandom.current().nextInt(335, 365);

                    critamt = ThreadLocalRandom.current().nextInt(5, 7);
                    acc = true;
                    accamt = ThreadLocalRandom.current().nextInt(20, 25);
                    life = true;
                    lifeamt = ThreadLocalRandom.current().nextInt(4, 6);

                    name = ChatColor.YELLOW + "Tortmenting Sword of Slaughter";
                    is.setType(Material.GOLD_SWORD);
                    llore = llore + "The Warden's great sword.";
                    rarityId = 2;
                    if (item == 5) {

                        nrg = ThreadLocalRandom.current().nextInt(6, 6);
                        hp = ThreadLocalRandom.current().nextInt(2000, 2350);
                        blockamt = ThreadLocalRandom.current().nextInt(3, 6);
                        dpsamt = ThreadLocalRandom.current().nextInt(10, 12);
                        dex = true;
                        dexamt = ThreadLocalRandom.current().nextInt(260, 300);
                        dodge = true;
                        dodgeamt = ThreadLocalRandom.current().nextInt(3, 6);

                        name = ChatColor.YELLOW + "The Warden's Mask";
                        is.setType(Material.GOLD_HELMET);
                        llore = llore + "The mask of a demonic warden.";
                        rarityId = 2;
                    }
                    if (item == 6) {
                        dexamt = ThreadLocalRandom.current().nextInt(260, 300);
                        nrg = ThreadLocalRandom.current().nextInt(8, 8);
                        hp = ThreadLocalRandom.current().nextInt(3900, 4700);
                        blockamt = ThreadLocalRandom.current().nextInt(3, 6);
                        dpsamt = ThreadLocalRandom.current().nextInt(15, 20);
                        dex = true;

                        dodge = true;
                        dodgeamt = ThreadLocalRandom.current().nextInt(3, 6);
                        name = ChatColor.YELLOW + "The Warden's Chestplate";
                        is.setType(Material.GOLD_CHESTPLATE);
                        llore = llore + "The breastplate worn by The Warden himself.";
                        rarityId = 2;
                    }
                    if (item == 7) {
                        dexamt = ThreadLocalRandom.current().nextInt(260, 300);
                        nrg = ThreadLocalRandom.current().nextInt(8, 8);
                        hp = ThreadLocalRandom.current().nextInt(3900, 4700);
                        blockamt = ThreadLocalRandom.current().nextInt(3, 6);
                        dpsamt = ThreadLocalRandom.current().nextInt(15, 20);
                        dex = true;

                        dodge = true;
                        dodgeamt = ThreadLocalRandom.current().nextInt(3, 6);
                        name = ChatColor.YELLOW + "The Warden's Leggings";
                        is.setType(Material.GOLD_LEGGINGS);
                        llore = llore + "The leggings worn by The Warden himself.";
                        rarityId = 2;
                    }
                    if (item == 8) {
                        nrg = ThreadLocalRandom.current().nextInt(6, 6);
                        hp = ThreadLocalRandom.current().nextInt(2000, 2350);
                        blockamt = ThreadLocalRandom.current().nextInt(3, 6);
                        dpsamt = ThreadLocalRandom.current().nextInt(10, 12);
                        dex = true;
                        dexamt = ThreadLocalRandom.current().nextInt(260, 300);
                        dodge = true;
                        dodgeamt = ThreadLocalRandom.current().nextInt(3, 6);

                        name = ChatColor.YELLOW + "The Warden's Boots";
                        is.setType(Material.GOLD_BOOTS);
                        llore = llore + "The boots worn by The Warden himself.";
                        rarityId = 2;
                    }
                }
                break;
            case "krampus":
                tier = 5;
                rarityId = 3;
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";

                if (item <= 4) {
                    boolean nameType = random.nextBoolean();

                    name = ChatColor.YELLOW + "Krampus' Accurate Mace of Ice";
                    llore = ChatColor.GRAY + "Forged out of pure gold, used by Krampus himself.";

                    is.setType(Material.GOLD_SPADE);


                    elem = 3;
                    elemamt = ThreadLocalRandom.current().nextInt(40, 50);
                    pureamt = ThreadLocalRandom.current().nextInt(30, 50);

                    mindmg = ThreadLocalRandom.current().nextInt(170, 190);
                    maxdmg = ThreadLocalRandom.current().nextInt(250, 290);
                }

                if (item == 5) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Krampus' Forged Helmet" : ChatColor.YELLOW + "Krampus' Worn Helmet";
                    llore = ChatColor.GRAY + "Forged out of pure gold, worn by Krampus himself.";

                    is.setType(Material.GOLD_HELMET);

                    armdps = 2;
                    dpsamt = 9;

                    hp = ThreadLocalRandom.current().nextInt(2000, 2350);
                    nrg = ThreadLocalRandom.current().nextInt(6, 6);
                    nrghp = 2;

                    vit = true;
                    vitamt = ThreadLocalRandom.current().nextInt(260, 300);
                }
                if (item == 6) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Krampus' Forged Chestplate" : ChatColor.YELLOW + "Krampus' Worn Chestplate";
                    llore = ChatColor.GRAY + "Forged out of pure gold, worn by Krampus himself.";

                    is.setType(Material.GOLD_CHESTPLATE);

                    armdps = 2;
                    dpsamt = 19;

                    hp = ThreadLocalRandom.current().nextInt(3900, 4700);
                    nrg = ThreadLocalRandom.current().nextInt(8, 8);
                    nrghp = 2;

                    vit = true;
                    vitamt = ThreadLocalRandom.current().nextInt(260, 300);
                }
                if (item == 7) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Krampus' Forged Leggings" : ChatColor.YELLOW + "Krampus' Worn Leggings";
                    llore = ChatColor.GRAY + "Forged out of pure gold, worn by Krampus himself.";

                    is.setType(Material.GOLD_LEGGINGS);

                    armdps = 2;
                    dpsamt = 19;

                    hp = ThreadLocalRandom.current().nextInt(3900, 4700);
                    nrg = ThreadLocalRandom.current().nextInt(8, 8);
                    nrghp = 2;

                    vit = true;
                    vitamt = ThreadLocalRandom.current().nextInt(260, 300);
                }
                if (item == 8) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Krampus' Forged Boots" : ChatColor.YELLOW + "Krampus' Worn Boots";
                    llore = ChatColor.GRAY + "Forged out of pure gold, worn by Krampus himself.";

                    is.setType(Material.GOLD_BOOTS);

                    armdps = 2;
                    dpsamt = 9;

                    hp = ThreadLocalRandom.current().nextInt(2000, 2350);
                    nrg = ThreadLocalRandom.current().nextInt(6, 6);
                    nrghp = 2;

                    vit = true;
                    vitamt = ThreadLocalRandom.current().nextInt(260, 300);
                }
                break;
            case "weakSkeletonEntity":
                tier = 5;
                rarityId = 2;
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";

                if (item <= 4) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Infernal Spear of the Death" : ChatColor.YELLOW + "Skeletal Keeper's Polearm";
                    llore = ChatColor.GRAY + "Mythical weapon forged out of an unknown material.";

                    is.setType(Material.GOLD_SPADE);

                    elem = 1;
                    elemamt = ThreadLocalRandom.current().nextInt(40, 50);

                    mindmg = ThreadLocalRandom.current().nextInt(220, 225);
                    maxdmg = ThreadLocalRandom.current().nextInt(265, 280);

                    life = true;
                    lifeamt = ThreadLocalRandom.current().nextInt(5, 8);


                }
                if (item == 5) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Infernal Keeper's Helmet" : ChatColor.YELLOW + "Soulstealer's Helmet";
                    llore = ChatColor.GRAY + "Mythical helmet forged out of an unknown material.";

                    is.setType(Material.GOLD_HELMET);

                    hp = ThreadLocalRandom.current().nextInt(2700, 3400);
                    hps = ThreadLocalRandom.current().nextInt(350, 350);
                    nrghp = 1;

                    dodge = random.nextBoolean();
                    dodgeamt = ThreadLocalRandom.current().nextInt(5, 5);

                    dex = true;
                    dexamt = ThreadLocalRandom.current().nextInt(100, 100);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(10, 12);
                }
                if (item == 6) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Infernal Keeper's Chestpiece" : ChatColor.YELLOW + "Soulstealer's Chestplate";
                    llore = ChatColor.GRAY + "Mythical chestpiece forged out of an unknown material.";

                    is.setType(Material.GOLD_CHESTPLATE);

                    hp = ThreadLocalRandom.current().nextInt(2700, 3400);
                    hps = ThreadLocalRandom.current().nextInt(350, 350);
                    nrghp = 1;

                    dodge = random.nextBoolean();
                    dodgeamt = ThreadLocalRandom.current().nextInt(5, 5);

                    dex = true;
                    dexamt = ThreadLocalRandom.current().nextInt(100, 100);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(20, 24);
                }
                if (item == 7) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Infernal Keeper's Leggings" : ChatColor.YELLOW + "Soulstealer's Leggings";
                    llore = ChatColor.GRAY + "Mythical leggings forged out of an unknown material.";

                    is.setType(Material.GOLD_LEGGINGS);

                    hp = ThreadLocalRandom.current().nextInt(2700, 3400);
                    hps = ThreadLocalRandom.current().nextInt(350, 350);
                    nrghp = 1;

                    dodge = random.nextBoolean();
                    dodgeamt = ThreadLocalRandom.current().nextInt(5, 5);

                    dex = true;
                    dexamt = ThreadLocalRandom.current().nextInt(100, 100);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(20, 24);
                }
                if (item == 8) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Infernal Keeper's Boots" : ChatColor.YELLOW + "Soulstealer's Boots";
                    llore = ChatColor.GRAY + "Mythical boots forged out of an unknown material.";

                    is.setType(Material.GOLD_BOOTS);

                    hp = ThreadLocalRandom.current().nextInt(2700, 3400);
                    hps = ThreadLocalRandom.current().nextInt(350, 350);
                    nrghp = 1;

                    dodge = random.nextBoolean();
                    dodgeamt = ThreadLocalRandom.current().nextInt(5, 5);

                    dex = true;
                    dexamt = ThreadLocalRandom.current().nextInt(100, 100);

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
                    llore = ChatColor.GRAY + "Mythical weapon forged out of an unknown material.";

                    is.setType(Material.GOLD_SWORD);

                    elem = 2;
                    elemamt = ThreadLocalRandom.current().nextInt(45, 65);

                    mindmg = ThreadLocalRandom.current().nextInt(390, 420);
                    maxdmg = ThreadLocalRandom.current().nextInt(490, 530);

                    life = true;
                    lifeamt = ThreadLocalRandom.current().nextInt(3, 5);

                }

                if (item == 5) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Helmet Of The Death" : ChatColor.YELLOW + "Mythical Skeletal Helmet";
                    llore = ChatColor.GRAY + "Worn by The Restless Skeleton Deathlord himself..";

                    is.setType(Material.GOLD_HELMET);

                    hp = ThreadLocalRandom.current().nextInt(3000, 3300);
                    nrg = ThreadLocalRandom.current().nextInt(7, 8);
                    nrghp = 2;

                    dodge = true;
                    dodgeamt = ThreadLocalRandom.current().nextInt(5, 9);

                    vit = random.nextBoolean();
                    vitamt = ThreadLocalRandom.current().nextInt(200, 225);
                    intel = random.nextBoolean();
                    intamt = ThreadLocalRandom.current().nextInt(240, 285);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(10, 12);
                }
                if (item == 6) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Skeletal Soul Protector" : ChatColor.YELLOW + "The Skeleton Deathlord's Chestplate";
                    llore = ChatColor.GRAY + "Worn by The Restless Skeleton Deathlord himself..";

                    is.setType(Material.GOLD_CHESTPLATE);

                    hp = ThreadLocalRandom.current().nextInt(6000, 6400);
                    nrg = ThreadLocalRandom.current().nextInt(8, 10);
                    nrghp = 2;
                    dodge = true;

                    dodgeamt = ThreadLocalRandom.current().nextInt(5, 9);

                    vit = true;
                    vitamt = ThreadLocalRandom.current().nextInt(240, 285);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(16, 24);
                    intel = true;
                    intamt = ThreadLocalRandom.current().nextInt(240, 285);
                }
                if (item == 7) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.RED + "Skeletal Death Leggings" : ChatColor.RED + "The Skeleton Deathlord's Leggings";
                    llore = ChatColor.GRAY + "Worn by The Restless Skeleton Deathlord himself..";

                    is.setType(Material.GOLD_LEGGINGS);

                    hp = ThreadLocalRandom.current().nextInt(6000, 6400);
                    nrg = ThreadLocalRandom.current().nextInt(8, 10);
                    nrghp = 2;
                    dodge = true;

                    dodgeamt = ThreadLocalRandom.current().nextInt(5, 9);

                    vit = true;
                    vitamt = ThreadLocalRandom.current().nextInt(240, 285);

                    armdps = 2;
                    dpsamt = ThreadLocalRandom.current().nextInt(16, 24);
                    intel = true;
                    intamt = ThreadLocalRandom.current().nextInt(240, 285);
                }
                if (item == 8) {
                    boolean nameType = random.nextBoolean();

                    name = nameType ? ChatColor.YELLOW + "Skeletal Boots of Intellect" : ChatColor.YELLOW + "The Skeleton Deathlord's Boots";
                    llore = ChatColor.GRAY + "Worn by The Restless Skeleton Deathlord himself..";

                    is.setType(Material.GOLD_BOOTS);

                    hp = ThreadLocalRandom.current().nextInt(3000, 3300);
                    nrg = ThreadLocalRandom.current().nextInt(7, 8);
                    nrghp = 2;

                    dodge = true;
                    dodgeamt = ThreadLocalRandom.current().nextInt(5, 9);

                    vit = random.nextBoolean();
                    vitamt = ThreadLocalRandom.current().nextInt(200, 225);
                    intel = random.nextBoolean();
                    intamt = ThreadLocalRandom.current().nextInt(240, 285);

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
        if (dungeonBoss.equals("bossSkeletonDungeon")) is.addEnchantment(Enchants.glow, 1);

        NBTAccessor nbtAccessor = new NBTAccessor(is).check();
        nbtAccessor.setString("rarityType", color.name());
        nbtAccessor.setDouble("namedElite", 1D);

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
        int hps = 0;
        int nrg = 0;

        // Tier 1 - Mitsuki
        if (mobname.equalsIgnoreCase("mitsuki")) {
            nrghp = 2;
            armdps = 1;
            vit = true;
            elem = 1;
            block = true;
            life = true;
            elemamt = ThreadLocalRandom.current().nextInt(6, 8);
            lifeamt = random.nextInt(5) + 30;
            mindmg = random.nextInt(16, 18);
            maxdmg = random.nextInt(19, 22);

            // Sword of Bloodthirst
            if (item <= 4) {
                name = "Mitsuki's Bloodthirst Blade";
                is.setType(Material.WOOD_SWORD);
                llore = llore + "The Master of Ruins' blood-stained ridged Sword.";
            }

            // Leather Coif
            if (item == 5) {
                nrg = random.nextInt(1) + 3;
                blockamt = random.nextInt(2) + 3;
                dpsamt = 1;
                vitamt = 15;
                hp = random.nextInt(95, 123);
                name = "Mitsuki's Torn Leather Coif";
                is.setType(Material.LEATHER_HELMET);
                llore = llore + "A ripped remains of a Leather Coif far from industry standards.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }

            // Dirty Leather Rags
            if (item == 6) {
                nrg = random.nextInt(1) + 5;
                blockamt = random.nextInt(2) + 3;
                vitamt = 30;
                dpsamt = random.nextInt(2) + 2;
                hp = random.nextInt(200, 240);
                name = "Mitsuki's Filthy Leather Rags";
                is.setType(Material.LEATHER_CHESTPLATE);
                llore = llore + "Blood-stained rags that reek of Zombie flesh.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }

            // Ripped Leather Pants
            if (item == 7) {
                nrg = random.nextInt(1) + 5;
                blockamt = random.nextInt(2) + 3;
                vitamt = 30;
                dpsamt = random.nextInt(2) + 2;
                hp = random.nextInt(200, 240);
                name = "Mitsuki's Ripped Leather Pants";
                is.setType(Material.LEATHER_LEGGINGS);
                llore = llore + "Can be referred to as 'shorts' due to intensive ripping.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }

            // Leather Sandals
            if (item == 8) {
                nrg = random.nextInt(1) + 3;
                blockamt = random.nextInt(2) + 3;
                dpsamt = 1;
                vitamt = 15;
                hp = random.nextInt(95, 123);
                name = "Mitsuki's Bloodstained Leather Sandals";
                is.setType(Material.LEATHER_BOOTS);
                llore = llore + "Blood-stained sandals. Not very comfortable.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }

            tier = 1;
        }

// Tier 1 - Thura
        if (mobname.equalsIgnoreCase("thura")) {
            nrghp = 2;
            armdps = 1;
            dodge = true;
            str = true;
            crit = true;
            elem = 2;
            elemamt = ThreadLocalRandom.current().nextInt(6, 8);
            critamt = random.nextInt(2) + 6;
            mindmg = random.nextInt(15, 18);
            maxdmg = random.nextInt(20, 25);

            // Sword of Bloodthirst
            if (item <= 4) {
                name = "Thura's Vicious Axe";
                is.setType(Material.WOOD_AXE);
                llore = llore + "The Master of Ruins' blood-stained vicious axe.";
            }

            // Leather Coif
            if (item == 5) {
                nrg = random.nextInt(1) + 3;
                dodgeamt = random.nextInt(2) + 3;
                dpsamt = 1;
                stramt = 15;
                hp = random.nextInt(95, 123);
                name = "Thura's Torn Leather Coif";
                is.setType(Material.LEATHER_HELMET);
                llore = llore + "A ripped remains of a Leather Coif far from industry standards.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }

            // Dirty Leather Rags
            if (item == 6) {
                nrg = random.nextInt(1) + 5;
                dodgeamt = random.nextInt(2) + 3;
                stramt = 30;
                dpsamt = random.nextInt(2) + 2;
                hp = random.nextInt(200, 240);
                name = "Thura's Grimy Leather Rags";
                is.setType(Material.LEATHER_CHESTPLATE);
                llore = llore + "Blood-stained rags that reek of Zombie flesh.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }

            // Ripped Leather Pants
            if (item == 7) {
                nrg = random.nextInt(1) + 5;
                dodgeamt = random.nextInt(2) + 3;
                stramt = 30;
                dpsamt = random.nextInt(2) + 2;
                hp = random.nextInt(200, 240);
                name = "Thura's Tattered Leather Pants";
                is.setType(Material.LEATHER_LEGGINGS);
                llore = llore + "Can be referred to as 'shorts' due to intensive ripping.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }

            // Leather Sandals
            if (item == 8) {
                nrg = random.nextInt(1) + 3;
                dodgeamt = random.nextInt(2) + 3;
                dpsamt = 1;
                stramt = 15;
                hp = random.nextInt(95, 123);
                name = "Thura's Bloodied Leather Sandals";
                is.setType(Material.LEATHER_BOOTS);
                llore = llore + "Blood-stained sandals. Not very comfortable.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }

            tier = 1;
        }

        // Tier 2 - Copjak
        if (mobname.equalsIgnoreCase("copjak")) {
            nrghp = 2;
            armdps = 1;
            vit = true;
            elem = 2;
            elemamt = 14;
            crit = true;
            critamt = random.nextInt(3) + 8;
            mindmg = random.nextInt(35, 40);
            maxdmg = random.nextInt(43, 48);

            // Cop'Jaks Deadly Sword
            if (item <= 4) {
                name = "Cop'Jak's Wicked Blade";
                is.setType(Material.STONE_SWORD);
                llore = llore + "A long wicked sword of Trollish design, crafted by Cop'Jak.";
            }

            // Cop'Jaks Shaman Headgear
            if (item == 5) {
                vitamt = 25;
                nrg = random.nextInt(1) + 4;
                hp = random.nextInt(215, 235);
                dpsamt = random.nextInt(2) + 3;
                name = "Cop'Jak's Shamanistic Headgear";
                is.setType(Material.CHAINMAIL_HELMET);
                llore = llore + "A standard Shaman's headgear consisting of a bear's head, worn by Cop'Jak.";
            }

            // Cop'Jaks greased Chainmail Chestpiece
            if (item == 6) {
                vitamt = 45;
                nrg = random.nextInt(1) + 5;
                hp = random.nextInt(390, 470);
                dpsamt = random.nextInt(2) + 6;
                name = "Cop'Jak's Greased Chainmail Chestpiece";
                is.setType(Material.CHAINMAIL_CHESTPLATE);
                llore = llore + "A bad fit made for the broad chests of Trolls, worn by Cop'Jak.";
            }

            // Cop'Jaks Chainlinked Pants
            if (item == 7) {
                vitamt = 45;
                nrg = random.nextInt(1) + 5;
                hp = random.nextInt(390, 470);
                dpsamt = random.nextInt(2) + 6;
                name = "Cop'Jak's Chainlinked Pants";
                is.setType(Material.CHAINMAIL_LEGGINGS);
                llore = llore + "Large greased and ready for action, worn by Cop'Jak.";
            }

            // Cop'Jaks Chainmail Boots
            if (item == 8) {
                vitamt = 25;
                nrg = random.nextInt(1) + 4;
                hp = random.nextInt(215, 235);
                dpsamt = random.nextInt(2) + 3;
                name = "Cop'Jak's Spiked Chainmail Boots";
                is.setType(Material.CHAINMAIL_BOOTS);
                llore = llore + "Spiked Chainmail boots, worn by Cop'Jak.";
            }

            tier = 2;
        }

// Tier 2 - Risk_Elite
        if (mobname.equalsIgnoreCase("risk_Elite")) {
            nrghp = 2;
            armdps = 1;
            str = true;
            elem = 3;
            life = true;
            elemamt = 15;
            pure = true;
            lifeamt = ThreadLocalRandom.current().nextInt(3, 5);
            pureamt = ThreadLocalRandom.current().nextInt(12, 18);
            intel = true;
            mindmg = random.nextInt(35, 40);
            maxdmg = random.nextInt(43, 48);

            // Riskan's Deadly Battle Axe
            if (item <= 4) {
                name = "Riskan's Fury Battle Axe";
                is.setType(Material.STONE_AXE);
                llore = llore + "Riskan's battle axe of fury, capable of unleashing devastating strikes.";
            }

            // Riskan's Chainlinked Headgear
            if (item == 5) {
                stramt = 50;
                intamt = 30;
                nrg = random.nextInt(1) + 4;
                hp = random.nextInt(215, 235);
                dpsamt = random.nextInt(2) + 3;
                name = "Riskan's Glorious Chainlinked Headgear";
                is.setType(Material.CHAINMAIL_HELMET);
                llore = llore + "A lustrous helmchen that embodies the glory of Riskan.";
            }

            // Riskan's Lapus Encrusted Chest Piece
            if (item == 6) {
                stramt = 45;
                intamt = 40;
                nrg = random.nextInt(2) + 5;
                hp = random.nextInt(390, 470);
                dpsamt = random.nextInt(2) + 6;
                name = "Riskan's Glimmering Lapus Chest Piece";
                is.setType(Material.CHAINMAIL_CHESTPLATE);
                llore = llore + "The breastplate of the lapis lord, adorned with precious lapus gems.";
            }

            // Riskan's Belted Trousers
            if (item == 7) {
                stramt = 45;
                intamt = 40;
                nrg = random.nextInt(2) + 5;
                hp = random.nextInt(390, 470);
                dpsamt = random.nextInt(2) + 6;
                name = "Riskan's Tightly Fastened Belted Trousers";
                is.setType(Material.CHAINMAIL_LEGGINGS);
                llore = llore + "The tightly fastened leggings of Riskan himself, providing unmatched agility.";
            }

            // Riskan's Bejeweled Boots
            if (item == 8) {
                stramt = 50;
                intamt = 30;
                nrg = random.nextInt(1) + 4;
                hp = random.nextInt(215, 235);
                dpsamt = random.nextInt(2) + 3;
                name = "Riskan's Crowned Bejeweled Boots";
                is.setType(Material.CHAINMAIL_BOOTS);
                llore = llore + "Riskan's boots of fire, adorned with precious jewels and crowned with power.";
            }

            tier = 2;
        }

        //Tier 3
        if (mobname.equalsIgnoreCase("impa")) {
            nrghp = 2;
            armdps = 1;
            str = true;
            block = true;
            crit = true;
            life = true;
            elem = 2;
            elemamt = 15;
            lifeamt = 15;
            critamt = random.nextInt(3) + 7;
            mindmg = random.nextInt(50, 58);
            maxdmg = random.nextInt(70, 90);
            if (item <= 4) {
                name = "Impa's Dreaded Polearm";
                is.setType(Material.IRON_SPADE);
                llore = llore + "The spearhead of the initial attack on Avalon.";
                rarityId = 2;
            }
            if (item == 5) {
                stramt = 75;
                blockamt = 8;
                nrg = random.nextInt(1) + 5;
                hp = random.nextInt(430, 530);
                dpsamt = random.nextInt(2) + 5;
                name = "Crooked Battle Mask";
                is.setType(Material.IRON_HELMET);
                llore = llore + "A skeleton general's black mask";
                rarityId = 2;
            }
            if (item == 6) {
                stramt = 75;
                blockamt = 12;
                nrg = random.nextInt(2) + 5;
                hp = random.nextInt(850, 1050);
                dpsamt = random.nextInt(2) + 5;
                name = "Haunting Platemail of Fright";
                is.setType(Material.IRON_CHESTPLATE);
                llore = llore + "A breastplate with the symbol of Impas army carved into it.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                stramt = 100;
                blockamt = 12;
                nrg = random.nextInt(2) + 5;
                hp = random.nextInt(850, 1050);
                dpsamt = random.nextInt(2) + 5;
                name = "Warding Skeletal Leggings";
                is.setType(Material.IRON_LEGGINGS);
                llore = llore + "Spiked bone leggings of greater skeleton invaders.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                stramt = 75;
                blockamt = 8;
                nrg = random.nextInt(1) + 5;
                hp = random.nextInt(430, 530);
                dpsamt = random.nextInt(2) + 5;
                name = "Skeletal Death Walkers";
                is.setType(Material.IRON_BOOTS);
                llore = llore + "The boots with which Impa treaded into this land.";
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
            lifeamt = 10;
            elemamt = ThreadLocalRandom.current().nextInt(15, 20);
            critamt = random.nextInt(2) + 8;
            mindmg = random.nextInt(65, 75);
            maxdmg = random.nextInt(80, 95);
            if (item <= 4) {
                name = "The Thieving Axe of the Greed King";
                is.setType(Material.IRON_AXE);
                llore = llore + "Extremely sharp with a hilt encrusted with gems.";
                rarityId = 2;
            }
            if (item == 5) {
                stramt = 75;
                blockamt = 10;
                nrg = random.nextInt(2) + 6;
                hp = random.nextInt(430, 530);
                dpsamt = random.nextInt(2) + 5;
                name = "The King of Greeds Golden Helm";
                is.setType(Material.IRON_HELMET);
                llore = llore + "Iron helm plated with gold";
                rarityId = 2;
            }
            if (item == 6) {
                stramt = 75;
                blockamt = 12;
                nrg = random.nextInt(3) + 8;
                hp = random.nextInt(850, 1050);
                dpsamt = random.nextInt(2) + 5;
                name = "The Gem Encrusted Plate of the Greed King";
                is.setType(Material.IRON_CHESTPLATE);
                llore = llore + "A broad chestplate fit with rubies and diamonds.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                stramt = 75;
                blockamt = 12;
                nrg = random.nextInt(3) + 8;
                hp = random.nextInt(850, 1050);
                dpsamt = random.nextInt(2) + 5;
                name = "The Gem Encrusted Legs of the Greed King";
                is.setType(Material.IRON_LEGGINGS);
                llore = llore + "Iron leggings fit with emeralds and amethysts.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                stramt = 75;
                blockamt = 10;
                nrg = random.nextInt(3) + 6;
                hp = random.nextInt(430, 530);
                dpsamt = random.nextInt(2) + 5;
                name = "The King of Greeds Golden Boots";
                is.setType(Material.IRON_BOOTS);
                llore = llore + "Golden boots that are completely covered in mud.";
                rarityId = 2;
            }
            tier = 3;
        }
        if (mobname.equalsIgnoreCase("skeletonking")) {
            nrghp = 1;
            armdps = 1;
            vit = true;
            pure = true;
            acc = true;
            pureamt = 15;
            accamt = random.nextInt(8) + 25;
            mindmg = random.nextInt(10) + 60;
            maxdmg = random.nextInt(10) + 80;
            if (item <= 4) {
                name = "The Skeleton Kings Sword of Banishment";
                is.setType(Material.IRON_SWORD);
                llore = llore + "A powerful sword enhanced with the soul of the Skeleton King.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 5) {
                vitamt = 75;
                nrg = random.nextInt(4) + 6;
                hp = random.nextInt(430, 530);
                dpsamt = random.nextInt(2) + 4;
                name = "The Skeleton Kings Soul Helmet";
                is.setType(Material.IRON_HELMET);
                llore = llore + "A shadowy transparent helmet.";
                rarityId = 2;
            }
            if (item == 6) {
                vitamt = 100;
                nrg = random.nextInt(4) + 8;
                hp = random.nextInt(850, 1050);
                dpsamt = 8;
                name = "The Skeleton Kings Soul Armour";
                is.setType(Material.IRON_CHESTPLATE);
                llore = llore + "Armor imbued with the power of the Skeleton king.";
                rarityId = 2;
            }
            if (item == 7) {
                vitamt = 100;
                nrg = random.nextInt(4) + 8;
                hp = random.nextInt(850, 1050);
                dpsamt = 8;
                name = "The Skeleton Kings Soul Leggings";
                is.setType(Material.IRON_LEGGINGS);
                llore = llore + "Resistant to the most powerful of Physical Damage.";
                rarityId = 2;
            }
            if (item == 8) {
                vitamt = 75;
                nrg = random.nextInt(4) + 6;
                hp = random.nextInt(430, 530);
                dpsamt = random.nextInt(2) + 4;
                name = "The Skeleton Kings Soul Boots";
                is.setType(Material.IRON_BOOTS);
                llore = llore + "The shining boots of a king.";
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
                llore = llore + "The cruel Staff of an ancient warrior.";
                rarityId = 2;
            }
            if (item == 5) {
                stramt = 75;
                blockamt = 8;
                nrg = random.nextInt(3) + 3;
                hp = random.nextInt(1200, 1400);
                dpsamt = random.nextInt(5) + 9;
                name = "Duranor's Melted Mask";
                is.setType(Material.DIAMOND_HELMET);
                llore = llore + "An ancient helmet forged from the remains of kings.";
                rarityId = 2;
            }
            if (item == 6) {
                stramt = 75;
                blockamt = 8;
                hps = random.nextInt(25) + 80;
                hp = random.nextInt(2820, 3020);
                dpsamt = random.nextInt(5) + 9;
                name = "Duranor's Chestguard of Ancient Power";
                is.setType(Material.DIAMOND_CHESTPLATE);
                llore = llore + "A chestguard imbued with unsettling power.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                stramt = 75;
                blockamt = 8;
                hps = random.nextInt(25) + 80;
                hp = random.nextInt(2820, 3020);
                dpsamt = random.nextInt(5) + 9;
                name = "Duranor's Gore-Soaked Platelegs";
                is.setType(Material.DIAMOND_LEGGINGS);
                llore = llore + "Platelegs soaked in the blood of the infidels.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                stramt = 75;
                blockamt = 8;
                hps = random.nextInt(25) + 80;
                hp = random.nextInt(1200, 1400);
                dpsamt = random.nextInt(5) + 9;
                name = "Duranor's Warboots of Absolute Eradication";
                is.setType(Material.DIAMOND_BOOTS);
                llore = llore + "Sabatons passed down through generations of ancient warriors.";
                rarityId = 2;
            }
            tier = 4;
        }

        if (mobname.equalsIgnoreCase("spiderQueen")) {
            // HPS --- DPS
            nrghp = 1;
            armdps = 1;
            // still don't know which is deeps leaving as is :shrug:

            elem = 2;

            intel = true;
            dodge = true;
            acc = true;

            intamt = random.nextInt(26) + 49;
            dodgeamt = random.nextInt(3) + 5;

            elemamt = random.nextInt(11) + 35;
            accamt = ThreadLocalRandom.current().nextInt(10, 16);

            mindmg = random.nextInt(16) + 80;
            maxdmg = random.nextInt(170, 190);
            if (item <= 4) {
                name = "The Spider Queen's Fang";
                is.setType(Material.IRON_SWORD);
                llore = llore + "A fang still dripping with poison.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 5) {
                dpsamt = random.nextInt(3) + 3;
                hp = random.nextInt(900, 1000);
                hps = random.nextInt(16) + 49;
                name = "The Spider Queen's Cowl";
                is.setType(Material.IRON_HELMET);
                llore = llore + "A Cowl protected by carapace";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(3) + 3;
                hp = random.nextInt(2000, 2400);
                hps = random.nextInt(16) + 49;
                name = "Spider Queen's Carapace Chestplate";
                is.setType(Material.IRON_CHESTPLATE);
                llore = llore + "A Chestplate protected by thick carapace.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(3) + 3;
                hp = random.nextInt(2000, 2400);
                nrg = random.nextInt(4) + 4;
                name = "Spider Queen's Carapce Leggings";
                is.setType(Material.IRON_LEGGINGS);
                llore = llore + "Legs protected by thick carapace.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(3) + 3;
                hp = random.nextInt(900, 1000);
                nrg = random.nextInt(4) + 4;
                name = "Spider Queens Carapace Boots";
                is.setType(Material.IRON_BOOTS);
                llore = llore + "Boots protected by thick carapace.";
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

            critamt = random.nextInt(4) + 10;
            life = true;
            lifeamt = random.nextInt(4) + 8;
            mindmg = random.nextInt(180, 195);
            maxdmg = random.nextInt(210, 230);
            if (item <= 4) {
                name = "The Butchers Bloody Cleaver";
                is.setType(Material.DIAMOND_SWORD);
                llore = llore + "This cleaver was used for much more than cutting beef.";
                rarityId = 2;
            }
            if (item == 5) {
                dpsamt = random.nextInt(3) + 5;
                vitamt = 100;
                hp = random.nextInt(875, 1000);
                nrg = random.nextInt(4) + 6;
                name = "The Butchers Bloody Helm";
                is.setType(Material.DIAMOND_HELMET);
                llore = llore + "The bloody dented headpiece of the butcher.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(2) + 6;
                vitamt = 160;
                hp = random.nextInt(1850, 2300);
                nrg = random.nextInt(4) + 6;
                name = "The Butchers Tattered Apron";
                is.setType(Material.DIAMOND_CHESTPLATE);
                llore = llore + "An apron covered in bits of gut and flesh.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(2) + 6;
                vitamt = 160;
                hp = random.nextInt(1850, 2300);
                nrg = random.nextInt(4) + 6;
                name = "The Butchers Bloody Chaps";
                is.setType(Material.DIAMOND_LEGGINGS);
                llore = llore + "Covered in streaks of blood from wiping his blade.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(3) + 5;
                vitamt = 100;
                hp = random.nextInt(875, 1000);
                nrg = random.nextInt(4) + 6;
                name = "The Butchers Blood Soaked Boots";
                is.setType(Material.DIAMOND_BOOTS);
                llore = llore + "Boots soaked in the blood of his enemies.";
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
            elemamt = random.nextInt(25) + 60;
            crit = true;
            critamt = random.nextInt(4) + 8;
            mindmg = random.nextInt(145, 160);
            maxdmg = random.nextInt(170, 185);
            if (item <= 4) {
                name = "Blayshans Wicked Axe";
                is.setType(Material.DIAMOND_AXE);
                llore = llore + "An Axe with the face of the cursed Blayshan carved into it.";
                rarityId = 2;
            }
            if (item == 5) {
                dpsamt = random.nextInt(3) + 4;
                stramt = 150;
                hp = random.nextInt(875, 1000);
                nrg = random.nextInt(2) + 4;
                name = "Blayshans Accursed Helmet";
                is.setType(Material.DIAMOND_HELMET);
                llore = llore + "A weirdly shaped aqua blue helmet.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(3) + 5;
                stramt = 200;
                hp = random.nextInt(1850, 2300);
                nrg = random.nextInt(1) + 7;
                name = "Blayshans Wicked Horned Platemail";
                is.setType(Material.DIAMOND_CHESTPLATE);
                llore = llore + "Not well made but light with studded mail fists.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(3) + 5;
                stramt = 200;
                hp = random.nextInt(1850, 2300);
                nrg = random.nextInt(1) + 7;
                name = "Blayshans Wicked Horned Leggings";
                is.setType(Material.DIAMOND_LEGGINGS);
                llore = llore + "Glistening with the blood of fallen enemies.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(3) + 4;
                stramt = 150;
                hp = random.nextInt(875, 1000);
                nrg = random.nextInt(2) + 4;
                name = "Blayshans Platemail Boots";
                is.setType(Material.DIAMOND_BOOTS);
                llore = llore + "A pair of boots shaped to fit a Naga.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            tier = 4;
        }
        if (mobname.equalsIgnoreCase("spectralKnight")) {
            nrghp = 2;
            armdps = 1;
            crit = true;
            block = true;


            acc = true;

            accamt = random.nextInt(8) + 22;

            critamt = random.nextInt(3) + 10;

            mindmg = random.nextInt(10) + 130;
            maxdmg = random.nextInt(25) + 160;
            if (item <= 4) {
                name = "Spectral Rapier";
                is.setType(Material.DIAMOND_SWORD);
                llore = llore + "A Rapier that can pierce through the thickest of armor with ease.";
                rarityId = 2;
            }
            if (item == 5) {
                dpsamt = random.nextInt(6) + 5;
                hp = random.nextInt(875, 1000);
                nrg = random.nextInt(2) + 4;
                blockamt = 8;
                name = "Spectral Helmet";
                is.setType(Material.DIAMOND_HELMET);
                llore = llore + "A Helmet that can fade in and out of reality in the blink of an eye.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(6) + 10;
                hp = random.nextInt(1850, 2300);
                nrg = random.nextInt(0) + 6;
                blockamt = 8;
                name = "Spectral Platemail";
                is.setType(Material.DIAMOND_CHESTPLATE);
                llore = llore + "A large Platemail that can fade in and out of reality in the blink of an eye.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(6) + 10;
                hp = random.nextInt(1850, 2300);
                nrg = random.nextInt(0) + 6;
                blockamt = 8;
                name = "Spectral Leggings";
                is.setType(Material.DIAMOND_LEGGINGS);
                llore = llore + "A Pair of Pants that fade in and out of reality in the blink of an eye.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(6) + 5;
                hp = random.nextInt(875, 1000);
                nrg = random.nextInt(2) + 4;
                blockamt = 8;
                name = "Spectral Boots";
                is.setType(Material.DIAMOND_BOOTS);
                llore = llore + "A Pair of Boots that fade in and out of reality in the blink of an eye.";
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

            intel = true;
            block = true;

            dexamt = random.nextInt(25) + 75;
            blockamt = random.nextInt(3) + 9;

            critamt = random.nextInt(1) + 8;
            elemamt = random.nextInt(20) + 50;

            mindmg = random.nextInt(100, 110);
            maxdmg = random.nextInt(125, 140);

            if (item <= 4) {
                name = "Watchmaster's Poleaxe";
                is.setType(Material.DIAMOND_SPADE);
                llore = llore + "The Watchmaster's massive poleaxe.";
                rarityId = 2;
            }
            if (item == 5) {
                dpsamt = random.nextInt(3) + 9;
                hp = random.nextInt(875, 1000);
                nrg = random.nextInt(0) + 6;
                intel = true;
                intamt = random.nextInt(0) + 125;
                name = "Watchmaster's Greathelm";
                is.setType(Material.DIAMOND_HELMET);
                llore = llore + "The Watchmaster's large helmet.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(3) + 12;
                hp = random.nextInt(1850, 2300);
                nrg = random.nextInt(1) + 7;
                intel = true;
                intamt = random.nextInt(0) + 155;
                name = "Watchmaster's Pride";
                is.setType(Material.DIAMOND_CHESTPLATE);
                llore = llore + "The Watchmaster's decorated chestplate.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(3) + 12;
                hp = random.nextInt(1850, 2300);
                nrg = random.nextInt(1) + 7;
                intel = true;
                intamt = random.nextInt(0) + 155;
                name = "Watchmaster's Platelegs";
                is.setType(Material.DIAMOND_LEGGINGS);
                llore = llore + "The Watchmaster's platemail leggings.";
                rarity = ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(3) + 9;
                hp = random.nextInt(875, 1000);
                nrg = random.nextInt(0) + 6;
                intel = true;
                intamt = random.nextInt(0) + 125;
                name = "Watchmaster's Boots";
                is.setType(Material.DIAMOND_BOOTS);
                llore = llore + "The Watchmaster's large boots.";
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
            elemamt = random.nextInt(20) + 50;

            mindmg = random.nextInt(280, 295);
            maxdmg = random.nextInt(345, 375);
            if (item <= 4) {
                name = "Jayden's Swift War Axe";
                is.setType(Material.GOLD_AXE);
                llore = llore + "King Jayden's sturdy axe encrusted with diamonds.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 5) {
                dpsamt = random.nextInt(2) + 6;
                vitamt = random.nextInt(0) + 250;
                hp = random.nextInt(2000, 2350);
                nrg = random.nextInt(1) + 6;
                dodgeamt = random.nextInt(3) + 8;
                name = "Jayden's Beautiful Crown";
                is.setType(Material.GOLD_HELMET);
                llore = llore + "A golden crown forged by Maltai's great smith.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(4) + 12;
                vitamt = 300;
                hp = random.nextInt(3900, 4700);
                nrg = random.nextInt(3) + 6;
                dodgeamt = random.nextInt(3) + 8;
                name = "Jayden's Stolen Chest Guard";
                is.setType(Material.GOLD_CHESTPLATE);
                llore = llore + "The protection from the last blow of the fallen knight.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(4) + 12;
                vitamt = 300;
                hp = random.nextInt(3900, 4700);
                nrg = random.nextInt(3) + 8;
                dodgeamt = random.nextInt(3) + 6;
                name = "Jayden's Godlike Leggings";
                is.setType(Material.GOLD_LEGGINGS);
                llore = llore + "The legendary leggings of a fallen lord.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(2) + 6;
                vitamt = random.nextInt(0) + 250;
                hp = random.nextInt(2000, 2350);
                nrg = random.nextInt(1) + 7;
                dodgeamt = random.nextInt(3) + 5;
                name = "Jayden's Legendary Footwear";
                is.setType(Material.GOLD_BOOTS);
                llore = llore + "Sturdy golden footwear owner by the king himself.";
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
            crit = true;
            critamt = 8;
            dodge = true;
            elemamt = 70;
            mindmg = random.nextInt(280, 295);
            maxdmg = random.nextInt(345, 375);
            if (item <= 4) {
                name = "Kilatans Axe of Destruction";
                is.setType(Material.GOLD_AXE);
                llore = llore + "A powerful staff imbued with the magics of Kilatan";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 5) {
                dpsamt = random.nextInt(2) + 6;
                intamt = 290;
                hp = random.nextInt(2000, 2350);
                nrg = random.nextInt(2) + 6;
                dodgeamt = random.nextInt(3) + 5;
                name = "Kilatans Crown of Death";
                is.setType(Material.GOLD_HELMET);
                llore = llore + "A golden crown of tyranny and power.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(4) + 12;
                intamt = 330;
                hp = random.nextInt(3900, 4700);
                nrg = random.nextInt(2) + 7;
                dodgeamt = random.nextInt(2) + 6;
                name = "Kilatans Legendary Platemail";
                is.setType(Material.GOLD_CHESTPLATE);
                llore = llore + "The Legendary platemail piece of the Demon Lord Kilatan.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(4) + 12;
                intamt = 330;
                hp = random.nextInt(3900, 4700);
                nrg = random.nextInt(2) + 7;
                dodgeamt = random.nextInt(2) + 6;
                name = "Kilatans Legendary Leggings";
                is.setType(Material.GOLD_LEGGINGS);
                llore = llore + "You can feel the power emanating from this armor piece.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(2) + 6;
                intamt = 290;
                hp = random.nextInt(2000, 2350);
                nrg = random.nextInt(2) + 7;
                dodgeamt = random.nextInt(3) + 5;
                name = "Kilatans Legendary Boots";
                is.setType(Material.GOLD_BOOTS);
                llore = llore + "Boots that carried the weight of the underworld.";
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
                llore = llore + "The axe wielded by only the strongest of champions.";
                rarityId = 2;
            }
            if (item == 5) {
                dpsamt = random.nextInt(3) + 12;
                hp = random.nextInt(2500, 3000);
                nrg = random.nextInt(2) + 4;
                name = ChatColor.YELLOW + "The King's Crown";
                is.setType(Material.GOLD_HELMET);
                llore = llore + "The king's icy crown.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 6) {
                dpsamt = random.nextInt(3) + 12;
                hp = random.nextInt(5600, 6000);
                nrg = random.nextInt(2) + 5;
                name = ChatColor.YELLOW + "The King's Chestplate of Frost";
                is.setType(Material.GOLD_CHESTPLATE);
                llore = llore + "The king's legendary chestplate.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 7) {
                dpsamt = random.nextInt(3) + 12;
                hp = random.nextInt(5600, 6000);
                nrg = random.nextInt(2) + 5;
                name = ChatColor.YELLOW + "The King's Platelegs of Frost";
                is.setType(Material.GOLD_LEGGINGS);
                llore = llore + "The king's armored platelegs.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            }
            if (item == 8) {
                dpsamt = random.nextInt(3) + 9;
                hp = random.nextInt(2500, 3000);
                nrg = random.nextInt(2) + 4;
                name = ChatColor.YELLOW + "The King's Boots of Frost";
                is.setType(Material.GOLD_BOOTS);
                llore = llore + "The king's large boots.";
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
            if (PracticeServer.t6) {
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
            } else {
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
                if (PracticeServer.t6) {
                    is.setType(Material.DIAMOND_SWORD);
                } else {
                    is.setType(Material.GOLD_SWORD);
                }
                llore = llore + "A Legendary Sword forged in the deepest depths of the Frozen Heart.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                rarityId = 2;
            } else if (item == 5) {
                name = "Frost-Forged Crown";
                llore = llore + "A Legendary Helmet made of the purest of ice from the North.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                if (PracticeServer.t6) {
                    dpsamt = random.nextInt(4) + 12;
                    hp = random.nextInt(6000, 6200);
                    nrg = random.nextInt(3) + 6;
                    is.setType(Material.LEATHER_HELMET);
                } else {
                    dpsamt = random.nextInt(3) + 11;
                    hp = random.nextInt(751) + 3000;
                    nrg = random.nextInt(2) + 5;
                    is.setType(Material.GOLD_HELMET);
                }
                rarityId = 2;
            } else if (item == 6) {
                name = "Treasure of the Crypt";
                llore = llore + "A heavily armored chestpiece adorned with beautiful blue jewels.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                if (PracticeServer.t6) {
                    dpsamt = random.nextInt(5) + 18;
                    hp = random.nextInt(12500, 13500);
                    nrg = random.nextInt(4) + 6;
                    is.setType(Material.LEATHER_CHESTPLATE);
                } else {
                    dpsamt = random.nextInt(4) + 18;
                    hp = random.nextInt(1501) + 6000;
                    nrg = random.nextInt(2) + 5;
                    is.setType(Material.GOLD_CHESTPLATE);
                }
                rarityId = 2;
            } else if (item == 7) {
                name = "Unbroken Greaves";
                llore = llore + "Greaves made from the strongest ice imaginable.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                if (PracticeServer.t6) {
                    dpsamt = random.nextInt(5) + 18;
                    hp = random.nextInt(12500, 13500);
                    nrg = random.nextInt(4) + 6;
                    is.setType(Material.LEATHER_LEGGINGS);
                } else {
                    dpsamt = random.nextInt(4) + 18;
                    hp = random.nextInt(1501) + 6000;
                    nrg = random.nextInt(2) + 5;
                    is.setType(Material.GOLD_LEGGINGS);
                }
                rarityId = 2;
            } else if (item == 8) {
                name = "Celeritas, Perpetuous Frost";
                llore = llore + "Icy cold boots leaving a trail of frost with every step.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                if (PracticeServer.t6) {
                    dpsamt = random.nextInt(4) + 12;
                    hp = random.nextInt(6000, 6200);
                    nrg = random.nextInt(3) + 6;
                    is.setType(Material.LEATHER_BOOTS);
                } else {
                    dpsamt = random.nextInt(3) + 11;
                    hp = random.nextInt(751) + 3000;
                    nrg = random.nextInt(2) + 5;
                    is.setType(Material.GOLD_BOOTS);
                }
                rarityId = 2;
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
            if (PracticeServer.t6) {
                intamt = random.nextInt(101) + 500;
                stramt = random.nextInt(101) + 400;
                blockamt = random.nextInt(4) + 10;
                pureamt = random.nextInt(21) + 30;
                elemamt = random.nextInt(41) + 90;
                lifeamt = random.nextInt(5) + 3;
                mindmg = random.nextInt(670, 700);
                maxdmg = random.nextInt(750, 820);
            } else {
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
                if (PracticeServer.t6) {
                    is.setType(Material.DIAMOND_SPADE);
                } else {
                    is.setType(Material.GOLD_SPADE);
                }
                llore = llore + "A Frozen Hammer Forged In The Ice Lakes Of The Crystal Summit.";
                rarityId = 2;
            } else if (item == 5) {
                name = "Frozen Crown of The Exiled King";
                llore = llore + "A blood stained crown made of crystalline ice.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                if (PracticeServer.t6) {
                    dpsamt = random.nextInt(3) + 12;
                    hp = random.nextInt(4200, 5000);
                    hps = random.nextInt(200) + 250;
                    is.setType(Material.LEATHER_HELMET);
                } else {
                    dpsamt = random.nextInt(3) + 12;
                    hp = random.nextInt(751) + 2250;
                    hps = random.nextInt(100) + 150;
                    is.setType(Material.GOLD_HELMET);
                }
                rarityId = 2;
            } else if (item == 6) {
                name = "Frozen Platemail of The Exiled King";
                llore = llore + "A large platemail made of pure crystalline ice.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                if (PracticeServer.t6) {
                    dpsamt = random.nextInt(5) + 18;
                    hp = random.nextInt(9000, 10000);
                    hps = random.nextInt(200) + 250;
                    is.setType(Material.LEATHER_CHESTPLATE);
                } else {
                    dpsamt = random.nextInt(5) + 20;
                    hp = random.nextInt(1501) + 4500;
                    hps = random.nextInt(100) + 150;
                    is.setType(Material.GOLD_CHESTPLATE);
                }
                rarityId = 2;
            } else if (item == 7) {
                name = "Frozen Greaves Of The Exiled King";
                llore = llore + "Shattered greaves made of pure ice.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                if (PracticeServer.t6) {
                    dpsamt = random.nextInt(5) + 18;
                    hp = random.nextInt(9000, 10000);
                    hps = random.nextInt(200) + 250;
                    is.setType(Material.LEATHER_LEGGINGS);
                } else {
                    dpsamt = random.nextInt(5) + 20;
                    hp = random.nextInt(1501) + 4500;
                    hps = random.nextInt(100) + 150;
                    is.setType(Material.GOLD_LEGGINGS);
                }
                rarityId = 2;
            } else if (item == 8) {
                name = "Frozen Snow Boots Of The Exiled King";
                llore = llore + "King Frost's presh frozen Yeezys.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                if (PracticeServer.t6) {
                    dpsamt = random.nextInt(3) + 12;
                    hp = random.nextInt(4200, 5000);
                    hps = random.nextInt(200) + 250;
                    is.setType(Material.LEATHER_BOOTS);
                } else {
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
            if (PracticeServer.t6) {
                blockamt = random.nextInt(6) + 6;
                intamt = random.nextInt(100) + 350;
                stramt = random.nextInt(100) + 350;
                critamt = random.nextInt(6) + 9;
                elemamt = random.nextInt(20) + 70;
                mindmg = random.nextInt(600, 650);
                maxdmg = random.nextInt(750, 850);
            } else {
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
                if (PracticeServer.t6) {
                    is.setType(Material.DIAMOND_AXE);
                } else {
                    is.setType(Material.GOLD_AXE);
                }
                llore = llore + "A large blade made of solid ice.";
                rarityId = 2;
            } else if (item == 5) {
                llore = llore + "A frozen crown forged by The Conquerer himself.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                name = "The Conquerer's Icy Crown";
                if (PracticeServer.t6) {
                    dpsamt = random.nextInt(4) + 15;
                    hp = random.nextInt(5000, 5900);
                    nrg = random.nextInt(3) + 6;
                    is.setType(Material.LEATHER_HELMET);
                } else {
                    dpsamt = random.nextInt(3) + 11;
                    hp = random.nextInt(751) + 2500;
                    nrg = random.nextInt(2) + 4;
                    is.setType(Material.GOLD_HELMET);
                }
                rarityId = 2;
            } else if (item == 6) {
                llore = llore + "A platemail made of solid ice.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                name = "Breastplate of The Conquerer";
                if (PracticeServer.t6) {
                    dpsamt = random.nextInt(5) + 12;
                    hp = random.nextInt(10000, 11500);
                    nrg = random.nextInt(4) + 6;
                    is.setType(Material.LEATHER_CHESTPLATE);
                } else {
                    dpsamt = random.nextInt(3) + 18;
                    hp = random.nextInt(1501) + 5000;
                    nrg = random.nextInt(2) + 5;
                    is.setType(Material.GOLD_CHESTPLATE);
                }
                rarityId = 2;
            } else if (item == 7) {
                llore = llore + "A pair of leggings carved from ice.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                name = "Frosted Platelegs of The Conquerer";
                if (PracticeServer.t6) {
                    dpsamt = random.nextInt(5) + 17;
                    hp = random.nextInt(10000, 11500);
                    nrg = random.nextInt(4) + 6;
                    is.setType(Material.LEATHER_LEGGINGS);
                } else {
                    dpsamt = random.nextInt(3) + 18;
                    hp = random.nextInt(1501) + 5000;
                    nrg = random.nextInt(2) + 5;
                    is.setType(Material.GOLD_LEGGINGS);
                }
                rarityId = 2;
            } else if (item == 8) {
                llore = llore + "A pair of spiked boots worn by The Conquerer.";
                rarity = ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare";
                name = "Spiked Boots of The Conquerer";
                if (PracticeServer.t6) {
                    dpsamt = random.nextInt(4) + 12;
                    hp = random.nextInt(5000, 5900);
                    nrg = random.nextInt(3) + 6;
                    is.setType(Material.LEATHER_BOOTS);
                } else {
                    dpsamt = random.nextInt(3) + 11;
                    hp = random.nextInt(751) + 2500;
                    nrg = random.nextInt(2) + 4;
                    is.setType(Material.GOLD_BOOTS);
                }
                rarityId = 2;
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
            if (is.getItemMeta() != null) {
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
            if (nbtAccessor == null) return is;
            if (mobname.equalsIgnoreCase("spectralKnight")) {
                nbtAccessor.setInt("fixedgear", 1);
            }
            nbtAccessor.setString("rarityType", color.name());
            nbtAccessor.setDouble("namedElite", 1D);
            ItemStack update = nbtAccessor.update();
            if (PracticeServer.GLOWING_NAMED_ELITE_DROP) {
                update.addUnsafeEnchantment(Enchants.glow, 1);
            }
            if (PracticeServer.RANDOM_DURA_NAMED_ELITE_DROP) {
                update.setDurability((short) random(new Random(), 0, update.getType().getMaxDurability()));
            }
            return update;
        } catch (NullPointerException e) {
            return is;
        }
    }

    public static int random(Random random, int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

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
                player.sendMessage(ChatColor.DARK_RED + "The Infernal Abyss: My deathlord.. " + player.getName() + ".. I'm coming for you.");
            });
        }
    }
}