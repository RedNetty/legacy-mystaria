package me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.bosses;

import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.WorldBoss;

public enum BossEnum {
    FROSTWING("Frost-Wing", Frostwing.class),
    CHRONOS("Chronos", Chronos.class);

    private final String displayName;
    private final Class<? extends WorldBoss> bossClass;

    BossEnum(String displayName, Class<? extends WorldBoss> bossClass) {
        this.displayName = displayName;
        this.bossClass = bossClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public WorldBoss getWorldBoss() {
        try {
            return bossClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}