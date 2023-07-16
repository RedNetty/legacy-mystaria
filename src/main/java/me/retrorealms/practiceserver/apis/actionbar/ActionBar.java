package me.retrorealms.practiceserver.apis.actionbar;
import java.util.HashMap;
import java.util.Map;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.pvp.Alignments;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;


public class ActionBar {

    private static Map<UUID, BukkitTask> taskMap = new ConcurrentHashMap<>();

    public static Map<UUID, BukkitTask> getTaskMap() {
        return taskMap;
    }

    public static void sendActionBar(Player player, String message, int duration) {
        cancelPreviousTask(player);
        BukkitTask task = new BukkitRunnable() {
            int timer = duration;

            public void run() {
                if (timer <= 0) {
                    taskMap.remove(player.getUniqueId());
                    cancel();
                } else {
                    String toSend = message;
                    String combatString = "";
                    if(Listeners.isInCombat(player)) combatString = "&7In Combat (&c" + Listeners.combatSeconds(player) + "s&7)";
                    if(message == "" && Listeners.isInCombat(player)){
                        toSend = combatString;
                    }
                    if(Listeners.isInCombat(player) && !(message.equalsIgnoreCase(""))) toSend = message + " &7- " + combatString;

                    sendActionBar(player, ChatColor.translateAlternateColorCodes('&', toSend));
                    timer--;
                }
            }
        }.runTaskTimerAsynchronously(PracticeServer.getInstance(), 0, 20);
        taskMap.put(player.getUniqueId(), task);
    }

    private static void cancelPreviousTask(Player player) {
        if (taskMap.containsKey(player.getUniqueId())) {
            taskMap.get(player.getUniqueId()).cancel();
            taskMap.remove(player.getUniqueId());
        }
    }

    public static void sendActionBar(Player player, String message) {
        IChatBaseComponent icbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat packet = new PacketPlayOutChat(icbc, ChatMessageType.GAME_INFO);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}