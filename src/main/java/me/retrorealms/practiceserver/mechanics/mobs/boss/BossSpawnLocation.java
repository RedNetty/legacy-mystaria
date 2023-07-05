package me.retrorealms.practiceserver.mechanics.mobs.boss;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public enum BossSpawnLocation {
    CASTLE(new Location(Bukkit.getWorld("jew"), 392, 45, -122)),
    TRIPOLI(new Location(Bukkit.getWorld("jew"), 872, 7, -39)),
    AVALON(new Location(Bukkit.getWorld("jew"), 568, 10, 381));

    public Location location;

    BossSpawnLocation(Location loc) {
        this.location = loc;
    }

    public Location getLocation() {
        return location;
    }
}
