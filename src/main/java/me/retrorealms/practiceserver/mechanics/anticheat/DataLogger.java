package me.retrorealms.practiceserver.mechanics.anticheat;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataLogger {
    private final PracticeServer plugin;
    private final File logFolder;
    private final SimpleDateFormat dateFormat;

    public DataLogger(PracticeServer plugin) {
        this.plugin = plugin;
        this.logFolder = new File(plugin.getDataFolder(), "anticheat_logs");
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public void logViolation(Player player, String checkName, double violationAmount) {
        String logMessage = String.format("[%s] %s failed %s check (VL: %.2f)",
                dateFormat.format(new Date()),
                player.getName(),
                checkName,
                violationAmount);

        writeToFile("violations.log", logMessage);
    }

    public void logPlayerData(Player player, ACPlayerData data) {
        String logMessage = String.format("[%s] Player: %s, Location: %s, Speed: %.2f, AirTicks: %d",
                dateFormat.format(new Date()),
                player.getName(),
                data.getLastLocation(),
                data.getLastSpeed(),
                data.getAirTicks());

        writeToFile("player_data.log", logMessage);
    }

    private void writeToFile(String fileName, String message) {
        File logFile = new File(logFolder, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write to log file: " + e.getMessage());
        }
    }
}
