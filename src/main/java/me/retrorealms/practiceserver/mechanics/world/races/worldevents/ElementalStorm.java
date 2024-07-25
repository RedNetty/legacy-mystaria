package me.retrorealms.practiceserver.mechanics.world.races.worldevents;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ElementalStorm {
    private static final int STORM_DURATION = 300; // 5 minutes
    private static final int STORM_RADIUS = 50; // blocks

    public enum StormType {
        FIRE, ICE, LIGHTNING, EARTH
    }

    public static void triggerElementalStorm(Location center, StormType type) {
        new BukkitRunnable() {
            int timeLeft = STORM_DURATION;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "The elemental storm has subsided.");
                    this.cancel();
                    return;
                }

                applyStormEffects(center, type);
                timeLeft--;
            }
        }.runTaskTimer(PracticeServer.getInstance(), 0L, 20L);

        Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "An elemental storm of " + type.name() + " has begun at X: " + center.getBlockX() + ", Z: " + center.getBlockZ() + "!");
    }

    private static void applyStormEffects(Location center, StormType type) {
        for (Player player : center.getWorld().getPlayers()) {
            if (player.getLocation().distance(center) <= STORM_RADIUS) {
                switch (type) {
                    case FIRE:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 1));
                        break;
                    case ICE:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 1));
                        break;
                    case LIGHTNING:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1));
                        break;
                    case EARTH:
                        player.setHealth(player.getHealth() + (player.getHealth() * .05));
                        StringUtil.sendCenteredMessage(player, "&a+" + (player.getHealth() * .05) + "&bHP &7- &3EARTH STORM");
                        break;
                }
            }
        }

    }

}