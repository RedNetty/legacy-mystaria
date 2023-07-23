package me.retrorealms.practiceserver.mechanics.player;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.commands.misc.ToggleEnergyCommand;
import me.retrorealms.practiceserver.mechanics.damage.Damage;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentHashMap;

public class Energy implements Listener {
    private static final int ENERGY_UPDATE_INTERVAL = 1;
    private static final int ENERGY_REDUCTION_INTERVAL = 4;
    private static final int MAX_ENERGY = 100;

    private static final float BASE_ENERGY_INCREASE = 100.0f;
    private static final float ENERGY_PER_ARMOR_PIECE = 7.5f;
    private static final float ENERGY_INCREASE_PER_INTEL = 0.02f;
    private static final int ENERGY_REDUCTION_AMOUNT = 85;
    private static final float ENERGY_REDUCTION_MULTIPLIER = 3.2f;
    private static final int ENERGY_REDUCTION_THRESHOLD = 0;
    private static final long ENERGY_REDUCTION_DELAY = 2000L;
    private static final int ENERGY_REDUCTION_POTION_DURATION = 40;
    private static final int ENERGY_REDUCTION_POTION_AMPLIFIER = 5;

    public static ConcurrentHashMap<String, Long> noDamage;
    public static ConcurrentHashMap<String, Long> cooldown;

    public Energy() {
        noDamage = new ConcurrentHashMap<>();
        cooldown = new ConcurrentHashMap<>();
    }

    public void onEnable() {
        PracticeServer.log.info("[Energy] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);

        // Update player energy every tick
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    PlayerInventory inventory = player.getInventory();
                    float energy = BASE_ENERGY_INCREASE;

                    for (ItemStack armorPiece : inventory.getArmorContents()) {
                        if (armorPiece != null && armorPiece.getType() != Material.AIR && armorPiece.hasItemMeta() && armorPiece.getItemMeta().hasLore()) {
                            int addedEnergy = Damage.getEnergy(armorPiece);
                            int addedIntel = Damage.getElem(armorPiece, "INT");

                            if (addedIntel > 0) {
                                addedEnergy += Math.round(addedIntel * ENERGY_INCREASE_PER_INTEL);
                            }

                            energy += addedEnergy * ENERGY_PER_ARMOR_PIECE;
                        }
                    }

                    if (getEnergy(player) < MAX_ENERGY && (!cooldown.containsKey(player.getName()) || System.currentTimeMillis() - cooldown.get(player.getName()) > ENERGY_REDUCTION_DELAY)) {
                        setEnergy(player, getEnergy(player) + energy / MAX_ENERGY);
                    }

                    if (getEnergy(player) <= ENERGY_REDUCTION_THRESHOLD) {
                        player.setSprinting(false);
                    }
                }
            }
        }.runTaskTimerAsynchronously(PracticeServer.plugin, 0, ENERGY_UPDATE_INTERVAL);

        // Reduce player energy when sprinting
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (!player.isSprinting() || Alignments.isSafeZone(player.getLocation()) || ToggleEnergyCommand.energytoggled.contains(player.getName())) {
                        continue;
                    }

                    float energyReduction = BASE_ENERGY_INCREASE + ENERGY_REDUCTION_AMOUNT;
                    energyReduction *= ENERGY_REDUCTION_MULTIPLIER;

                    if (getEnergy(player) > ENERGY_REDUCTION_THRESHOLD) {
                        setEnergy(player, getEnergy(player) - energyReduction / MAX_ENERGY);
                    }

                    if (getEnergy(player) <= ENERGY_REDUCTION_THRESHOLD) {
                        setEnergy(player, ENERGY_REDUCTION_THRESHOLD);
                        cooldown.put(player.getName(), System.currentTimeMillis());
                        slowDig(player);
                    }
                }
            }
        }.runTaskTimerAsynchronously(PracticeServer.plugin, 4, ENERGY_REDUCTION_INTERVAL);
    }

    public void slowDig(Player player) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PracticeServer.plugin, () -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, ENERGY_REDUCTION_POTION_DURATION, ENERGY_REDUCTION_POTION_AMPLIFIER), true);
            player.setSprinting(false);
        });
    }

    public void onDisable() {
        PracticeServer.log.info("[Energy] has been disabled.");
    }

    public static float getEnergy(Player player) {
        return player.getExp() * MAX_ENERGY;
    }

    public static void setEnergy(Player player, float energy) {
        if (energy > MAX_ENERGY) {
            energy = MAX_ENERGY;
        }

        if (energy / MAX_ENERGY < ENERGY_REDUCTION_THRESHOLD) {
            player.setExp(0);
        } else {
            player.setExp(energy / MAX_ENERGY);
        }

        player.setLevel((int) energy);
    }

    public static void removeEnergy(Player player, int amount) {
        if (Alignments.isSafeZone(player.getLocation()) || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (player.hasMetadata("lastenergy") && System.currentTimeMillis() - player.getMetadata("lastenergy").get(0).asLong() < ENERGY_UPDATE_INTERVAL) {
            return;
        }

        if (ToggleEnergyCommand.energytoggled.contains(player.getName())) {
            return;
        }

        player.setMetadata("lastenergy", new FixedMetadataValue(PracticeServer.plugin, System.currentTimeMillis()));
        setEnergy(player, getEnergy(player) - amount);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnergyUse(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (noDamage.containsKey(player.getName()) && System.currentTimeMillis() - noDamage.get(player.getName()) < ENERGY_UPDATE_INTERVAL) {
                event.setUseItemInHand(Event.Result.DENY);
                event.setCancelled(true);
                return;
            }

            int amount = 6;
            if (getEnergy(player) > ENERGY_REDUCTION_THRESHOLD) {
                if (itemInHand != null && itemInHand.getItemMeta() != null && itemInHand.getItemMeta().getDisplayName() != null) {
                    Material handType = itemInHand.getType();

                    if (handType == Material.WOOD_SWORD) {
                        amount = 2;
                    } else if (handType == Material.WOOD_AXE || handType == Material.WOOD_SPADE || handType == Material.STONE_SWORD) {
                        amount = 3;
                    } else if (handType == Material.STONE_AXE || handType == Material.STONE_SPADE || handType == Material.IRON_SWORD) {
                        amount = 4;
                    } else if (handType == Material.IRON_AXE || handType == Material.IRON_SPADE || handType == Material.DIAMOND_SWORD) {
                        amount = 5;
                    } else if (handType == Material.DIAMOND_AXE || handType == Material.DIAMOND_SPADE || handType == Material.GOLD_SWORD) {
                        amount = 6;
                    } else if (handType == Material.GOLD_AXE || handType == Material.GOLD_SPADE) {
                        amount = 7;
                    }

                    removeEnergy(player, amount);
                }
            }

            if (getEnergy(player) <= ENERGY_REDUCTION_THRESHOLD) {
                setEnergy(player, ENERGY_REDUCTION_THRESHOLD);
                cooldown.put(player.getName(), System.currentTimeMillis());
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, ENERGY_REDUCTION_POTION_DURATION, ENERGY_REDUCTION_POTION_AMPLIFIER), true);
                player.playSound(player.getLocation(), Sound.ENTITY_WOLF_PANT, 10.0f, 1.5f);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnergyUseDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
            Player player = (Player) event.getDamager();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (noDamage.containsKey(player.getName()) && System.currentTimeMillis() - noDamage.get(player.getName()) < ENERGY_UPDATE_INTERVAL) {
                event.setCancelled(true);
                event.setDamage(0.0);
                return;
            }

            if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
                event.setDamage(0.0);
                event.setCancelled(true);
            }

            noDamage.put(player.getName(), System.currentTimeMillis());

            if (getEnergy(player) > ENERGY_REDUCTION_THRESHOLD) {
                if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
                    player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                }

                int amount = 6;
                if (itemInHand != null && itemInHand.getItemMeta() != null && itemInHand.getItemMeta().getDisplayName() != null) {
                    Material handType = itemInHand.getType();

                    if (handType == Material.WOOD_HOE || handType == Material.WOOD_SPADE || handType == Material.WOOD_SWORD) {
                        amount = 7;
                    } else if (handType == Material.STONE_HOE || handType == Material.STONE_SPADE || handType == Material.WOOD_AXE || handType == Material.STONE_SWORD) {
                        amount = 8;
                    } else if (handType == Material.STONE_AXE || handType == Material.IRON_HOE || handType == Material.IRON_AXE || handType == Material.IRON_SPADE) {
                        amount = 9;
                    } else if (!itemInHand.getItemMeta().getDisplayName().contains(ChatColor.BLUE.toString()) && (handType == Material.DIAMOND_SWORD || handType == Material.DIAMOND_HOE || handType == Material.DIAMOND_SPADE) || handType == Material.IRON_AXE) {
                        amount = 10;
                    } else if (handType == Material.GOLD_HOE || handType == Material.GOLD_SPADE || handType == Material.GOLD_SWORD || (!itemInHand.getItemMeta().getDisplayName().contains(ChatColor.BLUE.toString()) && handType == Material.DIAMOND_AXE)) {
                        amount = 11;
                    } else if (handType == Material.GOLD_AXE || handType == Material.DIAMOND_SWORD || handType == Material.DIAMOND_HOE || handType == Material.DIAMOND_SPADE) {
                        amount = 12;
                    } else if (handType == Material.DIAMOND_AXE) {
                        amount = 13;
                    }

                    removeEnergy(player, amount);
                }
            }

            if (getEnergy(player) <= ENERGY_REDUCTION_THRESHOLD) {
                setEnergy(player, ENERGY_REDUCTION_THRESHOLD);
                cooldown.put(player.getName(), System.currentTimeMillis());
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, ENERGY_REDUCTION_POTION_DURATION, ENERGY_REDUCTION_POTION_AMPLIFIER), true);
                player.playSound(player.getLocation(), Sound.ENTITY_WOLF_PANT, 10.0f, 1.5f);
            }
        }
    }
}
