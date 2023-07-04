package me.retrorealms.practiceserver.mechanics.mobs.boss.abilties;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class BossAbility {
    private final int priority;
    private final int runDuration;
    private final int totalCooldown;
    private long currentCooldown;

    public BossAbility(int priority, int cooldown, int runDuration) {
        this.totalCooldown = cooldown;
        this.priority = priority;
        this.runDuration = runDuration;
        this.currentCooldown = 0;
    }

    public int getPriority() {
        return priority;
    }

    public void activateCooldown(long cooldownMillis) {
        currentCooldown = System.currentTimeMillis() + cooldownMillis;
    }

    public boolean isOnCooldown() {
        return System.currentTimeMillis() < currentCooldown;
    }

    public int getTotalCooldown() {
        return totalCooldown;
    }

    public int getRunDuration() {
        return runDuration;
    }

    public abstract void use(LivingEntity boss);

    public abstract void use(Player target);

    public abstract void use(Location location);
}
