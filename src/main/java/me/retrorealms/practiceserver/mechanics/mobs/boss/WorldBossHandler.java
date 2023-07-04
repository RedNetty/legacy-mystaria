package me.retrorealms.practiceserver.mechanics.mobs.boss;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;

public class WorldBossHandler implements Listener {
    public static FileConfiguration config;
    public static File file;

    public static void onLoad() {
        PracticeServer.getInstance().saveResource("WorldBoss.yml", false);
        try {
            file = new File(PracticeServer.getInstance().getDataFolder(), "WorldBoss.yml");

            config = new YamlConfiguration().loadConfiguration(file);
        } catch (Exception e) {

        }
    }

    public static FileConfiguration getBossFile() {
        return config;
    }
}
