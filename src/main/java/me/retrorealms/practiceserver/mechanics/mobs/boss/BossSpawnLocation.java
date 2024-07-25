package me.retrorealms.practiceserver.mechanics.mobs.boss;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public enum BossSpawnLocation {
    CASTLE(new Location(Bukkit.getWorld("jew"), 392, 45, -122)),
    TRIPOLI(new Location(Bukkit.getWorld("jew"), 872, 7, -39)),
    PSILOCYLAND(new Location(Bukkit.getWorld("jew"), 864, 23, -287));

    public Location location;

    BossSpawnLocation(Location loc) {
        this.location = loc;
    }

    public Location getLocation() {
        return location;
    }
}
