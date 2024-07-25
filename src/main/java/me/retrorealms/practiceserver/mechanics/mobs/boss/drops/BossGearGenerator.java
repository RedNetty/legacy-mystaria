package me.retrorealms.practiceserver.mechanics.mobs.boss.drops;

import me.retrorealms.practiceserver.mechanics.drops.CreateDrop;
import me.retrorealms.practiceserver.mechanics.mobs.boss.BossConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class BossGearGenerator {
    private static final FileConfiguration config = BossConfigHandler.getBossFile();

    public static int getWeaponType(String boss) {
        if (config == null) {
            Bukkit.getLogger().warning("Config is null in BossGearGenerator");
            return 2; // Default to polearm if config is null
        }
        String configString = boss.toLowerCase() + ".";
        for (int i = 1; i <= 4; i++) {
            if (config.contains(configString + i)) {
                return i;
            }
        }
        Bukkit.getLogger().warning("No valid weapon type found for " + boss + ", defaulting to 2 (polearm)");
        return 2; // Default to polearm if no valid type is found
    }
    public static int getHealth(int piece, String boss) {
        if (!CreateDrop.isWeapon(piece)) {
            String healthString = getString(piece, "health", boss);
            Bukkit.getLogger().info("Health string for " + boss + " piece " + piece + ": " + healthString);

            String[] valueStrings = healthString.split("-");
            Bukkit.getLogger().info("Split values: " + Arrays.toString(valueStrings));

            if (valueStrings.length >= 2) {
                try {
                    int minHealth = Integer.parseInt(valueStrings[0].trim());
                    int maxHealth = Integer.parseInt(valueStrings[1].trim());
                    int randomHealth = ThreadLocalRandom.current().nextInt(minHealth, maxHealth + 1);
                    return randomHealth;
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("Invalid health values for " + boss + " piece " + piece + ": " + healthString);
                    return 1000; // Default value
                }
            } else {
                Bukkit.getLogger().warning("Invalid health string format for " + boss + " piece " + piece + ": " + healthString);
                return 1000; // Default value
            }
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
            return config.contains(configString) && config.getString(configString).equalsIgnoreCase("armor");
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
