package me.retrorealms.practiceserver.mechanics.anticheat.checks.movement;

import me.retrorealms.practiceserver.mechanics.anticheat.AdvancedAntiCheat;
import me.retrorealms.practiceserver.mechanics.anticheat.ACPlayerData;
import me.retrorealms.practiceserver.mechanics.anticheat.checks.Check;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class ElytraFlightCheck extends Check {
    public ElytraFlightCheck(AdvancedAntiCheat antiCheat) {
        super(antiCheat, "ELYTRA_FLIGHT");
    }

    @Override
    public boolean isApplicable(Event event) {
        return event instanceof PlayerMoveEvent;
    }

    @Override
    public void check(Player player, ACPlayerData data, Event event) {
        if (player.isGliding()) {
            Vector velocity = player.getVelocity();
            double horizontalSpeed = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
            double verticalSpeed = velocity.getY();

            if (horizontalSpeed > 1.5 || verticalSpeed > 1.0) {
                fail(player, Math.max(horizontalSpeed - 1.5, verticalSpeed - 1.0));
            }
        }
    }
}