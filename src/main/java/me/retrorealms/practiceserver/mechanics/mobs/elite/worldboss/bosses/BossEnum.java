package me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.bosses;

import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBoss;

public enum BossEnum {
    FROSTWING("Frost-Wing", new Frostwing());

    private WorldBoss worldBoss;

    private String displayName;

    public WorldBoss getWorldBoss() {
        return worldBoss;
    }
    public String getDisplayName() {
        return displayName;
    }

    private BossEnum(String displayName, WorldBoss worldBoss) {
        this.displayName = displayName;
        this.worldBoss  = worldBoss;
    }
}
