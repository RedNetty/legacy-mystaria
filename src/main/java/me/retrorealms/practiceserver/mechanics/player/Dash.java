package me.retrorealms.practiceserver.mechanics.player;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.actionbar.ActionBar;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Dash implements Listener {

    private static final double DASH_SPEED = 1.35; // Adjust this value to change dash speed
    private static final int DASH_DURATION = 2; // Duration of dash in ticks (5 ticks = 0.25 seconds)
    private static final int COOLDOWN_SECONDS = 15;
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    public Dash() {
        PracticeServer.getInstance().getServer().getPluginManager().registerEvents(this, PracticeServer.getInstance());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();
        if (event.hasBlock()) {
            Block clickedBlock = event.getClickedBlock();
            if (isInteractiveBlock(clickedBlock)) {
                return; // Don't dash if interacting with an interactive block
            }
        }
        if (isNearNPC(player)) {
            return; // Don't dash if near an NPC
        }

        if (item == null || (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        boolean doDash = isWeapon(item) || (player.isSneaking() && isStaff(item));

        if (doDash && canDash(player)) {
                performDash(player);

        }
    }
    private boolean isInteractiveBlock(Block block) {
        if (block == null) return false;
        Material type = block.getType();
        return type == Material.CHEST
                || type == Material.TRAPPED_CHEST
                || type == Material.ENDER_CHEST
                || type == Material.WORKBENCH
                || type == Material.ANVIL
                || type == Material.FURNACE
                || type == Material.BREWING_STAND
                || type == Material.ENCHANTMENT_TABLE
                || type.name().contains("SHULKER_BOX");
    }
    private boolean isNearNPC(Player player) {
        List<Entity> nearbyEntities = player.getNearbyEntities(4, 4, 4); // Check in a 3-block radius
        for (Entity entity : nearbyEntities) {
            if (isNPC(entity)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNPC(Entity entity) {
        return entity.hasMetadata("NPC");
    }
    private boolean isWeapon(ItemStack item) {
        if (item == null) return false;
        String type = item.getType().name();
        return type.endsWith("_SWORD") || type.endsWith("_AXE")|| type.endsWith("_SPADE");
    }

    private boolean isStaff(ItemStack item) {
        if (item == null) return false;
        return item.getType().name().endsWith("_HOE");
    }

    private boolean canDash(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) {
            return true;
        }
        return System.currentTimeMillis() - cooldowns.get(player.getUniqueId()) > COOLDOWN_SECONDS * 1000;
    }

    private void performDash(Player player) {
        Vector direction = player.getLocation().getDirection().setY(0).normalize().multiply(DASH_SPEED);
        Location startLoc = player.getLocation();

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= DASH_DURATION) {
                    this.cancel();
                    return;
                }

                player.setVelocity(direction);
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 5, 0.2, 0.2, 0.2, 0.05);
                ticks++;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0, 1);

        // Play sound effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1.0f, 1.5f);

        // Create a trail of particles
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= DASH_DURATION * 4) {  // Particle effect lasts longer than the dash
                    this.cancel();
                    return;
                }

                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.05);
                ticks++;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0, 1);

        // Set cooldown
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        // Start cooldown timer display
        startCooldownTimer(player);
    }

    private void startCooldownTimer(Player player) {
        new BukkitRunnable() {
            int secondsLeft = COOLDOWN_SECONDS;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    ActionBar.sendActionBar(player, ChatColor.GRAY + "Dash Cooldown: " + ChatColor.RED + secondsLeft + "s", 2);
                    secondsLeft--;
                } else {
                    ActionBar.sendActionBar(player, ChatColor.GREEN + "Dash Ready!", 2);
                    this.cancel();
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0, 20); // Update every second
    }
}