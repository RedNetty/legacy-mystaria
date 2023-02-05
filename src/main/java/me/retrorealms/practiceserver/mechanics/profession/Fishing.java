package me.retrorealms.practiceserver.mechanics.profession;

import com.google.common.collect.Maps;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.ItemBuilder;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.money.Money;
import me.retrorealms.practiceserver.mechanics.player.Speedfish;
import me.retrorealms.practiceserver.utils.StringUtil;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Fishing {

    /**
     * @param tier
     * @return
     */
    public static int getTierLvl(int tier) {
        switch (tier) {
            case 1:
                return 1;
            case 2:
                return 20;
            case 3:
                return 40;
            case 4:
                return 60;
            case 5:
                return 80;
            case 6:
                return 100;
        }
        return 1;
    }

    public static int getNextLevelUp(int tier) {
        if (tier == 1) {
            return 20;
        }
        if (tier == 2) {
            return 40;
        }
        if (tier == 3) {
            return 60;
        }
        if (tier == 4) {
            return 80;
        }
        if (tier == 5) {
            return 100;
        }
        if (tier == 6) {
            return 120;
        }
        return -1;
    }

    public static int getLvl(ItemStack i) {
        return CraftItemStack.asNMSCopy(i).getTag().getInt("level");
    }

    public static int getFishTier(ItemStack fish) {
        return CraftItemStack.asNMSCopy(fish).getTag().getInt("itemTier");
    }

    public static boolean hasEnchants(ItemStack is) {
        ItemMeta meta = is.getItemMeta();
        List<String> lore = meta.getLore();
        for (String line : lore) {
            for (FishingRodEnchant enchants : FishingRodEnchant.values()) {
                if (line.contains(enchants.name))
                    return true;
            }
        }
        return false;
    }

    public enum EnumFish {
        Shrimp("A raw and pink crustacean", 1),
        Anchovie("A small blue, oily fish", 1),
        Crayfish("A lobster-like and brown crustacean", 1),
        Carp("A Large, silver-scaled fish", 2),
        Herring("A colourful and medium-sized fish", 2),
        Sardine("A small and oily green fish", 2),
        Salmon("A beautiful jumping fish", 3),
        Trout("A non-migrating Salmon", 3),
        Cod("A cold-water, deep sea fish", 3),
        Lobster("A Large, red crustacean", 4),
        Tuna("A large, sapphire blue fish", 4),
        Bass("A very large and white fish", 4),
        Shark("A terrifying and massive predator", 5),
        Swordfish("An elongated fish with a long bill", 5),
        Monkfish("A flat, large, and scary-looking fish", 5),
        Whale("A huge, meaty, mammal", 6),
        Orca("A large, black and white predator", 6),
        Narwhal("Narwhals Narwhals swimming in the ocean...", 6);


        public int tier;
        public String desc;

        EnumFish(String desc, int tier) {
            this.desc = desc;
            this.tier = tier;
        }

        public static EnumFish getFish(int tier) {
            List<EnumFish> fishList = getTieredFishList(tier);
            return fishList.get(random.nextInt(fishList.size() - 1));
        }

        public ItemStack buildFish(EnumFish fish) {
            ItemStack stack = null;
            switch (fish.tier) {
                case 1:
                    stack = new ItemStack(Material.RAW_FISH, 1, (short) 0);
                    break;
                case 2:
                    stack = new ItemStack(Material.RAW_FISH, 1, (short) 2);
                    break;
                case 3:
                    stack = new ItemStack(Material.RAW_FISH, 1, (short) 1);
                    break;
                case 4:
                    stack = new ItemStack(Material.RAW_FISH, 1, (short) 3);
                    break;
                case 5:
                    stack = new ItemStack(Material.COOKED_FISH, 1);
                    break;
                case 6:
                    stack = new ItemStack(Material.RAW_FISH, 1, (short) 4);
            }

            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(fish.name());
            List<String> lore = new ArrayList<>();


            return stack;
        }

        private static List<EnumFish> getTieredFishList(int tier) {
            List<EnumFish> fishList = new ArrayList<>();
            for (EnumFish fish : values()) {
                if (fish.tier == tier)
                    fishList.add(fish);
            }
            return fishList;
        }

        public static String getFishDesc(String fish_name) {
            for (EnumFish fish : values()) {
                if (fish.name().equalsIgnoreCase(fish_name))
                    return fish.desc;
            }
            return "A freshly caught fish.";
        }
    }

    private static Random random = new Random();


    public static ItemStack getFishDrop(int tier, Player player) {
        int normal = new Random().nextInt(100);

        if (normal <= 70) {

            int type = new Random().nextInt(100);

            if (inMonkRegion(player)) {
                type -= ThreadLocalRandom.current().nextInt(10, 40);
            }

            if (type >= 50) {
                ItemStack itemStack = new ItemStack(Material.RAW_FISH, 1, (short) 2);
                ItemMeta itemMeta = itemStack.getItemMeta();

                itemMeta.setDisplayName(ChatColor.GRAY + "Andalucian Tigerfish");
                itemMeta.setLore(Arrays.asList("", ChatColor.YELLOW.toString() + "Common Type", ChatColor.GRAY.toString() + ChatColor.ITALIC + "Often caught in the waters of Andalucia."));

                itemStack.setItemMeta(itemMeta);
                return itemStack;
            } else if (type < 50 && type > 12) {
                ItemStack itemStack = new ItemStack(Material.RAW_FISH);
                ItemMeta itemMeta = itemStack.getItemMeta();

                itemMeta.setDisplayName(ChatColor.GRAY + "Andalucian Scarp");
                itemMeta.setLore(Arrays.asList("", ChatColor.YELLOW.toString() + "Common Type", ChatColor.GRAY.toString() + ChatColor.ITALIC + "Often caught in the waters of Andalucia."));

                itemStack.setItemMeta(itemMeta);
                return itemStack;
            } else if (type <= 12) {
                ItemStack itemStack = new ItemStack(Material.RAW_FISH);
                ItemMeta itemMeta = itemStack.getItemMeta();

                itemMeta.setDisplayName(ChatColor.YELLOW + "Andalucian Great Monkfish");
                itemMeta.setLore(Arrays.asList("", ChatColor.YELLOW.toString() + "Rare Type", ChatColor.GRAY.toString() + ChatColor.ITALIC + "Found roaming the depths of Andalucia's waters."));

                itemStack.setItemMeta(itemMeta);

                int origin = tier * 45;

                int gemAmount = ThreadLocalRandom.current().nextInt(origin, 750);

                ItemStack gemsStack = Money.makeGems(1);

                itemStack.setAmount(gemAmount);

                player.getWorld().dropItem(player.getLocation().clone().add(0, 1, 0), gemsStack);
                player.getWorld().dropItem(player.getLocation().clone().add(0, 1, 0), gemsStack);
                player.getWorld().dropItem(player.getLocation().clone().add(0, 1, 0), gemsStack);

                player.sendMessage("");
                StringUtil.sendCenteredMessage(player, "&7[  &eYOU'VE CAUGHT A &nMONKFISH&7  ]");

                ItemStack loot = null;

                int item = new Random().nextInt(75);

                if (item <= 25) {
                    loot = Items.orb(false);

                    if (tier == 4)
                        loot.setAmount(ThreadLocalRandom.current().nextInt(1, 2));
                    else loot.setAmount(ThreadLocalRandom.current().nextInt(1, 3));

                    player.getInventory().addItem(loot);
                }

                if (item > 25 && item <= 50) {
                    loot = Items.enchant(tier, new Random().nextInt(1), false);

                    if (tier == 4)
                        loot.setAmount(ThreadLocalRandom.current().nextInt(1, 2));
                    else loot.setAmount(ThreadLocalRandom.current().nextInt(1, 3));

                    player.getInventory().addItem(loot);
                }

                if (item > 50) {
                    loot = null;
                }

                if (loot != null)
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "      &7[+" + loot.getAmount() + " " + loot.getItemMeta().getDisplayName() + " &7]"));
                player.sendMessage("");

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10F, 0.01F);
                return itemStack;
            }
        }

        return Speedfish.fish(tier, false);
    }


    public static int getEXPNeeded(int level) {
        if (level == 1) {
            return 176; // formula doens't work on level 1.
        }
        if (level == 120) {
            return 0;
        }
        int previous_level = level - 1;
        return (int) (Math.pow((previous_level), 2) + ((previous_level) * 20) + 150 + ((previous_level) * 4) + getEXPNeeded((previous_level)));
    }

    public static int getFishEXP(int tier) {
        if (tier == 1) {
            return (int) (2.0D * (500 + random.nextInt((int) (75 * 0.3D))));
        }
        if (tier == 2) {
            return (int) (2.0D * (700 + random.nextInt((int) (75 * 0.3D))));
        }
        if (tier == 3) {
            return (int) (2.0D * (800 + random.nextInt((int) (75 * 0.3D))));
        }
        if (tier == 4) {
            return (int) (2.0D * (900 + random.nextInt((int) (75 * 0.3D))));
        }
        if (tier == 5) {
            return (int) (2.0D * (1000 + random.nextInt((int) (75 * 0.3D))));
        }
        if (tier == 6) {
            return (int) (2.0D * (1100 + random.nextInt((int) (75 * 0.3D))));
        }
        return 1;
    }

    /**
     * Check if itemstack is a DR fishing pole.
     *
     * @param stack
     * @return boolean
     * @since 1.0
     */
    public static boolean isDRFishingPole(ItemStack stack) {
        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        return nms != null && nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("rod") && stack.getType() == Material.FISHING_ROD;
    }

//    public static HashMap<UUID, String> fishBuffs = new HashMap<>();

    /**
     * Add Experience to the specified stack(fishing pole)
     *
     * @param stack
     */
    public static void gainExp(ItemStack stack, Player p, int exp) {
        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        int currentXP = nms.getTag().getInt("XP");
        int maxXP = nms.getTag().getInt("maxXP");
        int tier = nms.getTag().getInt("itemTier");
        int professionBuffBonus = 0;

        int newEXP = currentXP + exp;

        if (newEXP >= maxXP) {
            lvlUp(tier, p);
            return;
        } else
            nms.getTag().setInt("XP", newEXP);
        stack = CraftItemStack.asBukkitCopy(nms);
        p.getEquipment().setItemInMainHand(stack);
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = stack.getItemMeta().getLore();
        String expBar = "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
        double percentDone = 100.0 * newEXP / maxXP;
        double percentDoneDisplay = (percentDone / 100) * 50.0D;
        int display = (int) percentDoneDisplay;
        if (display <= 0) {
            display = 1;
        }
        if (display > 50) {
            display = 50;
        }
        String newexpBar = ChatColor.GREEN.toString() + expBar.substring(0, display) + ChatColor.RED.toString()
                + expBar.substring(display, expBar.length());
        int lvl = CraftItemStack.asNMSCopy(stack).getTag().getInt("level");
        lore.set(0, ChatColor.GRAY.toString() + "Level: " + ChatColor.GREEN + lvl);
        lore.set(1, ChatColor.GRAY.toString() + newEXP + ChatColor.GRAY + " / " + ChatColor.GRAY + maxXP);
        lore.set(2, ChatColor.GRAY + "EXP: " + newexpBar);

        meta.setLore(lore);
        if (!meta.hasEnchant(Enchantment.LURE))
            meta.addEnchant(Enchantment.LURE, 3, false);

        stack.setItemMeta(meta);
        p.getEquipment().setItemInMainHand(stack);
    }


    public static int getTreasureFindChance(ItemStack is) {
        int chance = 0;

        if (!(isDRFishingPole(is))) {
            return chance;
        }

        for (String s : is.getItemMeta().getLore()) {
            if (s.contains("TREASURE FIND")) {
                chance = Integer.parseInt(s.substring(s.lastIndexOf(" ") + 1, s.lastIndexOf("%")));
                return chance;
            }
        }

        return chance;
    }

    public static void checkMonkRegion() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PracticeServer.getInstance(), () -> {

            Bukkit.getOnlinePlayers().forEach(player -> {
                if (inMonkRegion(player)) {

                    if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) return;

                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 150, 1));
                }
            });

        }, 0L, 10);
    }

    public static int getJunkFindChance(ItemStack is) {
        int chance = 0;

        if (!(isDRFishingPole(is))) {
            return chance;
        }

        for (String s : is.getItemMeta().getLore()) {
            if (s.contains("JUNK FIND")) {
                chance = Integer.parseInt(s.substring(s.lastIndexOf(" ") + 1, s.lastIndexOf("%")));
                return chance;
            }
        }

        return chance;
    }

    public static int getDoubleDropChance(ItemStack is) {
        int chance = 0;

        if (!(isDRFishingPole(is))) {
            return chance;
        }

        for (String s : is.getItemMeta().getLore()) {
            if (s.contains("DOUBLE")) {
                chance = Integer.parseInt(s.substring(s.lastIndexOf(" ") + 1, s.lastIndexOf("%")));
                return chance;
            }
        }

        return chance;
    }

    public static int getTripleDropChance(ItemStack is) {
        int chance = 0;

        if (!(isDRFishingPole(is))) {
            return chance;
        }

        for (String s : is.getItemMeta().getLore()) {
            if (s.contains("TRIPLE")) {
                chance = Integer.parseInt(s.substring(s.lastIndexOf(" ") + 1, s.lastIndexOf("%")));
                return chance;
            }
        }

        return chance;
    }

    public static int getSuccessChance(ItemStack is) {
        int chance = 0;

        if (!(isDRFishingPole(is))) {
            return chance;
        }

        for (String s : is.getItemMeta().getLore()) {
            if (s.contains("SUCCESS")) {
                chance = Integer.parseInt(s.substring(s.lastIndexOf("+") + 1, s.lastIndexOf("%")));
                return chance;
            }
        }

        return chance;
    }

    public static int getDurabilityBuff(ItemStack is) {
        int buff = 0;

        if (!(isDRFishingPole(is))) {
            return buff;
        }

        for (String s : is.getItemMeta().getLore()) {
            if (s.contains("DURABILITY")) {
                buff = Integer.parseInt(s.substring(s.lastIndexOf("+") + 1, s.lastIndexOf("%")));
                return buff;
            }
        }

        return buff;
    }


    public static void giveRandomStatBuff(ItemStack stack, int tier) {
        int typeID = new Random().nextInt(6);
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore();
        FishingRodEnchant enchant = null;
        main:
        switch (tier) {
            case 0:
            case 1:
            case 2:
            case 3:
                switch (typeID) {
                    case 0:
                    case 1:
                        enchant = FishingRodEnchant.DoubleCatch;
                        break main;
                    case 2:
                        enchant = FishingRodEnchant.CatchingSuccess;
                        break main;
                    case 3:
                        enchant = FishingRodEnchant.TripleCatch;
                        break main;
                    case 4:
                        enchant = FishingRodEnchant.Durability;
                        break main;
                    case 5:
                        enchant = FishingRodEnchant.JunkFind;
                        break main;
                }
            case 4:
            case 5:
            case 6:
                switch (typeID) {
                    case 0:
                        enchant = FishingRodEnchant.DoubleCatch;
                        break main;
                    case 1:
                        enchant = FishingRodEnchant.TreasureFind;
                        break main;
                    case 2:
                        enchant = FishingRodEnchant.CatchingSuccess;
                        break main;
                    case 3:
                        enchant = FishingRodEnchant.TripleCatch;
                        break main;
                    case 4:
                        enchant = FishingRodEnchant.Durability;
                        break main;
                    case 5:
                        enchant = FishingRodEnchant.JunkFind;
                        break main;
                }
        }

        Iterator<String> i = lore.iterator();
        int prevValue = -1;

        while (i.hasNext()) {
            String line = i.next();
            if (line.contains(enchant.name)) {
                prevValue = Integer.valueOf(line.substring(line.indexOf("+"), line.indexOf("%")));
                i.remove();
            }
        }


        String clone = lore.get(lore.size() - 1);
        int value = enchant.getBuff(tier);
        if (value == 0)
            value = 1;
        if (prevValue != -1 && prevValue > value)
            value = prevValue;
        lore.remove(lore.size() - 1);
        lore.add(ChatColor.RED + enchant.name + " +" + value + "%");
        lore.add(clone);
        meta.setLore(lore);
        stack.setItemMeta(meta);

    }

    private static void lvlUp(int tier, Player p) {
        PlayerDeathEvent deathEvent;
        ItemStack rod = p.getEquipment().getItemInMainHand();
        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(rod);
        int lvl;

        if (nms.getTag().hasKey("level")) {
            lvl = nms.getTag().getInt("level") + 1;
        } else lvl = 1;

        boolean addEnchant = false;
        if (lvl < 101) {
            switch (lvl) {
                case 20:
                    tier = 2;
                    addEnchant = true;
                    break;
                case 40:
                    tier = 3;
                    addEnchant = true;
                    break;
                case 60:
                    tier = 4;
                    addEnchant = true;
                    break;
                case 80:
                    tier = 5;
                    addEnchant = true;
                    break;
                case 100:
                    tier = 6;
                    addEnchant = true;
                    break;
                case 120:
                    addEnchant = true;
                    rod.getItemMeta().setDisplayName(ChatColor.YELLOW.toString() + "Grand Master Fishing Rod");
                    p.sendMessage(ChatColor.YELLOW + "Congratulations! Your Fishing Rod has reached " + ChatColor.UNDERLINE + "LVL 100"
                            + ChatColor.YELLOW + " this means you can no longer repair it. You now have TWO options.");
                    p.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "(1) " + ChatColor.YELLOW + "You can exchange the Fishing Rod at the merchant for a 'Buff Token' that will hold all the custom stats of your Fishingrod and may be applied to a new Fishingrod.");
                    p.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "(2) " + ChatColor.YELLOW + "If you continue to use this" +
                            " Fishingrod until it runs out of durability, it will transform into a LVL 1 Fishing Rod "
                            + ", but it will retain all its custom stats.");
                    p.sendMessage("");
                    break;
                default:
                    break;
            }
            nms = CraftItemStack.asNMSCopy(rod);
            p.sendMessage(ChatColor.YELLOW + "Your Fishing Rod has increased to level " + ChatColor.AQUA + lvl);

            NBTTagCompound nbtTagCompound = nms.getTag();
            nbtTagCompound.setInt("maxXP", getEXPNeeded(lvl));
            nbtTagCompound.setInt("XP", 0);
            nbtTagCompound.setInt("level", lvl);
            nbtTagCompound.setInt("itemTier", tier);

            nms.setTag(nbtTagCompound);

            System.out.println(nbtTagCompound.getInt("level"));

            rod = CraftItemStack.asBukkitCopy(nms);
            ItemMeta meta = rod.getItemMeta();
            List<String> lore = meta.getLore();
            String expBar = ChatColor.RED + "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
            lore.set(0, ChatColor.GRAY.toString() + "Level: " + ChatColor.GREEN + lvl);
            lore.set(1, ChatColor.GRAY.toString() + 0 + ChatColor.GRAY.toString() + " / " + ChatColor.GRAY + Mining.getEXPNeeded(lvl));
            lore.set(2, ChatColor.GRAY.toString() + "EXP: " + expBar);
            String name = "Novice Fishingrod";

            switch (tier) {
                case 1:
                    name = ChatColor.WHITE + "Basic Fishingrod";
                    lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of wood and thread.");
                    break;
                case 2:
                    name = ChatColor.GREEN.toString() + "Advanced Fishingrod";
                    lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of oak wood and thread.");
                    break;
                case 3:
                    name = ChatColor.AQUA.toString() + "Expert Fishingrod";
                    lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of ancient oak wood and spider silk.");
                    break;
                case 4:
                    name = ChatColor.LIGHT_PURPLE.toString() + "Supreme Fishingrod";
                    lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of jungle bamboo and spider silk.");
                    break;
                case 5:
                    name = ChatColor.YELLOW.toString() + "Master Fishingrod";
                    lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of rich mahogany and enchanted silk");
                    break;
                case 6:
                    name = ChatColor.BLUE.toString() + "Grand Master Fishingrod";
                    lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of frozen birch and enchanted silk");
                    break;
                default:
                    break;
            }
            meta.setDisplayName(name);
            meta.setLore(lore);
            if (!meta.hasEnchant(Enchantment.LURE))
                meta.addEnchant(Enchantment.LURE, 3, false);
            rod.setItemMeta(meta);
            if (addEnchant)
                giveRandomStatBuff(rod, tier);

            p.getEquipment().setItemInMainHand(rod);
        }
    }


    /**
     * Get the tier of said Rod.
     *
     * @param rodStack
     * @return Integer
     * @since 1.0
     */
    public static int getRodTier(ItemStack rodStack) {
        return CraftItemStack.asNMSCopy(rodStack).getTag().getInt("itemTier");
    }


    public enum FishingRodEnchant {
        DoubleCatch("DOUBLE CATCH"),
        TripleCatch("TRIPLE CATCH"),
        TreasureFind("TREASURE FIND"),
        Durability("DURABILITY"),
        CatchingSuccess("FISHING SUCCESS"),
        JunkFind("JUNK FIND");


        public String name;

        FishingRodEnchant(String display) {
            this.name = display;
        }

        public int getBuff(int tier) {
            Random rand = new Random();
            switch (this) {
                case DoubleCatch:
                    switch (tier) {
                        case 0:
                        case 1:
                        case 2:
                            return rand.nextInt(5) + 1;
                        case 3:
                            return rand.nextInt(9) + 1;
                        case 4:
                            return rand.nextInt(13) + 1;
                        case 5:
                            return rand.nextInt(24) + 1;
                        case 6:
                            return rand.nextInt(28) + 1;
                    }
                case TripleCatch:
                    switch (tier) {
                        case 0:
                        case 1:
                        case 2:
                            return rand.nextInt(2) + 1;
                        case 3:
                            return rand.nextInt(3) + 1;
                        case 4:
                            return rand.nextInt(4) + 1;
                        case 5:
                            return rand.nextInt(5) + 1;
                        case 6:
                            return rand.nextInt(6) + 1;
                    }
                    break;
                case TreasureFind:
                    switch (tier) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            return 0;
                        case 4:
                        case 5:
                            return 1;
                        case 6:
                            return 2;
                    }
                    break;
                case Durability:
                    switch (tier) {
                        case 0:
                        case 1:
                            return rand.nextInt(5) + 1;
                        case 2:
                            return rand.nextInt(10) + 1;
                        case 3:
                            return rand.nextInt(15) + 1;
                        case 4:
                            return rand.nextInt(20) + 1;
                        case 5:
                            return rand.nextInt(25) + 1;
                        case 6:
                            return rand.nextInt(30) + 1;
                    }
                    break;
                case CatchingSuccess:
                    switch (tier) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            return rand.nextInt(2) + 1;
                        case 5:
                            return rand.nextInt(6) + 1;
                        case 6:
                            return rand.nextInt(8) + 1;

                    }
                    break;
                case JunkFind:
                    switch (tier) {
                        case 0:
                        case 1:
                            return rand.nextInt(11) + 1;
                        case 2:
                            return rand.nextInt(12) + 1;
                        case 3:
                            return rand.nextInt(13) + 1;
                        case 4:
                            return rand.nextInt(14) + 1;
                        case 5:
                            return rand.nextInt(15) + 1;
                        case 6:
                            return rand.nextInt(18) + 1;
                    }
                    break;
            }

            return 1;
        }

        public static FishingRodEnchant getEnchant(String enchantTypeString) {
            for (FishingRodEnchant temp : values()) {
                Bukkit.getLogger().info(temp.name + " || " + enchantTypeString);
                if (temp.name().equalsIgnoreCase(enchantTypeString) || temp.name.contains(enchantTypeString) || temp.name.equalsIgnoreCase(enchantTypeString))
                    return temp;
            }
            return FishingRodEnchant.DoubleCatch;
        }
    }

    public static ItemStack getEnchant(int tier, FishingRodEnchant enchant) {
        int stat = enchant.getBuff(tier);
        String statBuff = ChatColor.RED + enchant.name + " " + stat + "%";
        ItemStack stack = new ItemBuilder().setItem(Material.EMPTY_MAP, (short) 0, ChatColor.WHITE + ChatColor.BOLD.toString() + "Scroll: " + ChatColor.YELLOW + "Fishingrod Enchant", new String[]{statBuff, ChatColor.GRAY + "Imbues a fishingrod with special attributes."}).build();

        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        nms.getTag().setString("type", "fishingenchant");
        nms.getTag().setInt(enchant.name(), stat);
        return CraftItemStack.asBukkitCopy(nms);
    }

    public static ItemStack getEnchant(int tier, FishingRodEnchant enchant, int percent) {
        String statBuff = ChatColor.RED + enchant.name + " " + percent + "%";
        ItemStack stack = new ItemBuilder().setItem(Material.EMPTY_MAP, (short) 0, ChatColor.WHITE + ChatColor.BOLD.toString() + "Scroll: " + ChatColor.YELLOW + "Fishingrod Enchant", new String[]{statBuff, ChatColor.GRAY + "Imbues a fishingrod with special attributes."}).build();
        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        nms.getTag().setString("type", "fishingenchant");
        nms.getTag().setInt(enchant.name(), percent);
        return CraftItemStack.asBukkitCopy(nms);

    }


    public HashMap<Location, Integer> FISHING_LOCATIONS = new HashMap<>();
    public HashMap<Location, List<Location>> FISHING_PARTICLES = new HashMap<>();

    public void generateFishingParticleBlockList() {

    }

    public HashMap<String, Integer> fishingRegions = Maps.newHashMap();

    public boolean inFishingRegion(Player player) {
        final Location location = player.getLocation();

        return isFishingRegion(location);
    }

    public boolean isFishingRegion(Location location) {
        ApplicableRegionSet regionSet = WGBukkit.getRegionManager(location.getWorld()).getApplicableRegions(location);

        for (ProtectedRegion region : regionSet)
            if (this.fishingRegions.containsKey(region.getId().toLowerCase()) || region.getId().equalsIgnoreCase("monkfishingregion"))
                return true;

        return false;
    }

    public static boolean inMonkRegion(Player player) {
        ApplicableRegionSet regionSet = WGBukkit.getRegionManager(player.getLocation().getWorld()).getApplicableRegions(player.getLocation());

        for (ProtectedRegion region : regionSet)
            if (region.getId().equalsIgnoreCase("monkfishingregion")) return true;

        return false;
    }

    public int getSpotTier(Location location) {
        if (!this.isFishingRegion(location)) return -1;

        ApplicableRegionSet regionSet = WGBukkit.getRegionManager(location.getWorld()).getApplicableRegions(location);

        for (ProtectedRegion region : regionSet)

            if (region.getId().equalsIgnoreCase("monkfishingregion")) return 5;

            else if (this.fishingRegions.containsKey(region.getId().toLowerCase()))
                return this.fishingRegions.get(region.getId().toLowerCase());


        return -1;
    }

    public Integer getFishingSpotTier(Location loc) {
        return getSpotTier(loc);
    }

    public static boolean isCustomFish(ItemStack is) {
        if (is != null && is.getType() == Material.COOKED_FISH && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().hasLore()) {
            return true;
        }
        return false;
    }

    public static boolean isCustomRawFish(ItemStack is) {
        if (is != null && is.getType() == Material.RAW_FISH && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().hasLore()) {
            return true;
        }
        return false;
    }

    public static void restoreFood(Player p, ItemStack fish) {
        List<String> lore = fish.getItemMeta().getLore();
        int food_to_restore = 0;

        for (String s : lore) {
            if (s.contains("% HUNGER")) {
                double percent = Integer.parseInt(s.substring(s.indexOf("-") + 1, s.indexOf("%")));
                int local_amount = (int) ((percent / 100.0D) * 20D);
                food_to_restore += local_amount;
            }
        }

        int cur_food = p.getFoodLevel();
        if (cur_food + food_to_restore >= 20) {
            p.setFoodLevel(20);
            p.setSaturation(20);
        } else {
            p.setFoodLevel(cur_food + food_to_restore);
            p.setSaturation(p.getSaturation() + food_to_restore);
        }
    }


    public void loadFishingLocations() {
        List<String> Spots = Arrays.asList("fishingspot1", "fishingspot2", "fishingspot3", "fishingspot4", "fishingspot5", "fishingspot6", "fishingspot7", "fishingspot8", "fishingspot9", "fishingspot10");

        Spots.forEach(spot -> {
            this.fishingRegions.put(spot, 4);
            this.fishingRegions.put(spot, 5);
            this.fishingRegions.put(spot, 6);
        });
    }

    private static Fishing instance;

    public static Fishing getInstance() {
        if (instance == null)
            instance = new Fishing();
        return instance;

    }
}
