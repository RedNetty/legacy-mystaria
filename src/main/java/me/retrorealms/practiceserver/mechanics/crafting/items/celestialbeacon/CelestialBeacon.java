package me.retrorealms.practiceserver.mechanics.crafting.items.celestialbeacon;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.crafting.items.CustomItem;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CelestialBeacon extends CustomItem implements Listener {
    private static final int SUMMON_DURATION = 60; // Duration in seconds

    public CelestialBeacon() {

        super("celestial_beacon", ChatColor.GOLD + "Celestial Beacon", Material.BEACON, .2f,
                Arrays.asList(
                        ChatColor.GRAY + "Summons a celestial ally to fight alongside you.",
                        ChatColor.GRAY + "Duration: " + SUMMON_DURATION + " seconds",
                        ChatColor.GOLD + "Legendary Item"
                ), ActivationType.RIGHT_CLICK);
    }

    @Override
    public void applyEffects(Player player) {
        // Remove the beacon from the player's inventory
        removeItemFromInventory(player);

        // Start the summoning animation
        startSummoningAnimation(player);
    }

    private void removeItemFromInventory(Player player) {
        player.getInventory().removeItem(player.getInventory().getItemInMainHand());
        player.updateInventory();
    }

    private void startSummoningAnimation(Player player) {
        Location center = player.getLocation().add(0, 0.5, 0);
        World world = player.getWorld();

        new BukkitRunnable() {
            int tick = 0;
            final int ANIMATION_DURATION = 60; // 3 seconds

            @Override
            public void run() {
                if (tick >= ANIMATION_DURATION) {
                    spawnCelestialAlly(player, center);
                    this.cancel();
                    return;
                }

                // Spiral particle effect
                double angle = tick * 0.5;
                double x = Math.cos(angle) * (1 - tick / (double) ANIMATION_DURATION);
                double z = Math.sin(angle) * (1 - tick / (double) ANIMATION_DURATION);
                Location particleLoc = center.clone().add(x, tick * 0.05, z);
                world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);

                // Ground ring effect
                for (int i = 0; i < 8; i++) {
                    double ringAngle = i * Math.PI / 4 + tick * 0.2;
                    double ringX = Math.cos(ringAngle) * 1.5;
                    double ringZ = Math.sin(ringAngle) * 1.5;
                    Location ringLoc = center.clone().add(ringX, 0.1, ringZ);
                    world.spawnParticle(Particle.SPELL_WITCH, ringLoc, 1, 0, 0, 0, 0);
                }

                // Sound effects
                if (tick % 10 == 0) {
                    world.playSound(center, Sound.BLOCK_NOTE_CHIME, 1.0f, 1.0f + (tick / (float) ANIMATION_DURATION));
                }

                tick++;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 1L);
    }

    private void spawnCelestialAlly(Player player, Location location) {
        CelestialAlly ally = new CelestialAlly(location, player);

        // Add the entity to the world
        ((CraftWorld) location.getWorld()).getHandle().addEntity(ally, CreatureSpawnEvent.SpawnReason.CUSTOM);

        // Final effects
        location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 10, 0.5, 0.5, 0.5, 0.1);
        location.getWorld().playSound(location, Sound.ENTITY_ENDERDRAGON_GROWL, 1.0f, 1.0f);

        player.sendMessage(ChatColor.GOLD + "You have summoned a Celestial Ally!");
        System.out.println("CelestialAlly spawned successfully at " + ally.locX + ", " + ally.locY + ", " + ally.locZ);
        ally.getBukkitEntity().setMetadata("CelestialAlly", new FixedMetadataValue(PracticeServer.getInstance(), player.getUniqueId().toString()));
        adjustAlly((LivingEntity) ally.getBukkitEntity());
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.getInstance());
        // Debug print to confirm metadata is set
        System.out.println("Metadata set: " + ally.getBukkitEntity().getMetadata("CelestialAlly").get(0).asString());
        // Set despawn timer
        Bukkit.getScheduler().runTaskLater(PracticeServer.getInstance(), () -> {
            if (!ally.dead) {
                ally.die();
                player.sendMessage(ChatColor.GOLD + "Your Celestial Ally has departed.");
            }
        }, 20L * SUMMON_DURATION);
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCelestialPlayer(EntityDamageByEntityEvent event) {
        if (event.getEntity().hasMetadata("CelestialAlly")) {
            System.out.print("Is Ally");
            List<MetadataValue> metadata = event.getEntity().getMetadata("CelestialAlly");
            if (metadata.isEmpty()) {
                System.out.println("Metadata is empty");
                return;
            }
            String ownerUUID = metadata.get(0).asString();
            System.out.print(ownerUUID);
            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();
                if (damager.getUniqueId().toString().equals(ownerUUID)) {
                    System.out.print("Is Owner Damaging");
                    // Owner is trying to damage the Celestial Ally
                    event.setDamage(0.0); // Set the damage to 0, effectively preventing any health reduction
                    damager.sendMessage(ChatColor.GOLD + "You cannot damage your own Celestial Ally.");
                    event.setCancelled(true);
                    return;
                }
                if (Parties.isInParty(Bukkit.getPlayer(UUID.fromString(ownerUUID))) && Parties.arePartyMembers(Bukkit.getPlayer(UUID.fromString(ownerUUID)), damager)) {
                    // Damager is a party member of the Celestial Ally's owner
                    event.setDamage(0.0); // Set the damage to 0, effectively preventing any health reduction
                    damager.sendMessage(ChatColor.GOLD + "You cannot damage your party member's Celestial Ally.");
                    event.setCancelled(true);
                    return;
                }
            }
            // Allow the damage to proceed for non-owner and non-party member targets
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCelestialDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager().hasMetadata("CelestialAlly")) {
            System.out.println("has metadata");
            List<MetadataValue> metadata = event.getDamager().getMetadata("CelestialAlly");
            if (metadata.isEmpty()) {
                System.out.println("Metadata is empty");
                return;
            }
            String ownerUUID = metadata.get(0).asString();
            System.out.println("has metadata " + ownerUUID);
            if (event.getEntity() instanceof Player) {
                Player target = (Player) event.getEntity();
                if (target.getUniqueId().toString().equals(ownerUUID)) {
                    event.setDamage(0.0);
                    event.setCancelled(true);
                    return;
                }
                if (Parties.isInParty(Bukkit.getPlayer(UUID.fromString(ownerUUID))) && Parties.arePartyMembers(Bukkit.getPlayer(UUID.fromString(ownerUUID)), target)) {
                    event.setDamage(0.0);
                    event.setCancelled(true);
                    return;
                }
            }
            // Allow the damage to proceed for non-owner targets
        }
    }
    public void adjustAlly(LivingEntity livingEntity) {
        if(livingEntity.hasMetadata("CelestialAlly")) {
            Player player = Bukkit.getPlayer(UUID.fromString(livingEntity.getMetadata("CelestialAlly").get(0).asString()));
            System.out.println("Celestial Ally Spawn Event" + " Player;" + player.getName());
            if (player == null) return;
            Entity ally = livingEntity;
            EntityEquipment equipment = livingEntity.getEquipment();
            // Copy player's armor
            equipment.setHelmet(player.getInventory().getHelmet());
            equipment.setChestplate(player.getInventory().getChestplate());
            equipment.setLeggings(player.getInventory().getLeggings());
            equipment.setBoots(player.getInventory().getBoots());

            // Copy player's first hotbar slot (weapon)
            equipment.setItemInMainHand(player.getInventory().getItem(0));
            livingEntity.setCustomName(ChatColor.GOLD + "[Celestial Ally] " + ChatColor.RESET + player.getName());
            livingEntity.setMaxHealth(player.getMaxHealth());
            livingEntity.setHealth(player.getHealth());

            equipment.setBootsDropChance(0.0F);
            equipment.setLeggingsDropChance(0.0F);
            equipment.setChestplateDropChance(0.0F);
            equipment.setHelmetDropChance(0.0F);
            equipment.setItemInMainHandDropChance(0.0F);

            ally.setGlowing(true);
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * SUMMON_DURATION, 1));

        }
    }
    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
            adjustAlly(event.getEntity());

    }
}
