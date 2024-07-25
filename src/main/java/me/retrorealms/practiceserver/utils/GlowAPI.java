package me.retrorealms.practiceserver.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class GlowAPI {

    private static ProtocolManager protocolManager;
    private static Map<UUID, ChatColor> glowingEntities = new ConcurrentHashMap<>();
    private static JavaPlugin plugin;
    private static final long DELAY_BETWEEN_UPDATES = 1L; // 1 tick delay between each player update

    public static void initialize(JavaPlugin plugin) {
        GlowAPI.plugin = plugin;
        protocolManager = ProtocolLibrary.getProtocolManager();
        plugin.getLogger().info("GlowAPI has been initialized.");
    }

    public static void setGlowing(Entity entity, ChatColor color) {
        if (entity == null) {
            plugin.getLogger().warning("Attempted to set glow for null entity");
            return;
        }

        if (color == null) {
            removeGlowing(entity);
            return;
        }

        glowingEntities.put(entity.getUniqueId(), color);

        new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> players = new ArrayList<>(entity.getWorld().getPlayers());
                updateGlowingWithDelay(players, entity, true, color, 0);
            }
        }.runTask(plugin);

        plugin.getLogger().info("Set glow color " + color + " for entity " + entity.getUniqueId());
    }

    public static void removeGlowing(Entity entity) {
        if (entity == null) {
            plugin.getLogger().warning("Attempted to remove glow for null entity");
            return;
        }

        glowingEntities.remove(entity.getUniqueId());

        new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> players = new ArrayList<>(entity.getWorld().getPlayers());
                updateGlowingWithDelay(players, entity, false, null, 0);
            }
        }.runTask(plugin);

        plugin.getLogger().info("Removed glow for entity " + entity.getUniqueId());
    }

    private static void updateGlowingWithDelay(List<Player> players, Entity entity, boolean glow, ChatColor color, int index) {
        if (index >= players.size()) {
            return;
        }

        Player player = players.get(index);
        updateGlowingForPlayer(player, entity, glow, color);

        new BukkitRunnable() {
            @Override
            public void run() {
                updateGlowingWithDelay(players, entity, glow, color, index + 1);
            }
        }.runTaskLater(plugin, DELAY_BETWEEN_UPDATES);
    }

    private static void updateGlowingForPlayer(Player player, Entity entity, boolean glow, ChatColor color) {
        if (player == null || !player.isOnline() || entity == null) {
            return;
        }

        try {
            sendMetadataPacket(player, entity, glow);
            if (glow) {
                sendTeamPacket(player, entity, color);
            } else {
                removeTeamPacket(player, entity);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update glowing for player " + player.getName(), e);
        }
    }

    private static void sendMetadataPacket(Player player, Entity entity, boolean glow) throws Exception {
        PacketContainer metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, entity.getEntityId());

        WrappedDataWatcher watcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
        watcher.setObject(0, serializer, (byte) (glow ? 0x40 : 0));

        metadataPacket.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        protocolManager.sendServerPacket(player, metadataPacket);
    }

    private static void sendTeamPacket(Player player, Entity entity, ChatColor color) throws Exception {
        String teamName = "glow_" + entity.getUniqueId().toString().substring(0, 8);

        PacketContainer teamPacket = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        teamPacket.getStrings().write(0, teamName);
        teamPacket.getIntegers().write(0, 0);
        teamPacket.getStrings().write(1, "");
        teamPacket.getStrings().write(2, color.toString());
        teamPacket.getStrings().write(3, "");
        teamPacket.getStrings().write(4, "always");
        teamPacket.getStrings().write(5, "never");
        teamPacket.getIntegers().write(1, 0);
        teamPacket.getSpecificModifier(Collection.class).write(0, Collections.singletonList(entity.getUniqueId().toString()));

        protocolManager.sendServerPacket(player, teamPacket);
    }

    private static void removeTeamPacket(Player player, Entity entity) throws Exception {
        String teamName = "glow_" + entity.getUniqueId().toString().substring(0, 8);
        PacketContainer removePacket = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        removePacket.getStrings().write(0, teamName);
        removePacket.getIntegers().write(0, 1);
        protocolManager.sendServerPacket(player, removePacket);
    }

    public static void updateGlowingForPlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                List<Map.Entry<UUID, ChatColor>> entries = new ArrayList<>(glowingEntities.entrySet());
                updateGlowingForPlayerWithDelay(player, entries, 0);
            }
        }.runTask(plugin);
    }

    private static void updateGlowingForPlayerWithDelay(Player player, List<Map.Entry<UUID, ChatColor>> entries, int index) {
        if (index >= entries.size()) {
            return;
        }

        Map.Entry<UUID, ChatColor> entry = entries.get(index);
        Entity entity = Bukkit.getEntity(entry.getKey());
        if (entity != null && entity.getWorld().equals(player.getWorld())) {
            updateGlowingForPlayer(player, entity, true, entry.getValue());
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                updateGlowingForPlayerWithDelay(player, entries, index + 1);
            }
        }.runTaskLater(plugin, DELAY_BETWEEN_UPDATES);
    }

    public static void cleanupGlowingEntities() {
        glowingEntities.entrySet().removeIf(entry -> Bukkit.getEntity(entry.getKey()) == null);
    }
}