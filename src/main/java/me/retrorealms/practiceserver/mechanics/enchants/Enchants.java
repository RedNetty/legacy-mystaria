package me.retrorealms.practiceserver.mechanics.enchants;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.itemapi.ItemAPI;
import me.retrorealms.practiceserver.mechanics.damage.Damage;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.item.Items;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayer;
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayers;
import me.retrorealms.practiceserver.mechanics.vendors.ItemVendors;
import me.retrorealms.practiceserver.utils.Particles;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

/**
 * Handles all item enhancement mechanics including:
 * - Item upgrading (+1, +2, etc.)
 * - Protection scrolls
 * - Success/failure mechanics
 */
public class Enchants implements Listener {
    // Constants for enhancement system
    private static final String CRAFTING_INVENTORY = "container.crafting";
    private static final int MAX_SAFE_ENHANCEMENT = 3;
    private static final int MAX_ENHANCEMENT = 12;
    private static final double STAT_INCREASE_PERCENT = 0.05;
    private static final double MIN_STAT_INCREASE = 1.0;

    // Static array of failure chances for each enhancement level
    private static final int[] FAILURE_CHANCES = {0, 0, 0, 30, 40, 50, 65, 75, 80, 85, 90, 95};

    // Glow enchantment for visual effects
    public static Enchantment glow;

    // Initialize glow enchantment
    static {
        glow = new GlowEnchant(69);
    }

    /**
     * Registers the custom glow enchantment with Bukkit
     * @return true if registration was successful
     */
    public static boolean registerNewEnchantment() {
        try {
            // Use reflection to allow new enchantments to be registered
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);

            try {
                Enchantment.registerEnchantment(glow);
                return true;
            } catch (IllegalArgumentException ex) {
                PracticeServer.log.log(Level.WARNING, "Failed to register glow enchantment (already registered)", ex);
            }
        } catch (Exception ex) {
            PracticeServer.log.log(Level.SEVERE, "Failed to register glow enchantment", ex);
        }
        return false;
    }

    /**
     * Extract the enhancement level (plus value) from an item's display name
     * @param item The item to check
     * @return The enhancement level, or 0 if none
     */
    public static int getPlus(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return 0;
        }

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (name.startsWith("[+")) {
            try {
                return Integer.parseInt(name.split("\\[\\+")[1].split("\\]")[0]);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Enables the enchantment system
     */
    public void onEnable() {
        PracticeServer.log.info("[Enchants] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        registerNewEnchantment();
    }

    /**
     * Disables the enchantment system
     */
    public void onDisable() {
        PracticeServer.log.info("[Enchants] has been disabled.");
    }

    /**
     * Prevents using empty maps as regular maps
     */
    @EventHandler
    public void onMapUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            return;
        }

        if (itemInHand.getType() == Material.EMPTY_MAP) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles applying protection scrolls to items
     */
    @EventHandler
    public void onProtectionApply(InventoryClickEvent event) {
        // Early validation
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR ||
                event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR ||
                !event.getInventory().getName().equalsIgnoreCase(CRAFTING_INVENTORY) ||
                event.getSlotType() == InventoryType.SlotType.ARMOR) {
            return;
        }

        ItemStack scrollItem = event.getCursor();
        ItemStack targetItem = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        // Check if the cursor item is a protection scroll
        if (!ItemAPI.isProtectionScroll(scrollItem)) {
            return;
        }

        // Check if the item is already protected
        if (ItemAPI.isProtected(targetItem)) {
            player.sendMessage(ChatColor.RED + "ITEM ALREADY PROTECTED");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 10F, 1F);
            return;
        }

        // Check if the item and scroll tiers match
        if (!ItemAPI.canEnchant(targetItem, scrollItem)) {
            player.sendMessage(ChatColor.RED + "ITEM CAN'T BE PROTECTED: MUST BE THE SAME TIER");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 10F, 1F);
            return;
        }

        // Apply protection
        event.setCancelled(true);
        event.setCurrentItem(ItemAPI.makeProtected(targetItem));

        // Success feedback
        player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "       ->  ITEM PROTECTED");
        player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "       " + targetItem.getItemMeta().getDisplayName());

        // Spawn success firework
        spawnFirework(player.getLocation(), FireworkEffect.Type.STAR, Color.GREEN);

        // Consume protection scroll
        consumeItem(event, scrollItem);
    }

    /**
     * Main event handler for item enhancement
     */
    @EventHandler
    public void onEnhancement(InventoryClickEvent event) throws Exception {
        // Early validation
        if (event.getCursor() == null || event.getCurrentItem() == null ||
                !event.getInventory().getName().equalsIgnoreCase(CRAFTING_INVENTORY) ||
                event.getSlotType() == InventoryType.SlotType.ARMOR) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack scrollItem = event.getCursor();
        ItemStack targetItem = event.getCurrentItem();

        // Check if in duel
        if (Duels.duelers.containsKey(player)) {
            player.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " use this item while in a duel");
            event.setCancelled(true);
            return;
        }

        // Check for rate limiting to prevent potential exploits
        if (ItemVendors.isRecentlyInteracted(player)) {
            event.setCancelled(true);
            return;
        }

        // If it's an armor enhancement scroll
        if (isArmorEnhancementScroll(scrollItem) && isEnhanceableArmor(targetItem, scrollItem)) {
            event.setCancelled(true);
            enhanceArmor(event, player, targetItem, scrollItem);
            return;
        }

        // If it's a weapon enhancement scroll
        if (isWeaponEnhancementScroll(scrollItem) && isEnhanceableWeapon(targetItem, scrollItem)) {
            event.setCancelled(true);
            enhanceWeapon(event, player, targetItem, scrollItem);
            return;
        }
    }

    /**
     * Handle armor enhancement logic
     */
    private void enhanceArmor(InventoryClickEvent event, Player player, ItemStack armor, ItemStack scroll) {
        int currentPlus = getPlus(armor);

        // Validation
        if (currentPlus >= MAX_ENHANCEMENT) {
            player.sendMessage(ChatColor.RED + "This item is already at maximum enhancement level");
            return;
        }

        // Get current stats
        double currentHp = Damage.getHp(armor);
        double currentHpRegen = Damage.getHps(armor);
        int currentEnergy = Damage.getEnergy(armor);
        List<String> currentLore = armor.getItemMeta().getLore();
        String itemName = armor.getItemMeta().getDisplayName();

        // Remove plus from name for processing
        if (itemName.startsWith(ChatColor.RED + "[+")) {
            itemName = itemName.split("] ")[1];
        }

        // Consume the scroll first to prevent duplication
        consumeItem(event, scroll);

        // Check enhancement success
        boolean success = true;
        if (currentPlus >= MAX_SAFE_ENHANCEMENT) {
            success = attemptRiskyEnhancement(player, currentPlus);
        }

        // Handle enhancement failure
        if (!success) {
            if (ItemAPI.isProtected(armor)) {
                event.setCurrentItem(ItemAPI.removeProtection(armor));
                player.sendMessage(ChatColor.GREEN + "YOUR PROTECTION SCROLL HAS PREVENTED THIS ITEM FROM VANISHING");
            } else {
                event.setCurrentItem(null);
            }
            return;
        }

        // Enhancement succeeded - update item
        double hpIncrease = Math.max(currentHp * STAT_INCREASE_PERCENT, MIN_STAT_INCREASE);
        int newHp = (int) (currentHp + hpIncrease);

        // Create updated item
        ItemStack enhancedItem = armor.clone();
        ItemMeta meta = enhancedItem.getItemMeta();

        // Update name
        meta.setDisplayName(ChatColor.RED + "[+" + (currentPlus + 1) + "] " + itemName);

        // Update lore with new stats
        List<String> lore = new ArrayList<>(meta.getLore());
        lore.set(1, ChatColor.RED + "HP: +" + newHp);

        // Update either energy regen or hp regen
        if (currentLore.get(2).contains("ENERGY REGEN")) {
            lore.set(2, ChatColor.RED + "ENERGY REGEN: +" + (currentEnergy + 1) + "%");
        } else if (currentLore.get(2).contains("HP REGEN")) {
            double hpRegenIncrease = Math.max(currentHpRegen * STAT_INCREASE_PERCENT, MIN_STAT_INCREASE);
            int newHpRegen = (int) (currentHpRegen + hpRegenIncrease);
            lore.set(2, ChatColor.RED + "HP REGEN: +" + newHpRegen + "/s");
        }

        meta.setLore(lore);
        enhancedItem.setItemMeta(meta);

        // Add glow effect for items +4 and above
        if (currentPlus + 1 >= MAX_SAFE_ENHANCEMENT) {
            enhancedItem.addUnsafeEnchantment(glow, 1);
        }

        // Remove protection if one was used
        enhancedItem = ItemAPI.removeProtection(enhancedItem);

        // Update the item in inventory
        event.setCurrentItem(enhancedItem);

        // Spawn success firework and play sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.25f);
        spawnFirework(player.getLocation(), FireworkEffect.Type.BURST, Color.YELLOW);

        // Add cooldown to prevent exploits
        ItemVendors.addToRecentlyInteracted(player);
    }

    /**
     * Handle weapon enhancement logic
     */
    private void enhanceWeapon(InventoryClickEvent event, Player player, ItemStack weapon, ItemStack scroll) {
        int currentPlus = getPlus(weapon);

        // Validation
        if (currentPlus >= MAX_ENHANCEMENT) {
            player.sendMessage(ChatColor.RED + "This item is already at maximum enhancement level");
            return;
        }

        // Get current stats
        double currentMinDmg = Damage.getDamageRange(weapon).get(0);
        double currentMaxDmg = Damage.getDamageRange(weapon).get(1);
        String itemName = weapon.getItemMeta().getDisplayName();

        // Remove plus from name for processing
        if (itemName.startsWith(ChatColor.RED + "[+")) {
            itemName = itemName.split("] ")[1];
        }

        // Consume the scroll first to prevent duplication
        consumeItem(event, scroll);

        // Check enhancement success
        boolean success = true;
        if (currentPlus >= MAX_SAFE_ENHANCEMENT) {
            success = attemptRiskyEnhancement(player, currentPlus);
        }

        // Handle enhancement failure
        if (!success) {
            if (ItemAPI.isProtected(weapon)) {
                event.setCurrentItem(ItemAPI.removeProtection(weapon));
                player.sendMessage(ChatColor.GREEN + "YOUR PROTECTION SCROLL HAS PREVENTED THIS ITEM FROM VANISHING");
            } else {
                event.setCurrentItem(null);
            }
            return;
        }

        // Enhancement succeeded - update item
        double minDmgIncrease = Math.max(currentMinDmg * STAT_INCREASE_PERCENT, MIN_STAT_INCREASE);
        double maxDmgIncrease = Math.max(currentMaxDmg * STAT_INCREASE_PERCENT, MIN_STAT_INCREASE);

        int newMinDmg = (int) (currentMinDmg + minDmgIncrease);
        int newMaxDmg = (int) (currentMaxDmg + maxDmgIncrease);

        // Create updated item
        ItemStack enhancedItem = weapon.clone();
        ItemMeta meta = enhancedItem.getItemMeta();

        // Update name
        meta.setDisplayName(ChatColor.RED + "[+" + (currentPlus + 1) + "] " + itemName);

        // Update lore with new damage values
        List<String> lore = new ArrayList<>(meta.getLore());
        lore.set(0, ChatColor.RED + "DMG: " + newMinDmg + " - " + newMaxDmg);

        meta.setLore(lore);
        enhancedItem.setItemMeta(meta);

        // Add glow effect for items +4 and above
        if (currentPlus + 1 >= MAX_SAFE_ENHANCEMENT) {
            enhancedItem.addUnsafeEnchantment(glow, 1);
        }

        // Remove protection if one was used
        enhancedItem = ItemAPI.removeProtection(enhancedItem);

        // Update the item in inventory
        event.setCurrentItem(enhancedItem);

        // Spawn success firework and play sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.25f);
        spawnFirework(player.getLocation(), FireworkEffect.Type.BURST, Color.YELLOW);

        // Add cooldown to prevent exploits
        ItemVendors.addToRecentlyInteracted(player);
    }

    /**
     * Attempt a risky enhancement with chance of failure
     * @return true if enhancement succeeded, false if failed
     */
    private boolean attemptRiskyEnhancement(Player player, int currentPlus) {
        // Determine failure chance based on current plus level
        int failureChance = currentPlus < MAX_ENHANCEMENT ? FAILURE_CHANCES[currentPlus] : 95;

        // Apply luck from player stats (if applicable)
        if (!PracticeServer.BETA_VENDOR_ENABLED) {
            PersistentPlayer pp = PersistentPlayers.get(player.getUniqueId());
            failureChance -= pp.luck * 2;

            // Ensure failure chance is within bounds
            failureChance = Math.max(0, Math.min(95, failureChance));
        } else {
            // In beta mode, always succeed
            failureChance = 0;
        }

        // Roll for success
        int roll = new Random().nextInt(100) + 1;

        if (roll <= failureChance) {
            // Failure effects
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 2.0f, 1.25f);
            Particles.LAVA.display(0.0f, 0.0f, 0.0f, 5.0f, 10, player.getEyeLocation(), 20.0);
            return false;
        }

        return true;
    }

    /**
     * Check if an item is a valid armor enhancement scroll
     */
    private boolean isArmorEnhancementScroll(ItemStack item) {
        return item != null &&
                item.getType() == Material.EMPTY_MAP &&
                item.hasItemMeta() &&
                item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().contains("Armor");
    }

    /**
     * Check if an item is a valid weapon enhancement scroll
     */
    private boolean isWeaponEnhancementScroll(ItemStack item) {
        return item != null &&
                item.getType() == Material.EMPTY_MAP &&
                item.hasItemMeta() &&
                item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().contains("Weapon");
    }

    /**
     * Check if armor can be enhanced with the given scroll (matching tiers)
     */
    private boolean isEnhanceableArmor(ItemStack armor, ItemStack scroll) {
        if (armor == null || !armor.hasItemMeta() || !armor.getItemMeta().hasDisplayName() ||
                !armor.getItemMeta().hasLore() || scroll == null || !scroll.hasItemMeta()) {
            return false;
        }

        String armorType = armor.getType().name();
        String scrollName = scroll.getItemMeta().getDisplayName();

        if (!armorType.contains("_HELMET") && !armorType.contains("_CHESTPLATE") &&
                !armorType.contains("_LEGGINGS") && !armorType.contains("_BOOTS")) {
            return false;
        }

        // Check material tiers match
        boolean isBlueLeather = Items.isBlueLeather(armor);

        return (isBlueLeather && armorType.contains("LEATHER_") && scrollName.contains("Frozen")) ||
                (armorType.contains("GOLD_") && scrollName.contains("Gold")) ||
                (armorType.contains("DIAMOND_") && scrollName.contains("Diamond")) ||
                (armorType.contains("IRON_") && scrollName.contains("Iron")) ||
                (armorType.contains("CHAINMAIL_") && scrollName.contains("Chainmail")) ||
                (!isBlueLeather && armorType.contains("LEATHER_") && scrollName.contains("Leather"));
    }

    /**
     * Check if weapon can be enhanced with the given scroll (matching tiers)
     */
    private boolean isEnhanceableWeapon(ItemStack weapon, ItemStack scroll) {
        if (weapon == null || !weapon.hasItemMeta() || !weapon.getItemMeta().hasDisplayName() ||
                !weapon.getItemMeta().hasLore() || scroll == null || !scroll.hasItemMeta()) {
            return false;
        }

        String weaponType = weapon.getType().name();
        String scrollName = scroll.getItemMeta().getDisplayName();
        String weaponName = weapon.getItemMeta().getDisplayName();

        if (!weaponType.contains("_SWORD") && !weaponType.contains("_HOE") &&
                !weaponType.contains("_SPADE") && !weaponType.contains("_AXE")) {
            return false;
        }

        // Check material tiers match
        return (weaponName.contains(ChatColor.BLUE.toString()) && weaponType.contains("DIAMOND_") && scrollName.contains("Frozen")) ||
                (weaponType.contains("GOLD_") && scrollName.contains("Gold")) ||
                (!weaponName.contains(ChatColor.BLUE.toString()) && weaponType.contains("DIAMOND_") && scrollName.contains("Diamond")) ||
                (weaponType.contains("IRON_") && scrollName.contains("Iron")) ||
                (weaponType.contains("STONE_") && scrollName.contains("Stone")) ||
                (weaponType.contains("WOOD_") && scrollName.contains("Wooden"));
    }

    /**
     * Helper method to safely consume one item from a stack
     */
    private void consumeItem(InventoryClickEvent event, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            event.setCursor(new ItemStack(Material.AIR));
        }
    }

    /**
     * Helper method to spawn a firework at a location
     */
    private void spawnFirework(Location location, FireworkEffect.Type type, Color color) {
        Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        FireworkEffect effect = FireworkEffect.builder()
                .flicker(false)
                .withColor(color)
                .withFade(color)
                .with(type)
                .trail(true)
                .build();

        fwm.addEffect(effect);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
    }
}