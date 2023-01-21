package me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss;

import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.bosses.BossEnum;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class WorldBoss {

    public BossEnum bossEnum = BossEnum.FROSTWING;
    private HashMap<UUID, Integer> damageDone = new HashMap<>();
    public List<ItemStack> drops = new ArrayList<>();
    public LivingEntity livingEntity = null;
    public String entityName = "";

    public int tier = 5;

    public HashMap<UUID, Integer> getDamageDone() {
        return damageDone;
    }

    public WorldBoss spawnBoss(Location location) {
        return this;
    }

    public void explodeDrops(LivingEntity boss) {}

    public void rewardLoot() {

    }
    public int getTier() {
        return tier;
    }

    public void addDamage(Player player, int damage) {
        if(damageDone.containsKey(player.getUniqueId())) {
            damageDone.put(player.getUniqueId(), damage + damageDone.get(player.getUniqueId()));
        }else{
            damageDone.put(player.getUniqueId(), damage);
        }
    }

    public String getEntityName() {
        return entityName;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }
}
