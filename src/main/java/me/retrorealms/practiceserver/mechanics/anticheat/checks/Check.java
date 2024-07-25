package me.retrorealms.practiceserver.mechanics.anticheat.checks;

import me.retrorealms.practiceserver.mechanics.anticheat.AdvancedAntiCheat;
import me.retrorealms.practiceserver.mechanics.anticheat.ACPlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class Check {
    protected final AdvancedAntiCheat antiCheat;
    protected final String checkName;

    public Check(AdvancedAntiCheat antiCheat, String checkName) {
        this.antiCheat = antiCheat;
        this.checkName = checkName;
    }

    public abstract boolean isApplicable(Event event);
    public abstract void check(Player player, ACPlayerData data, Event event);

    protected void fail(Player player, double violationAmount) {
        antiCheat.getViolationManager().logViolation(player, checkName, violationAmount);
    }
}
