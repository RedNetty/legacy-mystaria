package me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public enum BossSpawnLocation {
    CASTLE(new Location(Bukkit.getWorld("jew"), 392, 45, -122)),
    AVALON_ENTRANCE(new Location(Bukkit.getWorld("jew"), 638, 5, 68)),
    TRIPOLI(new Location(Bukkit.getWorld("jew"), 872, 7, -39)),
    AVALON_BRIDGE(new Location(Bukkit.getWorld("jew"), 612, 78, 369));

    public Location location;

    BossSpawnLocation(Location loc) {
        this.location = loc;
    }

    public Location getLocation() {
        return location;
    }
}
