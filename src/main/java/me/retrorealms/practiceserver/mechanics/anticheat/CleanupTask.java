package me.retrorealms.practiceserver.mechanics.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CleanupTask extends BukkitRunnable {
    private final AdvancedAntiCheat antiCheat;

    public CleanupTask(AdvancedAntiCheat antiCheat) {
        this.antiCheat = antiCheat;
    }

    @Override
    public void run() {
        // Get all online player UUIDs
        Set<UUID> onlinePlayerUUIDs = Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toSet());

        // Remove data for offline players
        antiCheat.getPlayerDataMap().keySet().removeIf(uuid -> !onlinePlayerUUIDs.contains(uuid));

        // Clean up violation data for offline players
        antiCheat.getViolationManager().getViolationLevels().keySet().removeIf(uuid -> !onlinePlayerUUIDs.contains(uuid));
    }
}
