package me.retrorealms.practiceserver.mechanics.mobs.boss.drops;

import me.retrorealms.practiceserver.mechanics.drops.CreateDrop;
import me.retrorealms.practiceserver.mechanics.mobs.boss.WorldBossHandler;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.concurrent.ThreadLocalRandom;

public class BossGearGenerator {
    private static final FileConfiguration config = WorldBossHandler.getBossFile();

    public static int getWeaponType(String boss) {
        String configString = boss.toLowerCase() + ".";
        if (config.contains(configString + "1")) {
            return 1;
        }
        if (config.contains(configString + "2")) {
            return 2;
        }
        if (config.contains(configString + "3")) {
            return 3;
        }
        if (config.contains(configString + "4")) {
            return 4;
        }
        return 1;
    }

    public static int getHealth(int piece, String boss) {
        if (!CreateDrop.isWeapon(piece)) {
            String healthString = getString(piece, "health", boss);
            String[] valueStrings = healthString.split("-", 0);
            return ThreadLocalRandom.current().nextInt(Integer.parseInt(valueStrings[0]), Integer.parseInt(valueStrings[1]));
        }
        return 0;
    }

    /*
     * piece - helm, chest, pants, boots, sword, axe, polearm, staff
     * stat - vit, block, dodge, fire, poison, ice, lifesteal, accuracy, crit, dex, vsplayers, vsmonsters, thorns, str, int, pure*/
    public static String getString(int piece, String stat, String boss) {
        if (config != null) {
            String configString = boss.toLowerCase() + "." + piece + "." + stat.toLowerCase();
            if (config.contains(configString)) {
                return config.getString(configString);
            } else {
                return "0";
            }
        }
        return "0";
    }

    /*
     * piece - helm, chest, pants, boots, sword, axe, polearm, staff
     * stat - vit, block, dodge, fire, poison, ice, lifesteal, accuracy, crit, dex, vsplayers, vsmonsters, thorns, str, int, pure*/
    public static int getInt(int piece, String stat, String boss) {
        if (config != null) {
            String configString = boss.toLowerCase() + "." + piece + "." + stat.toLowerCase();
            if (config.contains(configString)) {
                return config.getInt(configString);

            } else {
                return 0;
            }
        }
        return 0;
    }


    public static boolean isArmor(int piece, String boss) {
        if (config != null) {
            String configString = boss.toLowerCase() + "." + piece + ".armor-dps";
            return config.contains(configString) && config.getString(configString) == "armor";
        }
        return false;
    }

    /*
     * piece - helm, chest, pants, boots, sword, axe, polearm, staff
     * stat - vit, block, dodge, fire, poison, ice, lifesteal, accuracy, crit, dex, vsplayers, vsmonsters, thorns, str, int, pure*/
    public static boolean hasStat(int piece, String stat, String boss) {
        if (config != null) {
            return config.contains(boss.toLowerCase() + "." + piece + "." + stat.toLowerCase());
        }
        return false;
    }


}
