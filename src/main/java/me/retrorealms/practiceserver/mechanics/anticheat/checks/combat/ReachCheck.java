package me.retrorealms.practiceserver.mechanics.anticheat.checks.combat;

import me.retrorealms.practiceserver.mechanics.anticheat.AdvancedAntiCheat;
import me.retrorealms.practiceserver.mechanics.anticheat.ACPlayerData;
import me.retrorealms.practiceserver.mechanics.anticheat.checks.Check;
import me.retrorealms.practiceserver.mechanics.item.Items;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ReachCheck extends Check {
    private static final double BASE_REACH = 3.85;
    private static final int BUFFER_SIZE = 5;
    private static final double VIOLATION_THRESHOLD = 0.4;

    public ReachCheck(AdvancedAntiCheat antiCheat) {
        super(antiCheat, "REACH");
    }

    @Override
    public boolean isApplicable(Event event) {
        return event instanceof EntityDamageByEntityEvent;
    }

    @Override
    public void check(Player player, ACPlayerData data, Event event) {
        EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
        Entity target = damageEvent.getEntity();

        double distance = player.getLocation().distance(target.getLocation());
        double maxReach = getMaxReach(player);

        if (distance > maxReach) {
            data.addReachViolation(distance - maxReach);


            if (data.getReachViolations().size() >= BUFFER_SIZE) {
                double averageViolation = data.getReachViolations().stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);
                if (averageViolation > VIOLATION_THRESHOLD) {
                    fail(player, averageViolation);
                }
                data.clearReachViolations();
            }
            damageEvent.setDamage(0.0);
            damageEvent.setCancelled(true);

        } else {
            data.clearReachViolations();
        }
    }

    private double getMaxReach(Player player) {
        double reach = BASE_REACH;
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon != null) {
            reach += getWeaponReachBonus(weapon);
        }
        return reach;
    }

    private double getWeaponReachBonus(ItemStack weapon) {
        if (Items.isPolearm(weapon)) return 3.5;
        if (Items.isStaff(weapon)) return 9.0;
        return 0.0;
    }
}