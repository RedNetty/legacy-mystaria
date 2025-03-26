package me.retrorealms.practiceserver.mechanics.enchants;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import me.retrorealms.practiceserver.apis.itemapi.NBTAccessor;
import me.retrorealms.practiceserver.mechanics.damage.Damage;
import me.retrorealms.practiceserver.mechanics.donations.StatTrak.WepTrak;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayer;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayers;
import me.retrorealms.practiceserver.mechanics.vendors.ItemVendors;
import me.retrorealms.practiceserver.utils.Particles;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the application of Orbs of Alteration to modify item stats
 * and properties in the RetroRealms RPG system.
 */
public class Orbs implements Listener {

    private static final Logger LOGGER = Logger.getLogger(Orbs.class.getName());

    // Orb display names
    private static final String NORMAL_ORB_NAME = ChatColor.LIGHT_PURPLE + "Orb of Alteration";
    private static final String LEGENDARY_ORB_NAME = ChatColor.YELLOW + "Legendary Orb of Alteration";

    // Item type constants
    private static final int TYPE_STAFF = 0;
    private static final int TYPE_SPEAR = 1;
    private static final int TYPE_SWORD = 2;
    private static final int TYPE_AXE = 3;
    private static final int TYPE_HELMET = 4;
    private static final int TYPE_CHESTPLATE = 5;
    private static final int TYPE_LEGGINGS = 6;
    private static final int TYPE_BOOTS = 7;
    private static final int TYPE_UNKNOWN = -1;

    // Element type constants
    private static final int ELEM_FIRE = 1;
    private static final int ELEM_POISON = 2;
    private static final int ELEM_ICE = 3;

    // Minimum plus value for legendary orbs
    private static final int LEGENDARY_MINIMUM_PLUS = 4;

    // Set of currently processing transactions to prevent duplication
    private final Set<UUID> processingPlayers = new HashSet<>();

    /**
     * Determines the tier of an item based on its material type.
     *
     * @param itemStack The item to check
     * @return The tier of the item (1-6) or 0 if not a valid tier item
     */
    public static int getItemTier(final ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return 0;
        }

        String materialName = itemStack.getType().name();
        boolean isBlueLeather = Items.isBlueLeather(itemStack);

        if (materialName.contains("WOOD_") || (materialName.contains("LEATHER_") && !isBlueLeather)) {
            return 1;
        }
        if (materialName.contains("STONE_") || materialName.contains("CHAINMAIL_")) {
            return 2;
        }
        if (materialName.contains("IRON_")) {
            return 3;
        }
        if (materialName.contains("DIAMOND_") && !itemStack.getItemMeta().getDisplayName().contains(ChatColor.BLUE.toString())) {
            return 4;
        }
        if (materialName.contains("GOLD_")) {
            return 5;
        }
        if (materialName.contains("DIAMOND_") || materialName.contains("LEATHER_")) {
            return 6;
        }
        return 0;
    }

    /**
     * Determines the equipment type of an item based on its material.
     *
     * @param itemStack The item to check
     * @return The type of the item (0-7) or -1 if not a valid type
     */
    public static int getItemType(final ItemStack itemStack) {
        if (itemStack == null) {
            return TYPE_UNKNOWN;
        }

        String materialName = itemStack.getType().name();

        if (materialName.contains("_HOE")) {
            return TYPE_STAFF;
        }
        if (materialName.contains("_SPADE")) {
            return TYPE_SPEAR;
        }
        if (materialName.contains("_SWORD")) {
            return TYPE_SWORD;
        }
        if (materialName.contains("_AXE")) {
            return TYPE_AXE;
        }
        if (materialName.contains("_HELMET")) {
            return TYPE_HELMET;
        }
        if (materialName.contains("_CHESTPLATE")) {
            return TYPE_CHESTPLATE;
        }
        if (materialName.contains("_LEGGINGS")) {
            return TYPE_LEGGINGS;
        }
        if (materialName.contains("_BOOTS")) {
            return TYPE_BOOTS;
        }
        return TYPE_UNKNOWN;
    }

    /**
     * Checks if an item has special lore lines.
     *
     * @param itemStack The item to check
     * @return True if the item has special lore lines, false otherwise
     */
    public static boolean hasLoreLine(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta() || !itemStack.getItemMeta().hasLore()) {
            return false;
        }

        List<String> lore = itemStack.getItemMeta().getLore();
        for (String line : lore) {
            if (line.contains(ChatColor.GRAY.toString()) && line.contains(ChatColor.ITALIC.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Apply standard randomized stats to an item using a normal Orb of Alteration.
     *
     * @param itemStack The item to randomize
     * @return The modified item with new stats
     */
    public static ItemStack randomizeStats(final ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta() || !itemStack.getItemMeta().hasLore()) {
            return itemStack;
        }

        try {
            // Clone the itemstack to avoid modifying the original during processing
            ItemStack result = itemStack.clone();
            String oldName = result.getItemMeta().getDisplayName();
            List<String> oldLore = result.getItemMeta().getLore();
            List<String> newLore = new ArrayList<>();
            List<String> rareLore = extractRareLore(result);

            int tier = getItemTier(result);
            int itemType = getItemType(result);

            if (tier == 0 || itemType == TYPE_UNKNOWN) {
                return itemStack; // Invalid item, return original
            }

            // Generate random stat values based on item tier and type
            StatGenerator statGen = new StatGenerator(tier);
            String name = generateItemName(result, tier, itemType, statGen);

            // Add item-specific stats to lore
            if (isWeapon(itemType)) {
                addWeaponStatsToLore(result, newLore, oldLore, statGen);
            } else if (isArmor(itemType)) {
                addArmorStatsToLore(result, newLore, oldLore, statGen);
            }

            // Keep custom name for named elite items
            NBTAccessor nbt = new NBTAccessor(result).check();
            if (oldName != null && nbt.getInteger("namedElite") == 1) {
                name = oldName;
                for (String line : oldLore) {
                    if (line.startsWith(ChatColor.GRAY.toString())) {
                        newLore.add(line);
                    }
                }
            }

            // Add rare lore lines and finalize item
            newLore.addAll(rareLore);

            // Apply proper coloring and plus level to name
            int plus = Enchants.getPlus(result);
            name = applyColorBasedOnTier(name, tier);
            name = applyPlusToName(name, plus, false);

            // Update item metadata
            ItemMeta meta = result.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(newLore);
            result.setItemMeta(meta);

            validateItemName(result, oldName);

            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error randomizing item stats", e);
            return itemStack; // Return original item if anything goes wrong
        }
    }

    /**
     * Apply legendary randomized stats to an item using a Legendary Orb of Alteration.
     *
     * @param itemStack The item to randomize
     * @param player The player using the orb (for bonus rolls)
     * @return The modified item with new stats
     */
    public static ItemStack randomizeLegendaryStats(final ItemStack itemStack, Player player) {
        if (itemStack == null || !itemStack.hasItemMeta() || !itemStack.getItemMeta().hasLore() || player == null) {
            return itemStack;
        }

        try {
            // Clone the itemstack to avoid modifying the original during processing
            ItemStack result = itemStack.clone();
            String oldName = result.getItemMeta().getDisplayName();
            List<String> oldLore = result.getItemMeta().getLore();
            List<String> newLore = new ArrayList<>();
            List<String> rareLore = extractRareLore(result);

            int tier = getItemTier(result);
            int itemType = getItemType(result);

            if (tier == 0 || itemType == TYPE_UNKNOWN) {
                return itemStack; // Invalid item, return original
            }

            // Get player's bonus roll count
            PersistentPlayer pp = PersistentPlayers.get(player.getUniqueId());
            int bonusRolls = (pp != null) ? pp.orbrolls : 0;

            // Generate legendary stats with bonus rolls
            LegendaryStatGenerator statGen = new LegendaryStatGenerator(tier, bonusRolls);
            String name = generateItemName(result, tier, itemType, statGen);

            // Handle weapon enhancement from plus levels
            if (Enchants.getPlus(result) < 4) {
                enhanceItemBasedOnPlus(result, oldLore);
            }

            // Add item-specific stats to lore
            if (isWeapon(itemType)) {
                addWeaponStatsToLore(result, newLore, oldLore, statGen);
            } else if (isArmor(itemType)) {
                addArmorStatsToLore(result, newLore, oldLore, statGen);
            }

            // Keep custom name for named elite items
            NBTAccessor nbt = new NBTAccessor(result).check();
            if (oldName != null && nbt.getInteger("namedElite") == 1) {
                name = oldName;
                for (String line : oldLore) {
                    if (line.startsWith(ChatColor.GRAY.toString())) {
                        newLore.add(line);
                    }
                }
            }

            // Add rare lore lines and finalize item
            newLore.addAll(rareLore);

            // Apply proper coloring and plus level to name
            int plus = Enchants.getPlus(result);
            name = applyColorBasedOnTier(name, tier);
            name = applyPlusToName(name, plus, true);

            // Update item metadata
            ItemMeta meta = result.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(newLore);
            result.setItemMeta(meta);

            validateItemName(result, oldName);

            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error randomizing legendary item stats", e);
            return itemStack; // Return original item if anything goes wrong
        }
    }

    /**
     * Extracts rare lore lines from an item that should be preserved.
     *
     * @param itemStack The item to extract lore from
     * @return A list of rare lore lines
     */
    private static List<String> extractRareLore(ItemStack itemStack) {
        List<String> rareLore = new ArrayList<>();
        List<String> oldLore = itemStack.getItemMeta().getLore();
        int extra = 0;

        if (ItemAPI.isProtected(itemStack)) {
            extra = 2;
        }
        if (hasLoreLine(itemStack)) {
            extra = 1;
        }

        if (WepTrak.isStatTrak(itemStack)) {
            for (int i = oldLore.size() - 7 - extra; i < oldLore.size(); i++) {
                if (i < 0 || i >= oldLore.size()) continue;

                String line = oldLore.get(i);
                if (line.contains("Normal Orbs Used: ")) {
                    try {
                        int current = Integer.parseInt(line.split(": " + ChatColor.AQUA)[1]);
                        rareLore.add(ChatColor.GOLD + "Normal Orbs Used: " + ChatColor.AQUA + (current + 1));
                    } catch (Exception e) {
                        rareLore.add(line);
                    }
                } else {
                    rareLore.add(line);
                }
            }
        } else {
            for (int i = oldLore.size() - 1 - extra; i < oldLore.size(); i++) {
                if (i < 0 || i >= oldLore.size()) continue;

                try {
                    if (oldLore.get(i).contains(ChatColor.GRAY.toString()) &&
                            !oldLore.get(i).toLowerCase().contains("common") &&
                            !oldLore.get(i).toLowerCase().contains("untradeable")) {
                        i--;
                        if (i < 0) continue;
                    }
                } catch (Exception e) {
                    // Skip problematic indices
                    continue;
                }
                rareLore.add(oldLore.get(i));
            }
        }

        return rareLore;
    }

    /**
     * Enhances an item's stats based on its plus level.
     *
     * @param itemStack The item to enhance
     * @param oldLore The item's current lore
     */
    private static void enhanceItemBasedOnPlus(ItemStack itemStack, List<String> oldLore) {
        int itemType = getItemType(itemStack);
        int plus = Enchants.getPlus(itemStack);

        if (isWeapon(itemType)) {
            enhanceWeaponStats(itemStack, oldLore, plus);
        } else if (isArmor(itemType)) {
            enhanceArmorStats(itemStack, oldLore, plus);
        }
    }

    /**
     * Enhances weapon stats based on plus level.
     *
     * @param itemStack The weapon to enhance
     * @param oldLore The weapon's current lore
     * @param plus The weapon's plus level
     */
    private static void enhanceWeaponStats(ItemStack itemStack, List<String> oldLore, int plus) {
        try {
            double beforeMin = Damage.getDamageRange(itemStack).get(0);
            double beforeMax = Damage.getDamageRange(itemStack).get(1);

            double addedMin = 0;
            double addedMax = 0;

            switch (plus) {
                case 0:
                    addedMin = beforeMin * 0.20;
                    addedMax = beforeMax * 0.20;
                    break;
                case 1:
                    addedMin = beforeMin * 0.15;
                    addedMax = beforeMax * 0.15;
                    break;
                case 2:
                    addedMin = beforeMin * 0.10;
                    addedMax = beforeMax * 0.10;
                    break;
                case 3:
                    addedMin = beforeMin * 0.05;
                    addedMax = beforeMax * 0.05;
                    break;
            }

            addedMin = Math.max(1.0, addedMin);
            addedMax = Math.max(1.0, addedMax);

            int newMin = (int)(beforeMin + addedMin);
            int newMax = (int)(beforeMax + addedMax);

            oldLore.set(0, ChatColor.RED + "DMG: " + newMin + " - " + newMax);
            itemStack.addUnsafeEnchantment(Enchants.glow, 1);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error enhancing weapon stats", e);
        }
    }

    /**
     * Enhances armor stats based on plus level.
     *
     * @param itemStack The armor to enhance
     * @param oldLore The armor's current lore
     * @param plus The armor's plus level
     */
    private static void enhanceArmorStats(ItemStack itemStack, List<String> oldLore, int plus) {
        try {
            double beforeHp = Damage.getHp(itemStack);
            double beforeHpGen = Damage.getHps(itemStack);
            int beforeEnergy = Damage.getEnergy(itemStack);

            double addedHp = 0;
            double addedHpGen = 0;
            int addedEnergy = beforeEnergy;

            switch (plus) {
                case 0:
                    addedHp = beforeHp * 0.20;
                    addedHpGen = beforeHpGen * 0.20;
                    addedEnergy += 4;
                    break;
                case 1:
                    addedHp = beforeHp * 0.15;
                    addedHpGen = beforeHpGen * 0.15;
                    addedEnergy += 3;
                    break;
                case 2:
                    addedHp = beforeHp * 0.10;
                    addedHpGen = beforeHpGen * 0.10;
                    addedEnergy += 2;
                    break;
                case 3:
                    addedHp = beforeHp * 0.05;
                    addedHpGen = beforeHpGen * 0.05;
                    addedEnergy += 1;
                    break;
            }

            addedHp = Math.max(1.0, addedHp);
            int newHp = (int)(beforeHp + addedHp);
            oldLore.set(1, ChatColor.RED + "HP: +" + newHp);

            if (oldLore.get(2).contains("ENERGY REGEN")) {
                oldLore.set(2, ChatColor.RED + "ENERGY REGEN: +" + addedEnergy + "%");
            } else if (oldLore.get(2).contains("HP REGEN")) {
                addedHpGen = Math.max(1.0, addedHpGen);
                int newHpGen = (int)(beforeHpGen + addedHpGen);
                oldLore.set(2, ChatColor.RED + "HP REGEN: +" + newHpGen + "/s");
            }

            itemStack.addUnsafeEnchantment(Enchants.glow, 1);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error enhancing armor stats", e);
        }
    }

    /**
     * Adds weapon-specific stats to the lore.
     *
     * @param itemStack The weapon item
     * @param newLore The new lore list to add to
     * @param oldLore The current lore list
     * @param statGen The stat generator to use
     */
    private static void addWeaponStatsToLore(ItemStack itemStack, List<String> newLore, List<String> oldLore, StatGenerator statGen) {
        newLore.add(oldLore.get(0)); // Add DMG line

        int itemType = getItemType(itemStack);

        // Add Pure DMG for axes
        if (itemType == TYPE_AXE && statGen.isPureActive()) {
            newLore.add(ChatColor.RED + "PURE DMG: +" + statGen.getPureAmount());
        }

        // Add Accuracy
        if (statGen.isAccuracyActive()) {
            newLore.add(ChatColor.RED + "ACCURACY: " + statGen.getAccuracyAmount() + "%");
        }

        // Add VS Monsters
        if (statGen.isVsMonstersActive()) {
            newLore.add(ChatColor.RED + "VS MONSTERS: " + statGen.getVsMonstersAmount() + "%");
        }

        // Add VS Players
        if (statGen.isVsPlayersActive()) {
            newLore.add(ChatColor.RED + "VS PLAYERS: " + statGen.getVsPlayersAmount() + "%");
        }

        // Add Life Steal
        if (statGen.isLifeStealActive()) {
            newLore.add(ChatColor.RED + "LIFE STEAL: " + statGen.getLifeStealAmount() + "%");
        }

        // Add Critical Hit
        if (statGen.isCriticalHitActive()) {
            newLore.add(ChatColor.RED + "CRITICAL HIT: " + statGen.getCriticalHitAmount() + "%");
        }

        // Add Elemental Damage
        if (statGen.getElementType() > 0) {
            switch (statGen.getElementType()) {
                case ELEM_FIRE:
                    newLore.add(ChatColor.RED + "FIRE DMG: +" + statGen.getElementAmount());
                    break;
                case ELEM_POISON:
                    newLore.add(ChatColor.RED + "POISON DMG: +" + statGen.getElementAmount());
                    break;
                case ELEM_ICE:
                    newLore.add(ChatColor.RED + "ICE DMG: +" + statGen.getElementAmount());
                    break;
            }
        }
    }

    /**
     * Adds armor-specific stats to the lore.
     *
     * @param itemStack The armor item
     * @param newLore The new lore list to add to
     * @param oldLore The current lore list
     * @param statGen The stat generator to use
     */
    private static void addArmorStatsToLore(ItemStack itemStack, List<String> newLore, List<String> oldLore, StatGenerator statGen) {
        // Add base armor stats
        newLore.add(oldLore.get(0));
        newLore.add(oldLore.get(1));
        newLore.add(oldLore.get(2));

        // Add Intelligence
        if (statGen.isIntelligenceActive()) {
            newLore.add(ChatColor.RED + "INT: +" + statGen.getIntelligenceAmount());
        }

        // Add Strength
        if (statGen.isStrengthActive()) {
            newLore.add(ChatColor.RED + "STR: +" + statGen.getStrengthAmount());
        }

        // Add Vitality
        if (statGen.isVitalityActive()) {
            newLore.add(ChatColor.RED + "VIT: +" + statGen.getVitalityAmount());
        }

        // Add Dexterity
        if (statGen.isDexterityActive()) {
            newLore.add(ChatColor.RED + "DEX: +" + statGen.getDexterityAmount());
        }

        // Add Dodge
        if (statGen.isDodgeActive()) {
            newLore.add(ChatColor.RED + "DODGE: " + statGen.getDodgeAmount() + "%");
        }

        // Add Thorns
        if (statGen.isThornsActive()) {
            newLore.add(ChatColor.RED + "THORNS: " + statGen.getThornsAmount() + "%");
        }

        // Add Block
        if (statGen.isBlockActive()) {
            newLore.add(ChatColor.RED + "BLOCK: " + statGen.getBlockAmount() + "%");
        }
    }

    /**
     * Generates the appropriate item name based on stats and type.
     *
     * @param itemStack The item to generate a name for
     * @param tier The item's tier
     * @param itemType The item's type
     * @param statGen The stat generator used
     * @return The generated item name
     */
    private static String generateItemName(ItemStack itemStack, int tier, int itemType, StatGenerator statGen) {
        String name = getBaseItemName(tier, itemType);

        // Apply prefixes and suffixes based on active stats
        if (isWeapon(itemType)) {
            if (itemType == TYPE_AXE && statGen.isPureActive()) {
                name = "Pure " + name;
            }
            if (statGen.isAccuracyActive()) {
                name = "Accurate " + name;
            }
            if (statGen.isLifeStealActive()) {
                name = "Vampyric " + name;
            }
            if (statGen.isCriticalHitActive()) {
                name = "Deadly " + name;
            }
            if (statGen.getElementType() == ELEM_FIRE) {
                name = name + " of Fire";
            } else if (statGen.getElementType() == ELEM_POISON) {
                name = name + " of Poison";
            } else if (statGen.getElementType() == ELEM_ICE) {
                name = name + " of Ice";
            }
        } else if (isArmor(itemType)) {
            List<String> oldLore = itemStack.getItemMeta().getLore();
            if (oldLore.size() > 2 && oldLore.get(2).contains("HP REGEN:")) {
                name = "Mending " + name;
            }
            if (statGen.isDodgeActive()) {
                name = "Agile " + name;
            }
            if (statGen.isThornsActive()) {
                name = "Thorny " + name;
            }
            if (statGen.isBlockActive()) {
                name = "Protective " + name;
            }
            if (oldLore.size() > 2 && oldLore.get(2).contains("ENERGY REGEN:")) {
                name = name + " of Fortitude";
            }
        }

        return name;
    }

    /**
     * Gets the base item name based on tier and type.
     *
     * @param tier The item's tier
     * @param itemType The item's type
     * @return The base name for the item
     */
    private static String getBaseItemName(int tier, int itemType) {
        switch (tier) {
            case 1:
                return getBaseTier1Name(itemType);
            case 2:
                return getBaseTier2Name(itemType);
            case 3:
                return getBaseTier3Name(itemType);
            case 4:
                return getBaseTier4Name(itemType);
            case 5:
                return getBaseTier5Name(itemType);
            case 6:
                return getBaseTier6Name(itemType);
            default:
                return "Unknown Item";
        }
    }

    private static String getBaseTier1Name(int itemType) {
        switch (itemType) {
            case TYPE_STAFF: return "Staff";
            case TYPE_SPEAR: return "Spear";
            case TYPE_SWORD: return "Shortsword";
            case TYPE_AXE: return "Hatchet";
            case TYPE_HELMET: return "Leather Coif";
            case TYPE_CHESTPLATE: return "Leather Chestplate";
            case TYPE_LEGGINGS: return "Leather Leggings";
            case TYPE_BOOTS: return "Leather Boots";
            default: return "Unknown Item";
        }
    }

    private static String getBaseTier2Name(int itemType) {
        switch (itemType) {
            case TYPE_STAFF: return "Battletaff";
            case TYPE_SPEAR: return "Halberd";
            case TYPE_SWORD: return "Broadsword";
            case TYPE_AXE: return "Great Axe";
            case TYPE_HELMET: return "Medium Helmet";
            case TYPE_CHESTPLATE: return "Chainmail";
            case TYPE_LEGGINGS: return "Chainmail Leggings";
            case TYPE_BOOTS: return "Chainmail Boots";
            default: return "Unknown Item";
        }
    }

    private static String getBaseTier3Name(int itemType) {
        switch (itemType) {
            case TYPE_STAFF: return "Wizard Staff";
            case TYPE_SPEAR: return "Magic Polearm";
            case TYPE_SWORD: return "Magic Sword";
            case TYPE_AXE: return "War Axe";
            case TYPE_HELMET: return "Full Helmet";
            case TYPE_CHESTPLATE: return "Platemail";
            case TYPE_LEGGINGS: return "Platemail Leggings";
            case TYPE_BOOTS: return "Platemail Boots";
            default: return "Unknown Item";
        }
    }

    private static String getBaseTier4Name(int itemType) {
        switch (itemType) {
            case TYPE_STAFF: return "Ancient Staff";
            case TYPE_SPEAR: return "Ancient Polearm";
            case TYPE_SWORD: return "Ancient Sword";
            case TYPE_AXE: return "Ancient Axe";
            case TYPE_HELMET: return "Ancient Full Helmet";
            case TYPE_CHESTPLATE: return "Magic Platemail";
            case TYPE_LEGGINGS: return "Magic Platemail Leggings";
            case TYPE_BOOTS: return "Magic Platemail Boots";
            default: return "Unknown Item";
        }
    }

    private static String getBaseTier5Name(int itemType) {
        switch (itemType) {
            case TYPE_STAFF: return "Legendary Staff";
            case TYPE_SPEAR: return "Legendary Polearm";
            case TYPE_SWORD: return "Legendary Sword";
            case TYPE_AXE: return "Legendary Axe";
            case TYPE_HELMET: return "Legendary Full Helmet";
            case TYPE_CHESTPLATE: return "Legendary Platemail";
            case TYPE_LEGGINGS: return "Legendary Platemail Leggings";
            case TYPE_BOOTS: return "Legendary Platemail Boots";
            default: return "Unknown Item";
        }
    }

    private static String getBaseTier6Name(int itemType) {
        switch (itemType) {
            case TYPE_STAFF: return "Frozen Staff";
            case TYPE_SPEAR: return "Frozen Polearm";
            case TYPE_SWORD: return "Frozen Sword";
            case TYPE_AXE: return "Frozen Axe";
            case TYPE_HELMET: return "Frozen Full Helmet";
            case TYPE_CHESTPLATE: return "Frozen Platemail";
            case TYPE_LEGGINGS: return "Frozen Platemail Leggings";
            case TYPE_BOOTS: return "Frozen Platemail Boots";
            default: return "Unknown Item";
        }
    }

    /**
     * Applies color to the item name based on its tier.
     *
     * @param name The item name
     * @param tier The item's tier
     * @return Colored item name
     */
    private static String applyColorBasedOnTier(String name, int tier) {
        switch (tier) {
            case 1:
                return ChatColor.WHITE + name;
            case 2:
                return ChatColor.GREEN + name;
            case 3:
                return ChatColor.AQUA + name;
            case 4:
                return ChatColor.LIGHT_PURPLE + name;
            case 5:
                return ChatColor.YELLOW + name;
            case 6:
                return ChatColor.BLUE + name;
            default:
                return name;
        }
    }

    /**
     * Applies the [+X] prefix to item names.
     *
     * @param name The item name
     * @param plus The plus value
     * @param isLegendaryOrb Whether a legendary orb is being used
     * @return The updated item name with plus value
     */
    private static String applyPlusToName(String name, int plus, boolean isLegendaryOrb) {
        int newPlus = plus;
        if (isLegendaryOrb) {
            newPlus = Math.max(plus, LEGENDARY_MINIMUM_PLUS);
        }

        if (ChatColor.stripColor(name).contains("[+")) {
            return name.replaceAll("\\[\\+\\d+\\]", "[+" + newPlus + "]");
        } else if (!ChatColor.stripColor(name).contains("[+") && newPlus >= 1) {
            return ChatColor.RED + "[+" + newPlus + "] " + name;
        }
        return name;
    }

    /**
     * Validates and ensures custom item names are preserved.
     *
     * @param itemStack The item to validate
     * @param oldName The previous name to check against
     */
    private static void validateItemName(ItemStack itemStack, String oldName) {
        ItemMeta im = itemStack.getItemMeta();
        if (oldName != null && oldName.equals(im.getDisplayName())) {
            im.setDisplayName(oldName); // Ensure custom name remains unchanged
            itemStack.setItemMeta(im);
        }
    }

    /**
     * Checks if an item type is a weapon.
     *
     * @param itemType The item type to check
     * @return True if the item is a weapon
     */
    private static boolean isWeapon(int itemType) {
        return itemType >= TYPE_STAFF && itemType <= TYPE_AXE;
    }

    /**
     * Checks if an item type is armor.
     *
     * @param itemType The item type to check
     * @return True if the item is armor
     */
    private static boolean isArmor(int itemType) {
        return itemType >= TYPE_HELMET && itemType <= TYPE_BOOTS;
    }

    /**
     * Shows a success effect to the player.
     *
     * @param player The player to show the effect to
     */
    private void showSuccessEffect(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.25f);

        Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder()
                .flicker(false)
                .withColor(Color.YELLOW)
                .withFade(Color.YELLOW)
                .with(FireworkEffect.Type.BURST)
                .trail(true)
                .build();

        fwm.addEffect(effect);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
    }

    /**
     * Shows a failure effect to the player.
     *
     * @param player The player to show the effect to
     */
    private void showFailureEffect(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2.0f, 1.25f);
        Particles.LAVA.display(0.0f, 0.0f, 0.0f, 5.0f, 10, player.getEyeLocation(), 20.0);
    }

    /**
     * Sets up the listener when the plugin is enabled.
     */
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, PracticeServer.getInstance());
    }

    /**
     * Cleans up resources when the plugin is disabled.
     */
    public void onDisable() {
        processingPlayers.clear();
    }

    /**
     * Handles click events in player inventories to apply orbs.
     *
     * @param event The inventory click event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInvClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getWhoClicked();

        // Only process events in the player's inventory
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) {
            return;
        }

        // Don't process armor slot clicks
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            return;
        }

        // Check if player recently interacted with vendors
        if (ItemVendors.isRecentlyInteracted(player)) {
            event.setCancelled(true);
            return;
        }

        // Prevent processing multiple orb applications simultaneously
        if (processingPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        // Process Legendary Orb of Alteration
        if (isLegendaryOrbOfAlteration(event.getCursor()) && isValidItemForOrb(event.getCurrentItem())) {
            if (Duels.duelers.containsKey(player)) {
                return; // Don't allow during duels
            }

            event.setCancelled(true);
            processingPlayers.add(player.getUniqueId());

            try {
                // Apply the orb
                ItemStack currentItem = event.getCurrentItem().clone();
                int oldSize = currentItem.getItemMeta().getLore().size();

                // Consume the orb
                ItemStack cursor = event.getCursor().clone();
                if (cursor.getAmount() > 1) {
                    cursor.setAmount(cursor.getAmount() - 1);
                    event.setCursor(cursor);
                } else {
                    event.setCursor(null);
                }

                // Apply legendary stats
                ItemStack newItem = randomizeLegendaryStats(currentItem, player);
                int newSize = newItem.getItemMeta().getLore().size();

                // Show appropriate effect
                if (newSize > oldSize) {
                    showSuccessEffect(player);
                } else {
                    showFailureEffect(player);
                }

                // Update the item
                newItem.setDurability((short) 0);
                event.setCurrentItem(newItem);
                ItemVendors.addToRecentlyInteracted(player);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing legendary orb application", e);
                player.sendMessage(ChatColor.RED + "An error occurred while applying the orb.");
            } finally {
                processingPlayers.remove(player.getUniqueId());
            }
        }

        // Process Normal Orb of Alteration
        else if (isNormalOrbOfAlteration(event.getCursor()) && isValidItemForOrb(event.getCurrentItem())) {
            if (Duels.duelers.containsKey(player)) {
                return; // Don't allow during duels
            }

            event.setCancelled(true);
            processingPlayers.add(player.getUniqueId());

            try {
                // Apply the orb
                ItemStack currentItem = event.getCurrentItem().clone();
                int oldSize = currentItem.getItemMeta().getLore().size();

                // Consume the orb
                ItemStack cursor = event.getCursor().clone();
                if (cursor.getAmount() > 1) {
                    cursor.setAmount(cursor.getAmount() - 1);
                    event.setCursor(cursor);
                } else {
                    event.setCursor(null);
                }

                // Apply normal stats
                ItemStack newItem = randomizeStats(currentItem);
                int newSize = newItem.getItemMeta().getLore().size();

                // Show appropriate effect
                if (newSize > oldSize) {
                    showSuccessEffect(player);
                } else {
                    showFailureEffect(player);
                }

                // Update the item
                newItem.setDurability((short) 0);
                event.setCurrentItem(newItem);
                ItemVendors.addToRecentlyInteracted(player);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing normal orb application", e);
                player.sendMessage(ChatColor.RED + "An error occurred while applying the orb.");
            } finally {
                processingPlayers.remove(player.getUniqueId());
            }
        }
    }

    /**
     * Checks if an item is a legendary orb of alteration.
     *
     * @param item The item to check
     * @return True if the item is a legendary orb
     */
    private boolean isLegendaryOrbOfAlteration(ItemStack item) {
        return item != null &&
                item.getType() == Material.MAGMA_CREAM &&
                item.hasItemMeta() &&
                item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals(LEGENDARY_ORB_NAME);
    }

    /**
     * Checks if an item is a normal orb of alteration.
     *
     * @param item The item to check
     * @return True if the item is a normal orb
     */
    private boolean isNormalOrbOfAlteration(ItemStack item) {
        return item != null &&
                item.getType() == Material.MAGMA_CREAM &&
                item.hasItemMeta() &&
                item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals(NORMAL_ORB_NAME);
    }

    /**
     * Checks if an item is valid for orb application.
     *
     * @param item The item to check
     * @return True if the item can be modified by orbs
     */
    private boolean isValidItemForOrb(ItemStack item) {
        return item != null &&
                item.getType() != Material.AIR &&
                item.hasItemMeta() &&
                item.getItemMeta().hasLore() &&
                getItemTier(item) > 0 &&
                getItemTier(item) < 7 &&
                getItemType(item) > -1 &&
                getItemType(item) <= 7;
    }

    /**
     * Base class for generating random stats for items.
     */
    private static class StatGenerator {
        protected final Random random = new Random();
        protected final int tier;

        // Stat activation flags
        protected final boolean pureActive;
        protected final boolean accuracyActive;
        protected final boolean vsMonstersActive;
        protected final boolean vsPlayersActive;
        protected final boolean lifeStealActive;
        protected final boolean criticalHitActive;
        protected final boolean dodgeActive;
        protected final boolean blockActive;
        protected final boolean vitalityActive;
        protected final boolean strengthActive;
        protected final boolean intelligenceActive;
        protected final boolean dexterityActive;
        protected final boolean thornsActive;
        protected final int elementType;

        // Stat values
        protected int pureAmount;
        protected int accuracyAmount;
        protected int vsMonstersAmount;
        protected int vsPlayersAmount;
        protected int lifeStealAmount;
        protected int criticalHitAmount;
        protected int dodgeAmount;
        protected int blockAmount;
        protected int vitalityAmount;
        protected int strengthAmount;
        protected int intelligenceAmount;
        protected int dexterityAmount;
        protected int thornsAmount;
        protected int elementAmount;

        /**
         * Creates a stat generator for the specified tier.
         *
         * @param tier The item tier
         */
        public StatGenerator(int tier) {
            this.tier = tier;

            // Determine which stats are active
            this.elementType = random.nextInt(3) + 1;
            this.pureActive = random.nextInt(3) + 1 == 1;
            this.lifeStealActive = random.nextInt(5) + 1 == 1;
            this.vsMonstersActive = random.nextInt(8) + 1 == 1;
            this.vsPlayersActive = random.nextInt(8) + 1 == 1;
            this.criticalHitActive = random.nextInt(3) + 1 == 1;
            this.accuracyActive = random.nextInt(4) + 1 == 1;
            this.dodgeActive = random.nextInt(3) + 1 == 1;
            this.blockActive = random.nextInt(3) + 1 == 1;
            this.vitalityActive = random.nextInt(3) + 1 == 1;
            this.strengthActive = random.nextInt(3) + 1 == 1;
            this.intelligenceActive = random.nextInt(3) + 1 == 1;
            this.dexterityActive = random.nextInt(3) + 1 == 1;
            this.thornsActive = random.nextInt(3) + 1 == 1;

            // Generate stat values based on tier
            generateStatValues();
        }

        /**
         * Generates stat values based on the item's tier.
         */
        protected void generateStatValues() {
            switch (tier) {
                case 1:
                    generateTier1Stats();
                    break;
                case 2:
                    generateTier2Stats();
                    break;
                case 3:
                    generateTier3Stats();
                    break;
                case 4:
                    generateTier4Stats();
                    break;
                case 5:
                    generateTier5Stats();
                    break;
                case 6:
                    generateTier6Stats();
                    break;
            }
        }

        private void generateTier1Stats() {
            dodgeAmount = random.nextInt(5) + 1;
            blockAmount = random.nextInt(5) + 1;
            vitalityAmount = random.nextInt(15) + 1;
            strengthAmount = random.nextInt(15) + 1;
            intelligenceAmount = random.nextInt(15) + 1;
            dexterityAmount = random.nextInt(15) + 1;
            elementAmount = random.nextInt(4) + 1;
            pureAmount = random.nextInt(4) + 1;
            lifeStealAmount = random.nextInt(30) + 1;
            criticalHitAmount = random.nextInt(3) + 1;
            accuracyAmount = random.nextInt(10) + 1;
            thornsAmount = random.nextInt(2) + 1;
            vsMonstersAmount = random.nextInt(4) + 1;
            vsPlayersAmount = random.nextInt(4) + 1;
        }

        private void generateTier2Stats() {
            dodgeAmount = random.nextInt(8) + 1;
            blockAmount = random.nextInt(8) + 1;
            vitalityAmount = random.nextInt(35) + 1;
            dexterityAmount = random.nextInt(35) + 1;
            strengthAmount = random.nextInt(35) + 1;
            intelligenceAmount = random.nextInt(35) + 1;
            elementAmount = random.nextInt(9) + 1;
            pureAmount = random.nextInt(9) + 1;
            lifeStealAmount = random.nextInt(15) + 1;
            criticalHitAmount = random.nextInt(6) + 1;
            accuracyAmount = random.nextInt(12) + 1;
            thornsAmount = random.nextInt(3) + 1;
            vsMonstersAmount = random.nextInt(5) + 1;
            vsPlayersAmount = random.nextInt(4) + 1;
        }

        private void generateTier3Stats() {
            dodgeAmount = random.nextInt(10) + 1;
            blockAmount = random.nextInt(10) + 1;
            vitalityAmount = random.nextInt(75) + 1;
            dexterityAmount = random.nextInt(75) + 1;
            strengthAmount = random.nextInt(75) + 1;
            intelligenceAmount = random.nextInt(75) + 1;
            elementAmount = random.nextInt(15) + 1;
            pureAmount = random.nextInt(15) + 1;
            lifeStealAmount = random.nextInt(12) + 1;
            criticalHitAmount = random.nextInt(8) + 1;
            accuracyAmount = random.nextInt(25) + 1;
            thornsAmount = random.nextInt(4) + 1;
            vsMonstersAmount = random.nextInt(8) + 1;
            vsPlayersAmount = random.nextInt(7) + 1;
        }

        private void generateTier4Stats() {
            dodgeAmount = random.nextInt(12) + 1;
            blockAmount = random.nextInt(12) + 1;
            vitalityAmount = random.nextInt(115) + 1;
            dexterityAmount = random.nextInt(115) + 1;
            strengthAmount = random.nextInt(115) + 1;
            intelligenceAmount = random.nextInt(115) + 1;
            elementAmount = random.nextInt(25) + 1;
            pureAmount = random.nextInt(25) + 1;
            lifeStealAmount = random.nextInt(10) + 1;
            criticalHitAmount = random.nextInt(10) + 1;
            accuracyAmount = random.nextInt(28) + 1;
            thornsAmount = random.nextInt(5) + 1;
            vsMonstersAmount = random.nextInt(10) + 1;
            vsPlayersAmount = random.nextInt(9) + 1;
        }

        private void generateTier5Stats() {
            dodgeAmount = random.nextInt(12) + 1;
            blockAmount = random.nextInt(12) + 1;
            vitalityAmount = random.nextInt(150) + 100;
            dexterityAmount = random.nextInt(150) + 100;
            strengthAmount = random.nextInt(150) + 100;
            intelligenceAmount = random.nextInt(150) + 100;
            elementAmount = random.nextInt(20) + 25;
            pureAmount = random.nextInt(20) + 25;
            lifeStealAmount = random.nextInt(4) + 4;
            criticalHitAmount = random.nextInt(5) + 6;
            accuracyAmount = random.nextInt(10) + 25;
            thornsAmount = random.nextInt(3) + 2;
            vsMonstersAmount = random.nextInt(12) + 1;
            vsPlayersAmount = random.nextInt(12) + 1;
        }

        private void generateTier6Stats() {
            dodgeAmount = random.nextInt(13) + 1;
            blockAmount = random.nextInt(13) + 1;
            vitalityAmount = random.nextInt(250) + 100;
            dexterityAmount = random.nextInt(250) + 100;
            strengthAmount = random.nextInt(250) + 100;
            intelligenceAmount = random.nextInt(250) + 100;
            elementAmount = random.nextInt(30) + 40;
            pureAmount = random.nextInt(30) + 40;
            lifeStealAmount = random.nextInt(4) + 4;
            criticalHitAmount = random.nextInt(5) + 6;
            accuracyAmount = random.nextInt(20) + 20;
            thornsAmount = random.nextInt(2) + 3;
            vsMonstersAmount = random.nextInt(6) + 6;
            vsPlayersAmount = random.nextInt(6) + 6;
        }

        // Getters for stat activation flags
        public boolean isPureActive() { return pureActive; }
        public boolean isAccuracyActive() { return accuracyActive; }
        public boolean isVsMonstersActive() { return vsMonstersActive; }
        public boolean isVsPlayersActive() { return vsPlayersActive; }
        public boolean isLifeStealActive() { return lifeStealActive; }
        public boolean isCriticalHitActive() { return criticalHitActive; }
        public boolean isDodgeActive() { return dodgeActive; }
        public boolean isBlockActive() { return blockActive; }
        public boolean isVitalityActive() { return vitalityActive; }
        public boolean isStrengthActive() { return strengthActive; }
        public boolean isIntelligenceActive() { return intelligenceActive; }
        public boolean isDexterityActive() { return dexterityActive; }
        public boolean isThornsActive() { return thornsActive; }
        public int getElementType() { return elementType; }

        // Getters for stat values
        public int getPureAmount() { return pureAmount; }
        public int getAccuracyAmount() { return accuracyAmount; }
        public int getVsMonstersAmount() { return vsMonstersAmount; }
        public int getVsPlayersAmount() { return vsPlayersAmount; }
        public int getLifeStealAmount() { return lifeStealAmount; }
        public int getCriticalHitAmount() { return criticalHitAmount; }
        public int getDodgeAmount() { return dodgeAmount; }
        public int getBlockAmount() { return blockAmount; }
        public int getVitalityAmount() { return vitalityAmount; }
        public int getStrengthAmount() { return strengthAmount; }
        public int getIntelligenceAmount() { return intelligenceAmount; }
        public int getDexterityAmount() { return dexterityAmount; }
        public int getThornsAmount() { return thornsAmount; }
        public int getElementAmount() { return elementAmount; }
    }

    /**
     * Extended stat generator for legendary orbs with bonus rolls.
     */
    private static class LegendaryStatGenerator extends StatGenerator {
        private final int bonusRolls;

        /**
         * Creates a legendary stat generator with bonus rolls.
         *
         * @param tier The item tier
         * @param bonusRolls Number of additional rolls to take the best result
         */
        public LegendaryStatGenerator(int tier, int bonusRolls) {
            super(tier);
            this.bonusRolls = bonusRolls;

            // Apply bonus rolls to increase stats
            if (bonusRolls > 0) {
                applyBonusRolls();
            }

            // Apply axe accuracy cap
            if (accuracyActive && accuracyAmount > 15) {
                accuracyAmount = ThreadLocalRandom.current().nextInt(15, 21);
            }
        }

        /**
         * Applies bonus rolls to get better stat values.
         */
        private void applyBonusRolls() {
            for (int i = 0; i < bonusRolls; i++) {
                // Generate a new set of stats
                StatGenerator temp = new StatGenerator(tier);

                // Keep the highest values
                if (temp.getDodgeAmount() > dodgeAmount) dodgeAmount = temp.getDodgeAmount();
                if (temp.getBlockAmount() > blockAmount) blockAmount = temp.getBlockAmount();
                if (temp.getVsMonstersAmount() > vsMonstersAmount) vsMonstersAmount = temp.getVsMonstersAmount();
                if (temp.getVsPlayersAmount() > vsPlayersAmount) vsPlayersAmount = temp.getVsPlayersAmount();
                if (temp.getVitalityAmount() > vitalityAmount) vitalityAmount = temp.getVitalityAmount();
                if (temp.getDexterityAmount() > dexterityAmount) dexterityAmount = temp.getDexterityAmount();
                if (temp.getStrengthAmount() > strengthAmount) strengthAmount = temp.getStrengthAmount();
                if (temp.getIntelligenceAmount() > intelligenceAmount) intelligenceAmount = temp.getIntelligenceAmount();
                if (temp.getElementAmount() > elementAmount) elementAmount = temp.getElementAmount();
                if (temp.getPureAmount() > pureAmount) pureAmount = temp.getPureAmount();
                if (temp.getLifeStealAmount() > lifeStealAmount) lifeStealAmount = temp.getLifeStealAmount();
                if (temp.getCriticalHitAmount() > criticalHitAmount) criticalHitAmount = temp.getCriticalHitAmount();
                if (temp.getAccuracyAmount() > accuracyAmount) accuracyAmount = temp.getAccuracyAmount();
                if (temp.getThornsAmount() > thornsAmount) thornsAmount = temp.getThornsAmount();
            }
        }
    }
}