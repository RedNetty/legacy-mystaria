package me.retrorealms.practiceserver.mechanics.mobs.boss;


import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.mobs.boss.abilties.AbilityHandler;
import me.retrorealms.practiceserver.mechanics.mobs.boss.abilties.BossAbility;
import me.retrorealms.practiceserver.mechanics.mobs.boss.drops.BossGearGenerator;
import me.retrorealms.practiceserver.mechanics.mobs.boss.drops.WorldBossDrops;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class WorldBoss {
    public List<BossAbility> bossAbilities;
    public Location spawnLocation;
    public String configName;
    public int starterHealth = 100;
    public int tier = 1;
    public EntityType entityType;
    public LivingEntity bossEntity;
    private long abilityCooldownEndTime;
    private BossAbility currentAbility;
    private boolean usingAbility;

    public WorldBoss(String configName, EntityType entityType, BossSpawnLocation spawnLocation) {
        this.spawnLocation = spawnLocation.getLocation();
        this.configName = configName;
        this.entityType = entityType;

        bossAbilities = AbilityHandler.createAbilities(WorldBossHandler.getBossFile().getStringList(configName + ".abilities"));

    }

    public boolean isUsingAbility() {
        return usingAbility;
    }

    public BossAbility getCurrentAbility() {
        return currentAbility;
    }

    public Location getBossSpawnLocation() {
        return spawnLocation;
    }

    public List<BossAbility> getBossAbilities() {
        return bossAbilities;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public int getHealth() {
        return bossEntity == null ? 0 : (int) bossEntity.getHealth();
    }


    public void loadBossGear(LivingEntity boss) {
        boss.getEquipment().setChestplate(WorldBossDrops.createDrop(6, configName));
        boss.getEquipment().setLeggings(WorldBossDrops.createDrop(7, configName));
        boss.getEquipment().setBoots(WorldBossDrops.createDrop(8, configName));
        boss.getEquipment().setItemInMainHand(WorldBossDrops.createDrop(BossGearGenerator.getWeaponType(configName), configName));
    }

    public String getDisplayName() {
        return WorldBossHandler.getBossFile().contains(configName + ".display")
                ? ChatColor.translateAlternateColorCodes('&',
                WorldBossHandler.getBossFile().getString(configName + ".display"))
                : "";
    }

    public void spawn() {
        if (!entityType.isAlive() || entityType == EntityType.ARMOR_STAND) return;
        bossEntity = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, entityType);
        new CreatureSpawnEvent(bossEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);

        bossEntity.setCustomName(getDisplayName());
        bossEntity.setCustomNameVisible(true);
        bossEntity.setMetadata("name", new FixedMetadataValue(PracticeServer.plugin, getDisplayName()));
        bossEntity.setMetadata("type", new FixedMetadataValue(PracticeServer.plugin, configName));

        loadBossGear(bossEntity);
        // Start a repeating task to check for abilities and activate them
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bossEntity.isValid() && !bossEntity.isDead()) {
                    long currentTime = System.currentTimeMillis();
                    if (!usingAbility && currentTime >= abilityCooldownEndTime) {
                        BossAbility nextAbility = getNextAbility();
                        if (nextAbility != null) {
                            currentAbility = nextAbility;
                            activateAbility(currentAbility);
                        }
                    }
                } else {
                    // Boss is dead or not valid, cancel the task
                    cancel();
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0, 20); // Adjust the interval as needed (20 ticks = 1 second)
    }

    private BossAbility getNextAbility() {
        long currentTime = System.currentTimeMillis();
        List<BossAbility> availableAbilities = new ArrayList<>();

        for (BossAbility ability : bossAbilities) {
            if (!ability.isOnCooldown()) {
                availableAbilities.add(ability);
            }
        }

        if (!availableAbilities.isEmpty()) {
            List<BossAbility> weightedAbilities = new ArrayList<>();

            for (BossAbility ability : availableAbilities) {
                int weight = ability.getPriority(); // Use priority as weight
                for (int i = 0; i < weight; i++) {
                    weightedAbilities.add(ability); // Add the ability to the list multiple times based on its weight
                }
            }

            int randomIndex = new Random().nextInt(weightedAbilities.size()); // Randomly select an index from the weighted abilities list
            return weightedAbilities.get(randomIndex); // Return the randomly selected ability
        }

        return null; // No available abilities to activate
    }


    private void activateAbility(BossAbility ability) {
        usingAbility = true;
        ability.use(bossEntity); // Assuming you have a target defined
        ability.activateCooldown(ability.getTotalCooldown()); // Set a cooldown after using the ability
        abilityCooldownEndTime = System.currentTimeMillis() + (ability.getTotalCooldown() + 400) * 50L;
        new BukkitRunnable() {
            @Override
            public void run() {
                usingAbility = false;
            }
        }.runTaskLater(PracticeServer.getInstance(), ability.getRunDuration()); // Set the ability duration
    }


}
