package me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.actionbar.ActionBar;
import me.retrorealms.practiceserver.mechanics.mobs.MobHandler;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.bosses.BossEnum;
import me.retrorealms.practiceserver.mechanics.mobs.elite.worldboss.bosses.Frostwing;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class WorldBossHandler implements Listener {

    private static int totalT5Kills = 0;
    private static WorldBoss activeBoss = null;

    private static BossSpawnLocation bossSpawnLocation = null;


    public static BossSpawnLocation getBossSpawnLocation() {
        return bossSpawnLocation;
    }

    public static WorldBoss getActiveBoss() {
        return activeBoss;
    }

    public static int getTotalT5Kills() {
        return totalT5Kills;
    }

    public static void addKill() {
        if (activeBoss != null) return;
        totalT5Kills++;
    }

    public static void resetKills() {
        WorldBossHandler.totalT5Kills = 0;
    }

    public static void spawnBoss() {
        resetKills();
        bossSpawnLocation = BossSpawnLocation.values()[ThreadLocalRandom.current().nextInt(BossSpawnLocation.values().length)];
        activeBoss = new Frostwing().spawnBoss(bossSpawnLocation.getLocation());

        String bossSpawnMessage = "";
        switch (bossSpawnLocation) {
            case CASTLE:
                bossSpawnMessage = ChatColor.YELLOW + "* " + activeBoss.bossEnum.getDisplayName() + " spawned at 'The Castle' *";
                break;
            case TRIPOLI:
                bossSpawnMessage = ChatColor.YELLOW + "* " + activeBoss.bossEnum.getDisplayName() + " spawned in 'The Tripoli Plains' *";
                break;
            case AVALON_ENTRANCE:
                bossSpawnMessage = ChatColor.YELLOW + "* " + activeBoss.bossEnum.getDisplayName() + " spawned at 'Avalon Entrance' *";
                break;
            case AVALON_BRIDGE:
                bossSpawnMessage = ChatColor.YELLOW + "* " + activeBoss.bossEnum.getDisplayName() + " spawned on 'The Avalon Bridges' *";
                break;
        }
        String finalBossSpawnMessage = bossSpawnMessage;
        Bukkit.getOnlinePlayers().forEach(player -> {
            StringUtil.sendCenteredMessage(player, "&e★☆✫ &3&l World-Boss &e✫☆★");
            StringUtil.sendCenteredMessage(player, finalBossSpawnMessage);
            ActionBar.sendActionBar(player, finalBossSpawnMessage, 3);
            player.playSound(player.getLocation(), Sound.BLOCK_END_GATEWAY_SPAWN, 2, 2);
        });
    }

    public static WorldBoss bossFromString(String boss) {
        if (boss.equals("frostwing")) {
            return BossEnum.FROSTWING.getWorldBoss();
        }
        return BossEnum.FROSTWING.getWorldBoss();
    }

    public void onLoad() {
        Bukkit.getPluginManager().registerEvents(this, PracticeServer.getInstance());
        BossGearGenerator.onLoad();
        new BukkitRunnable() {
            public void run() {
                if (totalT5Kills > 300 && activeBoss == null) {
                    spawnBoss();
                    totalT5Kills = 0;
                }
                if (activeBoss != null && activeBoss.getLivingEntity().isDead()) {
                    activeBoss = null;
                }

                if (activeBoss != null) {

                    Location location = activeBoss.getLivingEntity().getLocation();
                    World world = location.getWorld();

                    world.getNearbyEntities(location, 40, 40, 40).forEach(entity -> {
                        if ((entity instanceof Player)) {

                            Player player = (Player) entity;
                            if (player.isInsideVehicle()) {
                                player.getVehicle().remove();
                                StringUtil.sendCenteredMessage(player, "&7&l*** &7You've dismounted due to a powerful Aura &7&l***");
                            }
                        }

                    });
                }
            }
        }.runTaskTimer(PracticeServer.getInstance(), 10L, 20L);
    }

    public void unLoad() {

    }

    @EventHandler
    public void onBossKill(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
            if (MobHandler.isWorldBoss(event.getEntity()) && getActiveBoss() != null) {
                if (event.getDamage() > ((LivingEntity) event.getEntity()).getHealth()) {
                    activeBoss.rewardLoot();
                    activeBoss.getLivingEntity().remove();
                    resetKills();

                }
            }
        }
    }
        @EventHandler
        public void onDamageBoss (EntityDamageByEntityEvent e){
            try {
                Player p;
                if (e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity) {
                    if (e.getDamage() > 0.0) {
                        if (MobHandler.isWorldBoss(e.getEntity())) {
                            p = (Player) e.getDamager();
                            activeBoss.addDamage(p, (int) e.getDamage());
                        }
                    }
                }
            } catch (Exception ex) {

            }
        }
    }