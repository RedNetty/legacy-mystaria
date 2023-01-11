package me.retrorealms.practiceserver.mechanics.world.region;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.retrorealms.practiceserver.mechanics.useless.task.AsyncTask;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Giovanni on 20-5-2017.
 */
public class RegionHandler {

    private static RegionHandler handler;

    private final ConcurrentHashMap<Region, CopyOnWriteArrayList<UUID>> regionMap = new ConcurrentHashMap<>();

    public void init() {

        new AsyncTask(() -> {

            Bukkit.getOnlinePlayers().forEach(player -> {
                ApplicableRegionSet regionSet = WGBukkit.getRegionManager(player.getLocation().getWorld()).getApplicableRegions(player.getLocation());
                regionSet.forEach(protectedRegion -> {

                    if (protectedRegion.getId().equalsIgnoreCase("tortmentedprison"))
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 10F, 0.01F);
                });
            });
        }).setInterval(15000L).scheduleRepeatingTask();
    }

    public boolean isRegion(ProtectedRegion region) {
        for (Region region1 : regionMap.keySet()) {
            return region1.name().equals(region.getId());
        }

        return false;
    }

    public boolean inRegion(Player player, Region region) {
        return regionMap.get(region).contains(player.getUniqueId());
    }

    public static RegionHandler getHandler() {
        return handler == null ? new RegionHandler() : handler;
    }
}
