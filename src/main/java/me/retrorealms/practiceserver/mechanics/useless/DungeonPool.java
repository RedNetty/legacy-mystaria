package me.retrorealms.practiceserver.mechanics.useless;

import com.google.common.collect.Lists;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.party.Parties;
import me.retrorealms.practiceserver.mechanics.useless.skeleton.SkeletonDungeon;
import me.retrorealms.practiceserver.mechanics.useless.task.AsyncTask;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Giovanni on 2-5-2017.
 */
public class DungeonPool {

    private static DungeonPool pool;

    public static DungeonPool getPool() {
        return pool == null ? new DungeonPool() : pool;
    }

    private final List<Player> inPool = Lists.newArrayList();

    public void init() {
        /* OLD */

        new AsyncTask(() -> {

            Bukkit.getOnlinePlayers().forEach(player -> {
                ApplicableRegionSet regionSet = WGBukkit.getRegionManager(player.getLocation().getWorld()).getApplicableRegions(player.getLocation());
                regionSet.forEach(protectedRegion -> {

                    if (protectedRegion.getId().equalsIgnoreCase("skellyDEntranceArea")) {

                        if (inPool.contains(player)) return;

                        inPool.add(player);

                        if (Parties.isInParty(player)) {
                            List<Player> players = Parties.getEntirePartyOf(player);

                            List<Player> nearbyPlayers = Lists.newArrayList();

                            player.getNearbyEntities(50, 50, 50).forEach(nearby -> {

                                if (nearby instanceof Player) {

                                    Player near = (Player) nearby;
                                    if (!players.contains(near)) return;

                                    nearbyPlayers.add(near);
                                    inPool.add(near);
                                }
                            });

                            nearbyPlayers.add(player);

                            SkeletonDungeon skeletonDungeon = new SkeletonDungeon(nearbyPlayers);

                            return;
                        }

                        SkeletonDungeon skeletonDungeon = new SkeletonDungeon(Collections.singletonList(player));
                    }
                });


            });


        }).setInterval(100L).scheduleRepeatingTask();
    }

    public void load(Consumer<World> doAfter) {
        String worldName = "dungeonSkeletonWorld_VAR_" + System.currentTimeMillis();

        try {
            unZip(new ZipFile(PracticeServer.getInstance().getDataFolder() + "/dungeons/skeletonDungeon.zip"), worldName);

            if (new File(worldName + "/uid.dat").exists()) {
                // Delete that shit.
                new File(worldName + "/uid.dat").delete();
            }

            if (new File(worldName + "/players").exists())
                deleteFolder(new File(worldName + "/players"));

        } catch (IOException e) {
            e.printStackTrace();
        }


        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticeServer.getInstance(), () -> {
            World world = null;

            WorldCreator worldCreator = new WorldCreator(worldName);
            worldCreator.generateStructures(false);
            world = Bukkit.getServer().createWorld(worldCreator);
            world.setStorm(false);
            world.setAutoSave(false);
            world.setKeepSpawnInMemory(false);
            world.setSpawnLocation(-1774, 21, -537);
            world.setPVP(false);
            world.setTime(18000);
            world.setGameRuleValue("randomTickSpeed", "0");
            Bukkit.getWorlds().add(world);

            doAfter.accept(world);

        }, 20);

    }


    private void deleteFolder(File folder) {
        try {
            if (folder == null) return;
            FileUtils.forceDelete(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void unZip(ZipFile zipFile, String worldName) {
        new File(worldName).mkdir();
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(worldName, entry.getName());

                if (entry.isDirectory())
                    entryDestination.mkdirs();
                else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
