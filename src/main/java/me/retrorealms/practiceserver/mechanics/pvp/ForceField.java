package me.retrorealms.practiceserver.mechanics.pvp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class ForceField implements Listener {
    private ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("PvP ForceField Thread").build());
    private final Map<UUID, Set<Location>> previousUpdates = new ConcurrentHashMap<>();
    public final CopyOnWriteArrayList<Player> tag = Lists.newCopyOnWriteArrayList();
    private static final List<BlockFace> ALL_DIRECTIONS = ImmutableList.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.getInstance());
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> player != null && (Listeners.isInCombat(player) || Alignments.chaotic.containsKey(player.getName()) || previousUpdates.containsKey(player.getUniqueId())))
                        .forEach(player -> {
                            if (executorService.isShutdown()) {
                                executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("PvP ForceField Thread").build());
                                return;
                            }

                            executorService.execute(() -> {
                                UUID uuid = player.getUniqueId();
                                if (!player.isOnline()) {
                                    previousUpdates.remove(uuid);
                                    return;
                                }

                                Set<Location> changedBlocks = getChangedBlocks(player);
                                Material forceFieldMaterial = Material.STAINED_GLASS;
                                byte forceFieldMaterialDamage = 14;

                                Set<Location> removeBlocks = previousUpdates.getOrDefault(uuid, new HashSet<>());
                                changedBlocks.forEach(location -> player.sendBlockChange(location, forceFieldMaterial, forceFieldMaterialDamage));
                                removeBlocks.removeAll(changedBlocks);

                                removeBlocks.forEach(location -> player.sendBlockChange(location, location.getBlock().getType(), location.getBlock().getData()));
                                previousUpdates.put(uuid, changedBlocks);
                            });
                        });
            }
        }.runTaskTimerAsynchronously(PracticeServer.getInstance(), 2L, 2L);
    }


    @EventHandler
    public void shutdown(PluginDisableEvent event) {
        System.out.println("Shutdown method called");
        previousUpdates.forEach((uuid, locations) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                locations.forEach(location -> {
                    Block block = location.getBlock();
                    player.sendBlockChange(location, block.getType(), block.getData());
                });
            }
        });

        // Shutdown executor service and clean up threads
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Set<Location> locations = previousUpdates.get(event.getPlayer().getUniqueId());
        if (locations != null && locations.contains(event.getTo())) {
            event.setCancelled(true);
        }
    }

    private Set<Location> getChangedBlocks(Player player) {
        Set<Location> locations = new HashSet<>();
        if (player == null || (!Listeners.isInCombat(player) && !Alignments.chaotic.containsKey(player.getName()))) {
            return locations;
        }

        // Find the radius around the player
        int r = 10;
        Location l = player.getLocation();
        IntStream.rangeClosed(-r, r).forEach(x -> {
            IntStream.rangeClosed(-r, r).forEach(z -> {
                Location location = l.clone().add(x, 0, z);
                if (Alignments.isSafeZone(location) && isPvpSurrounding(location)) {
                    IntStream.range(-r, r).forEach(i -> {
                        Location loc = location.clone().add(0, i, 0);
                        if (l.distanceSquared(loc) <= 80 && loc.getBlock().getType().equals(Material.AIR)) {
                            locations.add(loc);
                        }
                    });
                }
            });
        });

        return locations;
    }

    private boolean isPvpSurrounding(Location loc) {
        return Arrays.stream(ALL_DIRECTIONS.toArray()).anyMatch(direction -> !Alignments.isSafeZone(loc.getBlock().getRelative((BlockFace) direction).getLocation()));
    }
}
