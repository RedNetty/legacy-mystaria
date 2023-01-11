package me.retrorealms.practiceserver.utils;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Created by Khalid on 8/3/2017.
 */
public class AbstractFile {
    protected PracticeServer main;
    protected File f;
    protected FileConfiguration c;

    public AbstractFile(PracticeServer main, String name, boolean yml) {
        this.main = main;
        this.f = new File(main.getDataFolder(), name);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (yml)
            c = YamlConfiguration.loadConfiguration(f);
    }

    public void save() {
        try {
            c.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
