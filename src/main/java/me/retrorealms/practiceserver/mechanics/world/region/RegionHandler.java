package me.retrorealms.practiceserver.mechanics.world.region;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.retrorealms.practiceserver.mechanics.useless.task.AsyncTask;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Giovanni on 20-5-2017.
 */
public class RegionHandler {

    private static RegionHandler handler;

    private static List<ProtectedRegion> regionsToFlip = new ArrayList<>();

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



    public static void restoreRegionFlags() {
        if(regionsToFlip.isEmpty()) return;
        for (ProtectedRegion region : regionsToFlip) {
            region.setFlag(DefaultFlag.PVP,StateFlag.State.DENY);
        }

        regionsToFlip.clear();

    }
    public static void switchPvPFlagForRegions(World world) {
        WorldGuardPlugin worldGuardPlugin = WorldGuardPlugin.inst();

        List<ProtectedRegion> regions = getRegionsWithPvPDisabled(world);
        regionsToFlip.addAll(regions);
        for (ProtectedRegion region : regions) {
            region.setFlag(DefaultFlag.PVP,StateFlag.State.ALLOW);
        }

    }

    public static List<ProtectedRegion> getRegionsWithPvPDisabled(World world) {
        List<ProtectedRegion> pvpDisabledRegions = new ArrayList<>();

        WorldGuardPlugin worldGuardPlugin = WorldGuardPlugin.inst();
        RegionManager regionManager = worldGuardPlugin.getRegionManager(world);

        for (ProtectedRegion region : regionManager.getRegions().values()) {
            if (Objects.equals(region.getFlag(DefaultFlag.PVP), StateFlag.State.DENY)) {
                pvpDisabledRegions.add(region);
            }
        }

        return pvpDisabledRegions;
    }
}
