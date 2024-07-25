package me.retrorealms.practiceserver.mechanics.anticheat.checks.movement;

import me.retrorealms.practiceserver.mechanics.anticheat.AdvancedAntiCheat;
import me.retrorealms.practiceserver.mechanics.anticheat.ACPlayerData;
import me.retrorealms.practiceserver.mechanics.anticheat.checks.Check;
import me.retrorealms.practiceserver.mechanics.player.Mounts.Horses;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.Queue;

public class FlightCheck extends Check {
    private static final int MAX_AIRTIME = 100; // Maximum ticks allowed in air
    private static final double MAX_VERTICAL_SPEED = 1;
    private static final double MIN_FALL_SPEED = -3.5; // Terminal velocity in Minecraft
    private static final int BUFFER_SIZE = 10;
    private static final double GRAVITY = 0.08;
    private static final double AIR_FRICTION = 0.98;

    private final Queue<Double> verticalSpeedBuffer = new LinkedList<>();

    public FlightCheck(AdvancedAntiCheat antiCheat) {
        super(antiCheat, "FLIGHT");
    }

    @Override
    public boolean isApplicable(Event event) {
        return event instanceof PlayerMoveEvent;
    }

    @Override
    public void check(Player player, ACPlayerData data, Event event) {
        PlayerMoveEvent moveEvent = (PlayerMoveEvent) event;
        Location to = moveEvent.getTo();
        Location from = moveEvent.getFrom();

        if (isExempt(player, data)) {
            data.resetAirTicks();
            return;
        }

        double deltaY = to.getY() - from.getY();
        Vector velocity = player.getVelocity();

        boolean cancel = false;

        cancel |= checkAirTime(player, data);
        cancel |= checkVerticalSpeed(player, data, deltaY, velocity);
        cancel |= checkGlidingSpeed(player, data, from, to);
        cancel |= checkNoFall(player, data, deltaY);

        if (cancel) {
            handleViolation(player, moveEvent);
        } else {
            updatePlayerState(player, data, to);
        }
    }

    private boolean isExempt(Player player, ACPlayerData data) {
        return data.isExempt(player) || player.isFlying() || player.isInsideVehicle() || player.getLocation().add(0, -1, 0).getBlock().isLiquid() || player.getLocation().getBlock().isLiquid() ||
                System.currentTimeMillis() - data.getLastTeleportTime() < 1000 ||
                player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR ||
                isNearGround(player) || Horses.mounting.containsKey(player.getName()) ||
                player.hasPotionEffect(PotionEffectType.LEVITATION);
    }

    private boolean isNearGround(Player player) {
        Location loc = player.getLocation();
        for (int i = 0; i <= 3; i++) {
            if (loc.clone().subtract(0, i, 0).getBlock().getType().isSolid()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkAirTime(Player player, ACPlayerData data) {
        if (!isNearGround(player)) {
            data.incrementAirTicks();
            if (data.getAirTicks() > MAX_AIRTIME && !player.isGliding()) {
                return true;
            }
        } else {
            data.resetAirTicks();
        }
        return false;
    }

    private boolean checkVerticalSpeed(Player player, ACPlayerData data, double deltaY, Vector velocity) {
        double expectedDeltaY = (velocity.getY() - GRAVITY) * AIR_FRICTION;
        double difference = Math.abs(deltaY - expectedDeltaY);

        verticalSpeedBuffer.offer(difference);
        if (verticalSpeedBuffer.size() > BUFFER_SIZE) {
            verticalSpeedBuffer.poll();
        }

        double averageDifference = verticalSpeedBuffer.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        if (averageDifference > MAX_VERTICAL_SPEED && !player.isGliding()) {
            return true;
        }

        return false;
    }

    private boolean checkGlidingSpeed(Player player, ACPlayerData data, Location from, Location to) {
        if (player.isGliding()) {
            double distance = from.distance(to);
            double maxDistance = 1.95; // Maximum elytra speed per tick

            if (distance > maxDistance) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNoFall(Player player, ACPlayerData data, double deltaY) {
        if (data.getAirTicks() > 5 && deltaY == 0) {
            return true;
        }

        if (deltaY < MIN_FALL_SPEED) {
            return true;
        }

        return false;
    }

    private void handleViolation(Player player, PlayerMoveEvent moveEvent) {
        //teleportToGround(player);
        player.setVelocity(new Vector(0, 0, 0));
        //player.sendMessage(ChatColor.RED + "Suspicious movement detected.");
        fail(player, 10);
    }

    private void updatePlayerState(Player player, ACPlayerData data, Location to) {
        data.setLastLocation(to);
    }

    private void teleportToGround(Player player) {
        Location groundLoc = player.getLocation();
        while (!groundLoc.getBlock().getType().isSolid() && groundLoc.getY() > 0) {
            groundLoc.subtract(0, 1, 0);
        }
        if (groundLoc.getY() > 0) {
            groundLoc.add(0, 1, 0);
            player.teleport(groundLoc);
        }
    }
}