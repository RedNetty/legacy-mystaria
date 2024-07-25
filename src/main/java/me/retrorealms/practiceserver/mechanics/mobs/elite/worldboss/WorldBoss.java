package me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss;

import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.bosses.BossEnum;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public abstract class WorldBoss {

    public BossEnum bossEnum;
    private HashMap<UUID, Integer> damageDone = new HashMap<>();
    public List<ItemStack> drops = new ArrayList<>();
    public LivingEntity livingEntity = null;
    public String entityName = "";
    private Map<UUID, Long> playerDamageCooldowns = new HashMap<>();
    private static final long DAMAGE_COOLDOWN = 1500;
    public int tier = 5;

    public HashMap<UUID, Integer> getDamageDone() {
        return new HashMap<>(damageDone);
    }

    public void addDamage(Player player, int damage) {
        damageDone.merge(player.getUniqueId(), damage, Integer::sum);
    }
    public boolean canDamagePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        if (!playerDamageCooldowns.containsKey(playerId) || currentTime - playerDamageCooldowns.get(playerId) > DAMAGE_COOLDOWN) {
            playerDamageCooldowns.put(playerId, currentTime);
            return true;
        }
        return false;
    }
    public String getEntityName() {
        return entityName;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public abstract WorldBoss spawnBoss(Location location);

    public abstract void setArmor();

    public int getTier() {
        return tier;
    }

    public void explodeDrops(LivingEntity boss) {
        // Implement drop explosion logic here
    }

    public void rewardLoot() {
        // Implement loot rewarding logic here
    }

    public void clearDamage() {
        damageDone.clear();
    }

    public boolean isPlayerInvolved(Player player) {
        return damageDone.containsKey(player.getUniqueId());
    }

    public int getPlayerDamage(Player player) {
        return damageDone.getOrDefault(player.getUniqueId(), 0);
    }

    public void setBossEnum(BossEnum bossEnum) {
        this.bossEnum = bossEnum;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public BossEnum getBossEnum() {
        return bossEnum;
    }

    public void setLivingEntity(LivingEntity entity) {
        this.livingEntity = entity;
    }

    public void addDrop(ItemStack item) {
        this.drops.add(item);
    }

    public void clearDrops() {
        this.drops.clear();
    }

    public boolean isAlive() {
        return livingEntity != null && !livingEntity.isDead();
    }

    public Location getLocation() {
        return livingEntity != null ? livingEntity.getLocation() : null;
    }

    public void heal(double amount) {
        if (livingEntity != null) {
            double newHealth = Math.min(livingEntity.getHealth() + amount, livingEntity.getMaxHealth());
            livingEntity.setHealth(newHealth);
        }
    }

    public void damage(double amount) {
        if (livingEntity != null) {
            livingEntity.damage(amount);
        }
    }
    public WorldBoss(BossEnum bossEnum) {
        this.bossEnum = bossEnum;
    }
    public double getHealth() {
        return livingEntity != null ? livingEntity.getHealth() : 0;
    }

    public double getMaxHealth() {
        return livingEntity != null ? livingEntity.getMaxHealth() : 0;
    }

    public void remove() {
        if (livingEntity != null) {
            livingEntity.remove();
        }
        clearDamage();
        clearDrops();
    }
}