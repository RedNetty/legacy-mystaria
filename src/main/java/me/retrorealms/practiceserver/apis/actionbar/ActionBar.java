package me.retrorealms.practiceserver.apis.actionbar;

import lombok.Getter;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ActionBar {

    private static Map<UUID, Map<String, Function<Player, String>>> playerActionBars = new ConcurrentHashMap<>();
    @Getter
    private static Map<UUID, BukkitTask> taskMap = new ConcurrentHashMap<>();
    private static BukkitTask updateTask;

    public static void initialize() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateActionBar(player);
                }
            }
        }.runTaskTimerAsynchronously(PracticeServer.getInstance(), 0, 1);
    }

    public static void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        for (BukkitTask task : taskMap.values()) {
            task.cancel();
        }
        playerActionBars.clear();
        taskMap.clear();
    }

    public static void sendActionBar(Player player, String message) {
        sendActionBar(player, message, 1);
    }

    public static void sendActionBar(Player player, String message, int duration) {
        cancelPreviousTask(player);
        setActionBarComponent(player, "temporary", p -> message);
        BukkitTask task = new BukkitRunnable() {
            int timer = duration;

            public void run() {
                if (timer <= 0) {
                    removeActionBarComponent(player, "temporary");
                    taskMap.remove(player.getUniqueId());
                    cancel();
                } else {
                    timer--;
                }
            }
        }.runTaskTimerAsynchronously(PracticeServer.getInstance(), 0, 20);
        taskMap.put(player.getUniqueId(), task);
    }

    private static void setActionBarComponent(Player player, String key, Function<Player, String> messageSupplier) {
        UUID playerId = player.getUniqueId();
        playerActionBars.computeIfAbsent(playerId, k -> new LinkedHashMap<>()).put(key, messageSupplier);
    }

    private static void removeActionBarComponent(Player player, String key) {
        UUID playerId = player.getUniqueId();
        if (playerActionBars.containsKey(playerId)) {
            playerActionBars.get(playerId).remove(key);
        }
    }

    private static void updateActionBar(Player player) {
        UUID playerId = player.getUniqueId();
        if (playerActionBars.containsKey(playerId)) {
            List<String> activeComponents = new ArrayList<>();

            // Check for combat status
            String combatStatus = getCombatStatus(player);
            if (combatStatus != null) {
                activeComponents.add(combatStatus);
            }

            // Add other components
            for (Function<Player, String> messageSupplier : playerActionBars.get(playerId).values()) {
                String message = messageSupplier.apply(player);
                if (message != null && !message.isEmpty()) {
                    activeComponents.add(message);
                }
            }

            // Join non-empty components
            String finalMessage = activeComponents.stream()
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" - "));

            if (!finalMessage.isEmpty()) {
                sendActionBarPacket(player, ChatColor.translateAlternateColorCodes('&', finalMessage));
            }
        }
    }

    private static String getCombatStatus(Player player) {
        if (Listeners.isInCombat(player)) {
            return "&7In Combat (&c" + Listeners.combatSeconds(player) + "s&7)";
        }
        return null;
    }

    private static void sendActionBarPacket(Player player, String message) {
        IChatBaseComponent icbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat packet = new PacketPlayOutChat(icbc, ChatMessageType.GAME_INFO);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    private static void cancelPreviousTask(Player player) {
        UUID playerId = player.getUniqueId();
        if (taskMap.containsKey(playerId)) {
            taskMap.get(playerId).cancel();
            taskMap.remove(playerId);
        }
    }
}