package me.retrorealms.practiceserver.mechanics.mobs;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Giovanni on 11-2-2017.
 */
public class NameFixer implements Listener {

    private final List<EntityType> checkedEntities = Arrays.asList(EntityType.ZOMBIE, EntityType.PIG_ZOMBIE,
            EntityType.SKELETON, EntityType.SLIME,
            EntityType.MAGMA_CUBE, EntityType.IRON_GOLEM,
            EntityType.SILVERFISH, EntityType.GHAST, EntityType.ARMOR_STAND);

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (this.checkedEntities.contains(event.getEntityType())) {
            event.getEntity().setCustomNameVisible(true);
        }
    }
}
