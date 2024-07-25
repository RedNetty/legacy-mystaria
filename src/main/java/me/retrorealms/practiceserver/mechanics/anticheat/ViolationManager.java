package me.retrorealms.practiceserver.mechanics.anticheat;

import lombok.Getter;
import lombok.Setter;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ViolationManager {
    private final PracticeServer plugin;
    private final Map<UUID, Map<String, Integer>> violationLevels;

    public ViolationManager(PracticeServer plugin) {
        this.plugin = plugin;
        this.violationLevels = new HashMap<>();
    }

    public void decayViolations(UUID playerId) {
        Map<String, Integer> playerViolations = violationLevels.get(playerId);
        if (playerViolations != null) {
            for (Map.Entry<String, Integer> entry : playerViolations.entrySet()) {
                String checkType = entry.getKey();
                int violationLevel = entry.getValue();
                if (violationLevel > 0) {
                    playerViolations.put(checkType, violationLevel - 1);
                }
            }
        }
    }
    public void resetViolations(UUID playerId) {
        violationLevels.remove(playerId);
    }

    public void resetViolations(UUID playerId, String checkType) {
        Map<String, Integer> playerViolations = violationLevels.get(playerId);
        if (playerViolations != null) {
            playerViolations.remove(checkType);
        }
    }
    public void sendViolationLog(Player player, String checkType, double violationAmount, double confidence) {
        UUID playerId = player.getUniqueId();
        Map<String, Integer> playerViolations = violationLevels.computeIfAbsent(playerId, k -> new HashMap<>());
        int newViolationLevel = playerViolations.getOrDefault(checkType, 0) + (int) Math.ceil(violationAmount);
        playerViolations.put(checkType, newViolationLevel);

        String violationString = confidence >= 0 ? "(VL: " + newViolationLevel + "| Confidence = " + confidence + "%)" : "(VL: " + newViolationLevel + ")";
        plugin.getLogger().warning(player.getName() + " failed " + checkType + " check " + violationString);

        notifyAdmins(player, checkType, newViolationLevel);

        if (newViolationLevel >= 50) {
            punishPlayer(player, checkType);
        }
    }
    public void logViolation(Player player, String checkType, double violationAmount) {
        sendViolationLog(player, checkType, violationAmount, 0);
    }
    public void logViolation(Player player, String checkType, double violationAmount, double confidence) {
        sendViolationLog(player, checkType, violationAmount, confidence);
    }
    private void notifyAdmins(Player violator, String checkType, int violationLevel) {
        String message = "&c[RedCheat] &b" + violator.getName() + " &cfailed &b" + checkType + "&c check (VL: &e" + violationLevel + "&c)";
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("anticheat.notify")) {
                StringUtil.sendCenteredMessage(admin, message);
            }
        }
    }

    private void punishPlayer(Player player, String checkType) {
        // Implement punishment logic
        // Bukkit.getScheduler().runTask(plugin, () -> player.kickPlayer("[RedCheat] You were detected for " + checkType + " related activity."));
    }
}
