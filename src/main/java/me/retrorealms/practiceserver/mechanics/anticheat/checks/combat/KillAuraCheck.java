package me.retrorealms.practiceserver.mechanics.anticheat.checks.combat;

import me.retrorealms.practiceserver.mechanics.anticheat.AdvancedAntiCheat;
import me.retrorealms.practiceserver.mechanics.anticheat.ACPlayerData;
import me.retrorealms.practiceserver.mechanics.anticheat.checks.Check;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class KillAuraCheck extends Check {
    private static final int ATTACK_PATTERN_SIZE = 20;
    private static final double MAX_YAW_CHANGE = 180.0;
    private static final double MAX_PITCH_CHANGE = 180.0;
    private static final int MAX_CPS = 20;
    private static final int VIOLATION_THRESHOLD = 10;
    private static final long TARGET_SWITCH_TIME_THRESHOLD = 200; // milliseconds
    private static final int MAX_TARGETS_PER_SECOND = 5;

    private final Map<UUID, Integer> violationLevels = new HashMap<>();

    public KillAuraCheck(AdvancedAntiCheat antiCheat) {
        super(antiCheat, "KILLAURA");
    }

    @Override
    public boolean isApplicable(Event event) {
        return event instanceof EntityDamageByEntityEvent;
    }

    @Override
    public void check(Player player, ACPlayerData data, Event event) {
        EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
        Entity target = damageEvent.getEntity();

        long currentTime = System.currentTimeMillis();
        data.addAttackTime(currentTime);
        data.addAttackYaw(player.getLocation().getYaw());
        data.addAttackPitch(player.getLocation().getPitch());

        int cpsViolations = checkCPS(player, data);
        int rotationViolations = checkRotationConsistency(player, data);
        int targetSwitchingViolations = checkTargetSwitching(player, target, currentTime, data);
        int impossibleHitsViolations = checkImpossibleHits(player, target);

        int totalViolations = cpsViolations + rotationViolations + targetSwitchingViolations + impossibleHitsViolations;
        double confidence = calculateConfidence(cpsViolations, rotationViolations, targetSwitchingViolations, impossibleHitsViolations);

        if (totalViolations > 0) {
            int playerViolations = violationLevels.getOrDefault(player.getUniqueId(), 0) + totalViolations;
            violationLevels.put(player.getUniqueId(), playerViolations);

            if (playerViolations >= VIOLATION_THRESHOLD) {
                fail(player, playerViolations, confidence);
                violationLevels.put(player.getUniqueId(), 0);
            }
        } else {
            violationLevels.put(player.getUniqueId(), Math.max(0, violationLevels.getOrDefault(player.getUniqueId(), 0) - 1));
        }

        data.setLastAttackTime(currentTime);
    }

    private int checkCPS(Player player, ACPlayerData data) {
        Deque<Long> attackTimes = data.getAttackTimes();
        int attackCount = attackTimes.size();

        if (attackCount >= 2) {
            long timeFrame = attackTimes.peekLast() - attackTimes.peekFirst();
            double cps = (attackCount - 1) / (timeFrame / 1000.0);

            if (cps > MAX_CPS) {
                return (int) Math.ceil(cps - MAX_CPS);
            }
        }

        return 0;
    }

    private int checkRotationConsistency(Player player, ACPlayerData data) {
        List<Float> yaws = new ArrayList<>(data.getAttackYaws());
        List<Float> pitches = new ArrayList<>(data.getAttackPitches());
        if (yaws.size() < 3 || pitches.size() < 3) return 0;

        double yawVariance = calculateVariance(yaws);
        double pitchVariance = calculateVariance(pitches);

        int violations = 0;

        if (yawVariance < 0.1 && pitchVariance < 0.1) {
            violations += 3;
        }

        if (isRoboticPattern(yaws) || isRoboticPattern(pitches)) {
            violations += 2;
        }

        for (int i = 1; i < yaws.size(); i++) {
            float yawDiff = Math.abs(yaws.get(i) - yaws.get(i - 1));
            float pitchDiff = Math.abs(pitches.get(i) - pitches.get(i - 1));

            if (yawDiff > MAX_YAW_CHANGE || pitchDiff > MAX_PITCH_CHANGE) {
                violations += 1;
            }
        }

        return violations;
    }

    private int checkTargetSwitching(Player player, Entity currentTarget, long currentTime, ACPlayerData data) {
        List<ACPlayerData.EntityTargetInfo> playerTargets = data.getRecentTargets();

        playerTargets.removeIf(info -> currentTime - info.time > 1000);

        boolean isNewTarget = playerTargets.isEmpty() || !playerTargets.get(playerTargets.size() - 1).entityId.equals(currentTarget.getUniqueId());

        if (isNewTarget) {
            playerTargets.add(new ACPlayerData.EntityTargetInfo(currentTarget.getUniqueId(), currentTime));

            if (playerTargets.size() >= 2) {
                long timeSinceLastSwitch = currentTime - playerTargets.get(playerTargets.size() - 2).time;
                if (timeSinceLastSwitch < TARGET_SWITCH_TIME_THRESHOLD) {
                    return 2;
                }
            }

            if (playerTargets.size() > MAX_TARGETS_PER_SECOND) {
                return 3;
            }
        }

        return 0;
    }

    private int checkImpossibleHits(Player player, Entity target) {
        Vector toTarget = target.getLocation().toVector().subtract(player.getEyeLocation().toVector());
        Vector playerDirection = player.getLocation().getDirection();
        double angle = toTarget.angle(playerDirection);

        if (angle > Math.PI / 2) {
            return (int) Math.ceil(Math.toDegrees(angle - Math.PI / 2) / 10);
        }

        if (!player.hasLineOfSight(target)) {
            return 5;
        }

        return 0;
    }

    private double calculateConfidence(int cpsViolations, int rotationViolations, int targetSwitchingViolations, int impossibleHitsViolations) {
        double totalViolations = cpsViolations + rotationViolations + targetSwitchingViolations + impossibleHitsViolations;
        double maxViolations = MAX_CPS + 5 + 5 + 5;

        return Math.max(0, Math.min(100, ((maxViolations - totalViolations) / maxViolations) * 100));
    }

    private void fail(Player player, int violations, double confidence) {
        antiCheat.getViolationManager().logViolation(player, this.checkName, violations, confidence);
    }

    private double calculateVariance(List<Float> numbers) {
        double mean = numbers.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
        return numbers.stream().mapToDouble(x -> Math.pow(x - mean, 2)).average().orElse(0.0);
    }

    private boolean isRoboticPattern(List<Float> rotations) {
        if (rotations.size() < 4) return false;

        float diff1 = Math.abs(rotations.get(1) - rotations.get(0));
        float diff2 = Math.abs(rotations.get(2) - rotations.get(1));
        float diff3 = Math.abs(rotations.get(3) - rotations.get(2));

        return Math.abs(diff1 - diff2) < 0.1 && Math.abs(diff2 - diff3) < 0.1;
    }
}