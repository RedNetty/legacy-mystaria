package me.retrorealms.practiceserver.mechanics.anticheat.checks.combat;

import me.retrorealms.practiceserver.mechanics.anticheat.AdvancedAntiCheat;
import me.retrorealms.practiceserver.mechanics.anticheat.ACPlayerData;
import me.retrorealms.practiceserver.mechanics.anticheat.checks.Check;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.Deque;

public class AimbotCheck extends Check {
    private static final int SAMPLE_SIZE = 20;
    private static final double ANGLE_THRESHOLD = 0.2;
    private static final double CONSISTENCY_THRESHOLD = 0.9;

    public AimbotCheck(AdvancedAntiCheat antiCheat) {
        super(antiCheat, "AIMBOT");
    }

    @Override
    public boolean isApplicable(Event event) {
        return event instanceof EntityDamageByEntityEvent;
    }

    @Override
    public void check(Player player, ACPlayerData data, Event event) {
        EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
        Entity target = damageEvent.getEntity();

        Vector toTarget = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
        Vector playerDirection = player.getLocation().getDirection();
        double angle = toTarget.angle(playerDirection);

        data.addAimSample(angle);

        Deque<Double> aimSamples = data.getAimSamples();
        if (aimSamples.size() >= SAMPLE_SIZE) {
            checkAimbot(player, aimSamples);
        }
    }

    private void checkAimbot(Player player, Deque<Double> aimSamples) {
        double averageAngle = aimSamples.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = aimSamples.stream().mapToDouble(a -> Math.pow(a - averageAngle, 2)).average().orElse(0);

        int consistentAngles = 0;
        for (double angle : aimSamples) {
            if (Math.abs(angle - averageAngle) < ANGLE_THRESHOLD) {
                consistentAngles++;
            }
        }

        double consistency = (double) consistentAngles / SAMPLE_SIZE;

        if (variance < 0.0001 && consistency > CONSISTENCY_THRESHOLD) {
            fail(player, consistency);
        }
    }
}
