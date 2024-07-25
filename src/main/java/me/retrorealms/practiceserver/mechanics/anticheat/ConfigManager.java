package me.retrorealms.practiceserver.mechanics.anticheat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import me.retrorealms.practiceserver.PracticeServer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final PracticeServer plugin;
    private FileConfiguration config;
    private File configFile;
    private final Map<String, Object> defaults;

    public ConfigManager(PracticeServer plugin) {
        this.plugin = plugin;
        this.defaults = new HashMap<>();
        setDefaults();
        loadConfig();
    }

    private void setDefaults() {
        defaults.put("checks.flight.enabled", true);
        defaults.put("checks.flight.max-air-ticks", 20);
        defaults.put("checks.speed.enabled", true);
        defaults.put("checks.speed.max-speed", 0.8);
        defaults.put("checks.killaura.enabled", true);
        defaults.put("checks.killaura.max-cps", 20);
        defaults.put("checks.reach.enabled", true);
        defaults.put("checks.reach.max-reach", 3.5);
        defaults.put("violation.decay-rate", 1);
        defaults.put("violation.decay-interval", 60);
    }

    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "anticheat.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        setDefaults();

        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            if (!config.contains(entry.getKey())) {
                config.set(entry.getKey(), entry.getValue());
            }
        }

        saveConfig();
    }

    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            config.save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save config to " + configFile);
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public boolean isCheckEnabled(String checkName) {
        return getConfig().getBoolean("checks." + checkName + ".enabled", true);
    }

    public double getDoubleValue(String path) {
        return getConfig().getDouble(path, (double) defaults.get(path));
    }

    public int getIntValue(String path) {
        return getConfig().getInt(path, (int) defaults.get(path));
    }
}
