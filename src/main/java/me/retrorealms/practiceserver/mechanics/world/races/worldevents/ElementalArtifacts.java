package me.retrorealms.practiceserver.mechanics.world.races;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.drops.CreateDrop;
import me.retrorealms.practiceserver.mechanics.item.Items;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class ElementalArtifacts implements Listener {
    private static final Map<UUID, ElementType> playerElements = new HashMap<>();
    private static final Map<UUID, Integer> elementalCharges = new HashMap<>();
    private static final int MAX_CHARGES = 5;

    public enum ElementType {
        FIRE, ICE, LIGHTNING, EARTH
    }

    public static ItemStack createElementalArtifact(int tier, int itemType, ElementType element) {
        ItemStack baseItem = CreateDrop.createDrop(tier, itemType, 4);
        ItemMeta meta = baseItem.getItemMeta();
        List<String> lore = meta.getLore();

        String elementPrefix = getElementPrefix(element);
        meta.setDisplayName(elementPrefix + meta.getDisplayName());

        lore.add(ChatColor.MAGIC + "xx " + ChatColor.RESET + elementPrefix + "Elemental Artifact" + ChatColor.MAGIC + " xx");
        lore.addAll(getElementalEffects(element, tier));
        lore.add(ChatColor.GRAY + "Charges: 0/" + MAX_CHARGES);

        meta.setLore(lore);
        baseItem.setItemMeta(meta);

        return baseItem;
    }

    private static String getElementPrefix(ElementType element) {
        switch (element) {
            case FIRE: return ChatColor.RED + "Blazing ";
            case ICE: return ChatColor.AQUA + "Frozen ";
            case LIGHTNING: return ChatColor.YELLOW + "Thundering ";
            case EARTH: return ChatColor.GREEN + "Earthen ";
            default: return "";
        }
    }

    private static List<String> getElementalEffects(ElementType element, int tier) {
        List<String> effects = new ArrayList<>();
        int effectPower = tier * 5;

        switch (element) {
            case FIRE:
                effects.add(ChatColor.RED + "Ignites enemies for " + effectPower + " damage over 5 seconds");
                effects.add(ChatColor.RED + "+" + (tier * 2) + "% Fire Damage");
                break;
            case ICE:
                effects.add(ChatColor.AQUA + "Slows enemies by " + effectPower + "% for 3 seconds");
                effects.add(ChatColor.AQUA + "+" + (tier * 2) + "% Ice Armor");
                break;
            case LIGHTNING:
                effects.add(ChatColor.YELLOW + "Chain lightning to " + (tier + 1) + " nearby enemies");
                effects.add(ChatColor.YELLOW + "+" + (tier * 2) + "% Attack Speed");
                break;
            case EARTH:
                effects.add(ChatColor.GREEN + "Grants +" + effectPower + " bonus armor for 5 seconds on hit");
                effects.add(ChatColor.GREEN + "+" + (tier * 2) + "% Max Health");
                break;
        }

        return effects;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        checkAndApplyElementalArtifact(player);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            checkAndApplyElementalArtifact((Player) event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        checkAndApplyElementalArtifact(event.getPlayer());
    }

    private void checkAndApplyElementalArtifact(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (isElementalArtifact(mainHand)) {
            ElementType element = getElementFromItem(mainHand);
            playerElements.put(player.getUniqueId(), element);
            elementalCharges.putIfAbsent(player.getUniqueId(), 0);
        } else {
            playerElements.remove(player.getUniqueId());
            elementalCharges.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        UUID playerId = player.getUniqueId();

        if (!playerElements.containsKey(playerId)) return;

        ElementType element = playerElements.get(playerId);
        int charges = elementalCharges.get(playerId);

        if (charges >= MAX_CHARGES) {
            applyElementalEffect(player, event.getEntity(), element);
            elementalCharges.put(playerId, 0);
            updateChargesLore(player);
        } else {
            elementalCharges.put(playerId, charges + 1);
            updateChargesLore(player);
        }
    }

    public static boolean playerHasArtifact(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isElementalArtifact(item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isElementalArtifact(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return false;
        }
        List<String> lore = item.getItemMeta().getLore();
        return lore.stream().anyMatch(line -> line.contains("Elemental Artifact"));
    }

    public static ElementType getCurrentElement() {
        // This method should return the current elemental storm type
        // You might want to store this information somewhere when you start an elemental storm
        // For now, let's return a random element
        return ElementType.values()[new Random().nextInt(ElementType.values().length)];
    }

    public static ItemStack getPlayerArtifact(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isElementalArtifact(item)) {
                return item;
            }
        }
        return null;
    }

    public static ElementType getElementFromItem(ItemStack item) {
        if (!isElementalArtifact(item)) {
            return null;
        }
        String name = item.getItemMeta().getDisplayName();
        if (name.contains("Blazing")) return ElementType.FIRE;
        if (name.contains("Frozen")) return ElementType.ICE;
        if (name.contains("Thundering")) return ElementType.LIGHTNING;
        if (name.contains("Earthen")) return ElementType.EARTH;
        return null;
    }

    private static void updateChargesLore(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        int charges = elementalCharges.get(player.getUniqueId());

        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).contains("Charges:")) {
                lore.set(i, ChatColor.GRAY + "Charges: " + charges + "/" + MAX_CHARGES);
                break;
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private static void applyElementalEffect(Player attacker, Entity target, ElementType element) {
        switch (element) {
            case FIRE:
                applyFireEffect(attacker, target);
                break;
            case ICE:
                applyIceEffect(attacker, target);
                break;
            case LIGHTNING:
                applyLightningEffect(attacker, target);
                break;
            case EARTH:
                applyEarthEffect(attacker, target);
                break;
        }
    }

    private static void applyFireEffect(Player attacker, Entity target) {
        if (!(target instanceof LivingEntity)) return;
        LivingEntity livingTarget = (LivingEntity) target;
        int fireTicks = 100; // 5 seconds
        livingTarget.setFireTicks(fireTicks);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 5 || livingTarget.isDead()) {
                    this.cancel();
                    return;
                }
                livingTarget.damage(2, attacker);
                livingTarget.getWorld().spawnParticle(Particle.FLAME, livingTarget.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
                ticks++;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 20L);
    }

    private static void applyIceEffect(Player attacker, Entity target) {
        if (!(target instanceof LivingEntity)) return;
        LivingEntity livingTarget = (LivingEntity) target;
        livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 2));

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 3 || livingTarget.isDead()) {
                    this.cancel();
                    return;
                }
                livingTarget.damage(1, attacker);
                livingTarget.getWorld().spawnParticle(Particle.SNOW_SHOVEL, livingTarget.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.05);
                ticks++;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 20L);
    }

    private static void applyLightningEffect(Player attacker, Entity initialTarget) {
        if (!(initialTarget instanceof LivingEntity)) return;
        LivingEntity livingTarget = (LivingEntity) initialTarget;

        List<LivingEntity> nearbyEntities = livingTarget.getNearbyEntities(5, 5, 5).stream()
                .filter(e -> e instanceof LivingEntity && e != attacker && !(e instanceof Player))
                .map(e -> (LivingEntity) e)
                .collect(Collectors.toList());

        Collections.shuffle(nearbyEntities);
        int chainCount = Math.min(3, nearbyEntities.size());

        LivingEntity previousTarget = livingTarget;
        for (int i = 0; i < chainCount; i++) {
            LivingEntity currentTarget = nearbyEntities.get(i);
            currentTarget.damage(5, attacker);

            Location start = previousTarget.getLocation().add(0, 1, 0);
            Location end = currentTarget.getLocation().add(0, 1, 0);
            drawLightningBolt(start, end);

            previousTarget = currentTarget;
        }
    }

    private static void drawLightningBolt(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = start.distance(end);
        for (double d = 0; d < distance; d += 0.5) {
            Location particleLoc = start.clone().add(direction.clone().multiply(d / distance));
            particleLoc.add(Math.random() * 0.2 - 0.1, Math.random() * 0.2 - 0.1, Math.random() * 0.2 - 0.1);
            particleLoc.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    private static void applyEarthEffect(Player attacker, Entity target) {
        if (!(target instanceof LivingEntity)) return;
        LivingEntity livingTarget = (LivingEntity) target;

        // Create a temporary earth wall
        List<Block> wallBlocks = new ArrayList<>();
        Location targetLoc = livingTarget.getLocation();
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                Block block = targetLoc.clone().add(x, y, 0).getBlock();
                if (block.getType() == Material.AIR) {
                    block.setType(Material.DIRT);
                    wallBlocks.add(block);
                }
            }
        }

        // Remove the wall after 3 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block block : wallBlocks) {
                    block.setType(Material.AIR);
                    block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation(), 10, 0.5, 0.5, 0.5, 0, Material.DIRT.getData());
                }
            }
        }.runTaskLater(PracticeServer.getInstance(), 60L);

        // Knock back the target
        Vector knockback = attacker.getLocation().getDirection().multiply(2);
        livingTarget.setVelocity(knockback);
    }

    public static void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new ElementalArtifacts(), PracticeServer.getInstance());
    }
}