package me.retrorealms.practiceserver.mechanics.drops;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DropPriority implements Listener {

    private static final ConcurrentHashMap<UUID, Map<UUID, Double>> mobDamageTracking = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, DropInfo> itemDropInfo = new ConcurrentHashMap<>();

    private static class DropInfo {
        final UUID playerUUID;
        int timeRemaining;

        DropInfo(UUID playerUUID, int timeRemaining) {
            this.playerUUID = playerUUID;
            this.timeRemaining = timeRemaining;
        }
    }

    public void onEnable() {
        PracticeServer.log.info("[Drop Priority] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);

        new BukkitRunnable() {
            @Override
            public void run() {
                updateDropTimers();
            }
        }.runTaskTimerAsynchronously(PracticeServer.plugin, 0, 20);
    }

    public void onDisable() {
        PracticeServer.log.info("[DropPriority] has been disabled.");
    }

    public static Item dropItem(LivingEntity entity, Location location, ItemStack itemStack) {
        Item droppedItem = entity.getWorld().dropItemNaturally(location, itemStack);
        UUID entityUUID = entity.getUniqueId();

        if (mobDamageTracking.containsKey(entityUUID)) {
            Map<UUID, Double> damageMap = mobDamageTracking.get(entityUUID);
            UUID topDamagerUUID = damageMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (topDamagerUUID != null) {
                Player topDamager = Bukkit.getPlayer(topDamagerUUID);
                if (topDamager != null && topDamager.isOnline()) {
                    registerDropForPlayer(droppedItem, topDamager);
                    if (itemStack.getType() != Material.EMERALD) {
                        Mobdrops.dropShowString(topDamager, itemStack, entity);
                    }
                }
            }
        }

        return droppedItem;
    }

    private static void registerDropForPlayer(Item item, Player player) {
        int protectionTime = calculateProtectionTime(player);
        itemDropInfo.put(item.getUniqueId(), new DropInfo(player.getUniqueId(), protectionTime));
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        UUID itemUUID = item.getUniqueId();

        if (itemDropInfo.containsKey(itemUUID)) {
            DropInfo dropInfo = itemDropInfo.get(itemUUID);
            if (!dropInfo.playerUUID.equals(player.getUniqueId())) {
                event.setCancelled(true);
            } else {
                itemDropInfo.remove(itemUUID);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof Player) return;
        if (!(event.getDamager() instanceof Player)) return;

        LivingEntity mob = (LivingEntity) event.getEntity();
        Player damager = (Player) event.getDamager();
        UUID mobUUID = mob.getUniqueId();
        UUID damagerUUID = damager.getUniqueId();

        mobDamageTracking.computeIfAbsent(mobUUID, k -> new HashMap<>());
        Map<UUID, Double> damageMap = mobDamageTracking.get(mobUUID);
        damageMap.merge(damagerUUID, event.getFinalDamage(), Double::sum);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        mobDamageTracking.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (Toggles.isToggled(player, "Drop Protection")) {
            registerDropForPlayer(event.getItemDrop(), player);
        }
    }

    private static int calculateProtectionTime(Player player) {
        RankEnum rank = ModerationMechanics.getRank(player);
        switch (rank) {
            case DEFAULT:
                return 2;
            case SUB:
                return 5;
            case SUB1:
                return 8;
            case SUB2:
                return 15;
            default:
                return 30;
        }
    }

    private void updateDropTimers() {
        itemDropInfo.entrySet().removeIf(entry -> {
            DropInfo info = entry.getValue();
            info.timeRemaining--;
            return info.timeRemaining <= 0;
        });
    }
}