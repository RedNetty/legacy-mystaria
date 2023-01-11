package me.retrorealms.practiceserver.mechanics.pets.handlers;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Khalid on 8/9/2017.
 */
public class RainbowSheepTask {

    private static RainbowSheepTask i;

    public static RainbowSheepTask getInstance() {
        if (i == null)
            i = new RainbowSheepTask();
        return i;
    }

    private List<Entity> list = new ArrayList<>();
    private List<DyeColor> colors = Arrays.asList(DyeColor.values());

    public void init() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PracticeServer.getInstance(), () -> {
            list.forEach(entity -> {
                if (entity.isDead())
                    list.remove(entity);
                if (entity.getType() != EntityType.SHEEP)
                    list.remove(entity);
                Sheep sheep = (Sheep) entity;
                int index = ThreadLocalRandom.current().nextInt(0, colors.size());
                sheep.setColor(colors.get(index));
            });
        }, 10, 10);
    }

    public void register(Entity e) {
        list.add(e);
    }

    public void remove(Entity e) {
        list.removeIf(entity -> entity.getUniqueId().toString().equals(e.getUniqueId().toString()));
    }

}
