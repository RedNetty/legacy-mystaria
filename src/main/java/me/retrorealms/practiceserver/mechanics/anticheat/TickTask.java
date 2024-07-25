package me.retrorealms.practiceserver.mechanics.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TickTask extends BukkitRunnable {
    private final AdvancedAntiCheat antiCheat;

    public TickTask(AdvancedAntiCheat antiCheat) {
        this.antiCheat = antiCheat;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ACPlayerData data = antiCheat.getPlayerData(player);
            updatePlayerData(player, data);
        }
    }

    private void updatePlayerData(Player player, ACPlayerData data) {
        data.setLastLocation(player.getLocation());
        data.setPreviousVelocity(player.getVelocity());

        if (player.isOnGround()) {
            data.setLastOnGroundTime(System.currentTimeMillis());
        }

        // Update other time-sensitive data as needed
    }
}
