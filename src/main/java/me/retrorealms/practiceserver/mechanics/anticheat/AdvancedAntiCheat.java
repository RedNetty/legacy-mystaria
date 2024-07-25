package me.retrorealms.practiceserver.mechanics.anticheat;

import lombok.Getter;
import lombok.Setter;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.anticheat.listeners.CombatListener;
import me.retrorealms.practiceserver.mechanics.anticheat.listeners.PlayerJoinQuitListener;
import me.retrorealms.practiceserver.mechanics.anticheat.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Getter
@Setter
public class AdvancedAntiCheat implements Listener {

    private final PracticeServer plugin;
    private final Map<UUID, ACPlayerData> playerDataMap;
    private final ViolationManager violationManager;
    private final CheckManager checkManager;
    private final ConfigManager configManager;
    private final PacketAnalyzer packetAnalyzer;
    private final DataLogger dataLogger;

    public AdvancedAntiCheat(PracticeServer plugin) {
        this.plugin = plugin;
        this.configManager = new ConfigManager(plugin);
        this.packetAnalyzer = new PacketAnalyzer(this);
        this.dataLogger = new DataLogger(plugin);
        this.playerDataMap = new HashMap<>();
        this.violationManager = new ViolationManager(plugin);
        this.checkManager = new CheckManager(this);

        registerListeners();
        startTasks();
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinQuitListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), plugin);
    }

    private void startTasks() {
        new ViolationDecayTask(this).runTaskTimerAsynchronously(plugin, 1200L, 1200L); // Run every minute
        new CleanupTask(this).runTaskTimerAsynchronously(plugin, 6000L, 6000L);
        new TickTask(this).runTaskTimer(plugin, 1L, 1L);
    }

    public void onDisable() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(plugin);
        for (Player player : Bukkit.getOnlinePlayers()) {
            packetAnalyzer.uninjectPlayer(player);
        }
    }

    public ACPlayerData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new ACPlayerData(player));
    }

    public void removePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }


    public boolean isPassableBlock(Block block) {
        Material material = block.getType();
        return material.name().contains("ANVIL") || material.name().contains("CHEST") ||
                material.name().contains("GLASS") || material.name().contains("BAR") ||
                material.name().contains("STAIR") || material.name().contains("WALL") ||
                material.name().contains("FENCE") || material.name().contains("LADDER") ||
                material.name().contains("VINE") || material.toString().contains("PISTON") ||
                material.toString().contains("SLAB") || material.toString().contains("BUTTON") ||
                material.toString().contains("DOOR") || material.toString().contains("TRAPDOOR") ||
                material.toString().contains("REDSTONE") || material.toString().contains("SIGN") ||
                material.toString().contains("BANNER") || material.toString().contains("BED") ||
                material.toString().contains("CARPET") || material.toString().contains("RAIL") ||
                material.toString().contains("CAMPFIRE") || material.toString().contains("CAKE") ||
                material.toString().contains("CAULDRON") || material.toString().contains("LECTERN") ||
                material.toString().contains("TORCH") || material.toString().contains("LANTERN") ||
                material.toString().contains("BREWING_STAND") || material.toString().contains("COBWEB") ||
                material.toString().contains("COMMAND_BLOCK") || material.toString().contains("COMPOSTER") ||
                material.toString().contains("CONDUIT") || material.toString().contains("DAYLIGHT_DETECTOR") ||
                material.toString().contains("DIODE") || material.toString().contains("REPEATER") ||
                material.toString().contains("COMPARATOR") || material.toString().contains("DROPPER") ||
                material.toString().contains("ENCHANTING_TABLE") || material.toString().contains("ENDER_CHEST") ||
                material.toString().contains("FERN") || material.toString().contains("FLOWER") ||
                material.toString().contains("GRASS") || material.toString().contains("HOPPER") ||
                material.toString().contains("LILY_PAD") || material.toString().contains("MUSHROOM") ||
                material.toString().contains("SCAFFOLDING") || material.toString().contains("SEA_PICKLE") ||
                material.toString().contains("SNOW") || material.toString().contains("STANDING_BANNER") ||
                material.toString().contains("WALL_BANNER") || material.toString().contains("TRIPWIRE") ||
                material.toString().contains("TRIPWIRE_HOOK") || material.toString().contains("WALL_TORCH") ||
                material.toString().contains("SAPLING");
    }

    public Map<Object, Object> getPlayerDataMap() {
        return new HashMap<>(playerDataMap);
    }
}
