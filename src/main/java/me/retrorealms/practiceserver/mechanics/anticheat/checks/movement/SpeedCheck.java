package me.retrorealms.practiceserver.mechanics.anticheat.checks.movement;

import me.retrorealms.practiceserver.mechanics.anticheat.AdvancedAntiCheat;
import me.retrorealms.practiceserver.mechanics.anticheat.ACPlayerData;
import me.retrorealms.practiceserver.mechanics.anticheat.checks.Check;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class SpeedCheck extends Check {
    private static final double BASE_SPEED = 0.2873;
    private static final int BUFFER_SIZE = 10;
    private static final long VELOCITY_GRACE_PERIOD = 5000; // 1 second
    private static final int MAX_CONSECUTIVE_VIOLATIONS = 5; // Adjust this value as needed

    public SpeedCheck(AdvancedAntiCheat antiCheat) {
        super(antiCheat, "SPEED");
    }

    @Override
    public boolean isApplicable(Event event) {
        return event instanceof PlayerMoveEvent;
    }

    @Override
    public void check(Player player, ACPlayerData data, Event event) {
        PlayerMoveEvent moveEvent = (PlayerMoveEvent) event;
        if (isExempt(player, data)) {
            data.resetConsecutiveSpeedViolations();
            return;
        }

        Location from = moveEvent.getFrom();
        Location to = moveEvent.getTo();
        double distance = to.distance(from);
        double maxSpeed = getMaxAllowedSpeed(player, from, to);

        // Account for velocity changes
        Vector velocity = player.getVelocity();
        double velocityMagnitude = velocity.length();
        maxSpeed += velocityMagnitude;

        if (distance > maxSpeed) {
            data.addSpeedViolation(distance - maxSpeed);
            data.incrementConsecutiveSpeedViolations();

            if (data.getSpeedViolations().size() >= BUFFER_SIZE) {
                double averageViolation = data.getSpeedViolations().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                if (averageViolation > 0.1) {
                    fail(player, averageViolation);
                }
                data.clearSpeedViolations();
            }

            // Check if we should cancel the move event
            if (data.getConsecutiveSpeedViolations() >= MAX_CONSECUTIVE_VIOLATIONS) {
                moveEvent.setCancelled(true);
                player.teleport(from); // Teleport the player back to the previous location
                player.sendMessage("§cExcessive speed detected. Movement cancelled.");
            }
        } else {
            data.clearSpeedViolations();
            data.resetConsecutiveSpeedViolations();
        }

        // Update player state for future checks
        data.setLastSpeed(distance);
        data.setLastLocation(to);
    }

    private boolean isExempt(Player player, ACPlayerData data) {
        return player.isFlying() || player.isGliding() || player.isInsideVehicle() ||
                System.currentTimeMillis() - data.getLastTeleportTime() < 1000 ||
                player.getLocation().getBlock().getType() == Material.WATER ||
                player.getFallDistance() > 0 ||
                System.currentTimeMillis() - data.getLastVelocityChangeTime() < VELOCITY_GRACE_PERIOD;
    }

    private double getMaxAllowedSpeed(Player player, Location from, Location to) {
        double maxSpeed = BASE_SPEED;
        maxSpeed *= getMovementMultiplier(player);
        maxSpeed *= getPotionEffectMultiplier(player);
        maxSpeed *= getBlockEffectMultiplier(from, to);
        return maxSpeed;
    }

    private double getMovementMultiplier(Player player) {
        if (player.isSprinting()) return 1.3;
        if (player.isSneaking()) return 0.6;
        return 1.0;
    }

    private double getPotionEffectMultiplier(Player player) {
        PotionEffect speedEffect = player.getPotionEffect(PotionEffectType.SPEED);
        return speedEffect != null ? 1.0 + (speedEffect.getAmplifier() + 1) * 0.2 : 1.0;
    }

    private double getBlockEffectMultiplier(Location from, Location to) {
        double multiplier = 1.0;
        if (isOnIce(from) || isOnIce(to)) multiplier *= 2.5;
        if (isOnSoulSand(from) || isOnSoulSand(to)) multiplier *= 0.4;
        if (isInWeb(from) || isInWeb(to)) multiplier *= 0.05;
        return multiplier;
    }

    private boolean isOnIce(Location location) {
        Block block = location.getBlock().getRelative(0, -1, 0);
        return block.getType() == Material.ICE || block.getType() == Material.PACKED_ICE;
    }

    private boolean isOnSoulSand(Location location) {
        return location.getBlock().getRelative(0, -1, 0).getType() == Material.SOUL_SAND;
    }

    private boolean isInWeb(Location location) {
        return location.getBlock().getType() == Material.WEB;
    }
}