package me.retrorealms.practiceserver.mechanics.world.races.worldevents;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class ArtifactHunt {
    private static final int ARTIFACT_DURATION = 600; // 10 minutes

    public enum ArtifactType {
        SWORD_OF_LEGENDS(ChatColor.RED + "Sword of Legends", Material.DIAMOND_SWORD),
        CROWN_OF_WISDOM(ChatColor.BLUE + "Crown of Wisdom", Material.GOLD_HELMET),
        AMULET_OF_VITALITY(ChatColor.GREEN + "Amulet of Vitality", Material.EMERALD);

        private final String name;
        private final Material material;

        ArtifactType(String name, Material material) {
            this.name = name;
            this.material = material;
        }
    }

    public static void spawnArtifact(Location location, ArtifactType type) {
        ItemStack artifact = new ItemStack(type.material);
        ItemMeta meta = artifact.getItemMeta();
        meta.setDisplayName(type.name);
        artifact.setItemMeta(meta);

        location.getWorld().dropItem(location, artifact);

        Bukkit.broadcastMessage(ChatColor.GOLD + "The " + type.name + ChatColor.GOLD + " has appeared at X: " + location.getBlockX() + ", Z: " + location.getBlockZ() + "!");
    }

    public static void applyArtifactEffect(Player player, ArtifactType type) {
        switch (type) {
            case SWORD_OF_LEGENDS:
                // Increase player's damage
                break;
            case CROWN_OF_WISDOM:
                // Increase player's skill cooldown rate
                break;
            case AMULET_OF_VITALITY:
                // Increase player's max health and regeneration
                break;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                removeArtifactEffect(player, type);
            }
        }.runTaskLater(PracticeServer.getInstance(), ARTIFACT_DURATION * 20L);
    }

    private static void removeArtifactEffect(Player player, ArtifactType type) {
        // Remove the artifact's effect from the player
    }
}