package me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public enum BossSpawnLocation {
    CASTLE(new Location(Bukkit.getWorld("jew"), 392, 45, -122)),
    DRAGONS_DEN(new Location(Bukkit.getWorld("jew"), 580, 5, 69)),
    TRIPOLI(new Location(Bukkit.getWorld("jew"), 872, 7, -39)),
    THE_BENEATH(new Location(Bukkit.getWorld("jew"), 605, 15, 352));

    public Location location;

    BossSpawnLocation(Location loc) {
        this.location = loc;
    }

    public Location getLocation() {
        return location;
    }
}
