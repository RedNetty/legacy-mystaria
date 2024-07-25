package me.retrorealms.practiceserver.mechanics.anticheat;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

@Getter
@Setter
public class ACPlayerData {
    private final UUID uuid;
    private long lastKnockbackTime;
    private final Deque<Location> locationHistory;
    private final Deque<Long> attackTimes;
    private final Deque<Float> attackYaws;
    private final Deque<Float> attackPitches;
    private long lastAttackTime;
    private int attackCount;
    private long lastMultiAuraCheck;
    private long lastTeleportTime;
    private Location lastTeleportLocation;
    private Vector previousVelocity;
    private long lastOnGroundTime;
    private int criticalHits;
    private final Deque<Double> aimSamples;
    private int airTicks;
    private long lastNoClipCheck;
    private int failedNoClipChecks;
    private final List<Double> speedViolations;
    private final List<Double> reachViolations;
    private final List<double[]> flightSamples;
    private double lastSpeed;
    private Location lastLocation;
    private long lastVelocityChangeTime;
    private int consecutiveSpeedViolations;
    private int consecutiveReachViolations;
    private final List<EntityTargetInfo> recentTargets;

    private static final int MAX_HISTORY = 20;
    private static final int MAX_ATTACK_TIMES = 40;

    public ACPlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.locationHistory = new LinkedList<>();
        this.attackTimes = new LinkedList<>();
        this.attackYaws = new LinkedList<>();
        this.attackPitches = new LinkedList<>();
        this.aimSamples = new LinkedList<>();
        this.speedViolations = new ArrayList<>();
        this.reachViolations = new ArrayList<>();
        this.flightSamples = new ArrayList<>();
        this.recentTargets = new ArrayList<>();
    }

    public void addLocation(Location location) {
        locationHistory.addLast(location);
        if (locationHistory.size() > MAX_HISTORY) {
            locationHistory.removeFirst();
        }
    }

    public void addAimSample(double aim) {
        aimSamples.addLast(aim);
        if (aimSamples.size() > MAX_HISTORY) {
            aimSamples.removeFirst();
        }
    }

    public void addAttackTime(long time) {
        attackTimes.addLast(time);
        if (attackTimes.size() > MAX_ATTACK_TIMES) {
            attackTimes.removeFirst();
        }
    }

    public void addAttackYaw(float yaw) {
        attackYaws.addLast(yaw);
        if (attackYaws.size() > MAX_HISTORY) {
            attackYaws.removeFirst();
        }
    }

    public void addAttackPitch(float pitch) {
        attackPitches.addLast(pitch);
        if (attackPitches.size() > MAX_HISTORY) {
            attackPitches.removeFirst();
        }
    }

    public void addSpeedViolation(double violation) {
        speedViolations.add(violation);
    }

    public void addReachViolation(double violation) {
        reachViolations.add(violation);
    }

    public void addFlightSample(double[] sample) {
        flightSamples.add(sample);
    }

    public void clearSpeedViolations() {
        speedViolations.clear();
    }

    public void clearReachViolations() {
        reachViolations.clear();
    }

    public void clearFlightSamples() {
        flightSamples.clear();
    }

    public void incrementAirTicks() {
        airTicks++;
    }

    public void resetAirTicks() {
        airTicks = 0;
    }

    public void incrementFailedNoClipChecks() {
        failedNoClipChecks++;
    }

    public void resetFailedNoClipChecks() {
        failedNoClipChecks = 0;
    }

    public void incrementAttackCount() {
        attackCount++;
    }

    public void setLastKnockbackTime(long time) {
        lastKnockbackTime = time;
        lastVelocityChangeTime = time;
    }

    public void resetAttackCount() {
        attackCount = 0;
    }

    public void setLastVelocityChangeTime(long time) {
        this.lastVelocityChangeTime = time;
    }

    public long getLastVelocityChangeTime() {
        return this.lastVelocityChangeTime;
    }

    public void incrementConsecutiveSpeedViolations() {
        this.consecutiveSpeedViolations++;
    }

    public void resetConsecutiveSpeedViolations() {
        this.consecutiveSpeedViolations = 0;
    }

    public void incrementConsecutiveReachViolations() {
        this.consecutiveReachViolations++;
    }
    public boolean isExempt(Player player) {
        return player.isFlying() || player.isGliding() || player.isInsideVehicle() ||
                System.currentTimeMillis() - getLastTeleportTime() < 1000 ||
                player.getLocation().getBlock().getType() == Material.WATER ||
                player.getFallDistance() > 0 ||
                System.currentTimeMillis() - getLastVelocityChangeTime() < 10000;
    }
    public void resetConsecutiveReachViolations() {
        this.consecutiveReachViolations = 0;
    }

    public static class EntityTargetInfo {
        public UUID entityId;
        public long time;

        public EntityTargetInfo(UUID entityId, long time) {
            this.entityId = entityId;
            this.time = time;
        }
    }
}