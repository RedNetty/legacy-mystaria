package me.retrorealms.practiceserver.mechanics.anticheat;

import me.retrorealms.practiceserver.mechanics.anticheat.checks.*;
import me.retrorealms.practiceserver.mechanics.anticheat.checks.combat.AimbotCheck;
import me.retrorealms.practiceserver.mechanics.anticheat.checks.combat.KillAuraCheck;
import me.retrorealms.practiceserver.mechanics.anticheat.checks.combat.ReachCheck;
import me.retrorealms.practiceserver.mechanics.anticheat.checks.movement.FlightCheck;
import me.retrorealms.practiceserver.mechanics.anticheat.checks.movement.NoClipCheck;
import me.retrorealms.practiceserver.mechanics.anticheat.checks.movement.SpeedCheck;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

public class CheckManager {
    private final AdvancedAntiCheat antiCheat;
    private final List<Check> checks;

    public CheckManager(AdvancedAntiCheat antiCheat) {
        this.antiCheat = antiCheat;
        this.checks = new ArrayList<>();
        registerChecks();
    }

    private void registerChecks() {
        checks.add(new SpeedCheck(antiCheat));
        checks.add(new FlightCheck(antiCheat));
        checks.add(new NoClipCheck(antiCheat));
        //checks.add(new KillAuraCheck(antiCheat));
        checks.add(new ReachCheck(antiCheat));
        checks.add(new AimbotCheck(antiCheat));
    }

    public void runChecks(Event event, Player player) {
        ACPlayerData data = antiCheat.getPlayerData(player);
        for (Check check : checks) {
            if (check.isApplicable(event)) {
                check.check(player, data, event);
            }
        }
    }
}
