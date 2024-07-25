package me.retrorealms.practiceserver.mechanics.anticheat;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class ViolationDecayTask extends BukkitRunnable {
    private final AdvancedAntiCheat antiCheat;

    public ViolationDecayTask(AdvancedAntiCheat antiCheat) {
        this.antiCheat = antiCheat;
    }

    @Override
    public void run() {
        for (UUID playerId : antiCheat.getViolationManager().getViolationLevels().keySet()) {
            antiCheat.getViolationManager().decayViolations(playerId);
        }
    }
}
