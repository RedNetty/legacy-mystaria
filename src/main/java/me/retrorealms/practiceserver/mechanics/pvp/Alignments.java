package me.retrorealms.practiceserver.mechanics.pvp;

import com.google.common.collect.Maps;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.apis.actionbar.ActionBar;
import me.retrorealms.practiceserver.enums.ranks.RankEnum;
import me.retrorealms.practiceserver.mechanics.damage.Damage;
import me.retrorealms.practiceserver.mechanics.donations.StatTrak.WepTrak;
import me.retrorealms.practiceserver.mechanics.duels.Duels;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.mechanics.moderation.ModerationMechanics;
import me.retrorealms.practiceserver.mechanics.party.Scoreboards;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.mechanics.player.Toggles;
import me.retrorealms.practiceserver.mechanics.teleport.TeleportBooks;
import me.retrorealms.practiceserver.utils.JSONMessage;
import me.retrorealms.practiceserver.utils.StringUtil;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Alignments
        implements Listener {
    public static ConcurrentHashMap<String, Integer> neutral = new ConcurrentHashMap<String, Integer>();
    public static ConcurrentHashMap<String, Integer> chaotic = new ConcurrentHashMap<String, Integer>();
    public static ConcurrentHashMap<String, Long> tagged = new ConcurrentHashMap<String, Long>();
    public static boolean logout = false;

    public static HashMap<Player, BossBar> playerBossBars;

    private int NEUTRAL_SECONDS = 120;
    private int CHAOTIC_SECONDS = 300;

    public void onEnable() {
        playerBossBars = Maps.newHashMap();
        int time;
        PracticeServer.log.info("[Alignments] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        new BukkitRunnable() {

            public void run() {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (p.isOnline()) {
                        int time;
                        if (chaotic.containsKey(p.getName())) {
                            time = chaotic.get(p.getName());
                            if (time <= 1) {
                                chaotic.remove(p.getName());
                                neutral.put(p.getName(), NEUTRAL_SECONDS);
                                updatePlayerAlignment(p);
                                p.sendMessage(ChatColor.YELLOW + "          * YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *");
                                p.sendMessage(ChatColor.GRAY + "While neutral, players who kill you will not become chaotic. You have a 50% chance of dropping your weapon, and a 25% chance of dropping each piece of equiped armor on death. Neutral alignment will expire 2 minutes after last hit on player.");
                                p.sendMessage(ChatColor.YELLOW + "* YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *");
                                ActionBar.sendActionBar(p, ChatColor.YELLOW + "* YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *", 4);
                            } else {
                                chaotic.put(p.getName(), --time);
                            }
                        }
                        if (neutral.containsKey(p.getName())) {
                            time = neutral.get(p.getName());
                            if (time == 1) {
                                neutral.remove(p.getName());
                                updatePlayerAlignment(p);
                                p.sendMessage(ChatColor.GREEN + "          * YOU ARE NOW " + ChatColor.BOLD + "LAWFUL" + ChatColor.GREEN + " ALIGNMENT *");
                                p.sendMessage(ChatColor.GRAY + "While lawful, you will not lose any equipped armor on death, instead, all armor will lose 30% of its durability when you die. Any players who kill you while you're lawfully aligned will become chaotic.");
                                p.sendMessage(ChatColor.GREEN + "          * YOU ARE NOW " + ChatColor.BOLD + "LAWFUL" + ChatColor.GREEN + " ALIGNMENT *");
                                //  TTA_Methods.sendActionBar(p, ChatColor.GREEN + "* YOU ARE NOW " + ChatColor.BOLD + "LAWFUL" + ChatColor.GREEN + " ALIGNMENT *", 60);
                            } else {
                                --time;
                                neutral.put(p.getName(), time--);
                            }
                        }
                    }
                    if (tagged.containsKey(p.getName()) && (!tagged.containsKey(p.getName()) || System.currentTimeMillis() - tagged.get(p.getName()) <= 10000) || p.getHealth() <= 0.0)
                        continue;
                    PlayerInventory i = p.getInventory();
                    double amt = 5.0;
                    int vit = 0;
                    ItemStack[] arritemStack = i.getArmorContents();
                    int n = arritemStack.length;
                    int n2 = 0;
                    while (n2 < n) {
                        ItemStack is = arritemStack[n2];
                        if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
                            double added = Damage.getHps(is);
                            amt += added;
                            int addedvit = Damage.getElem(is, "VIT");
                            vit += addedvit;
                        }
                        ++n2;
                    }
                    if (vit > 0) {
                        amt += (double) ((int) Math.round((double) vit * 0.3));
                    }
                    double healthToSet = p.getHealth() + amt;
                    if (healthToSet > p.getMaxHealth()) {
                        p.setHealth(p.getMaxHealth());
                    } else p.setHealth(healthToSet);
                    double healthPercentage = (p.getHealth() / p.getMaxHealth());
                    if (healthPercentage * 100.0F > 100.0F) {
                        healthPercentage = 1.0;
                    }
                    float pcnt = (float) (healthPercentage * 1.F);
                    BarColor barColor = Damage.getBarColor(p);
                    ChatColor titleColor = Damage.barTitleColor(p);
                    if (Toggles.hasLevelBarHP(p)) {
                        if (!playerBossBars.containsKey(p)) {
                            // Set new one
                            BossBar bossBar = Bukkit.createBossBar(
                                    titleColor + "" + ChatColor.BOLD + "HP "
                                            + titleColor + (int) p.getHealth() + titleColor +
                                            ChatColor.BOLD + " / " + titleColor + (int) p.getMaxHealth(),
                                    barColor, BarStyle.SOLID);
                            bossBar.addPlayer(p);
                            playerBossBars.put(p, bossBar);
                            playerBossBars.get(p).setProgress(pcnt);
                        } else {
                            playerBossBars.get(p)
                                    .setTitle(titleColor + "" + ChatColor.BOLD + "HP "
                                            + titleColor + (int) p.getHealth() + titleColor
                                            + ChatColor.BOLD + " / " + titleColor + (int) p.getMaxHealth());
                            playerBossBars.get(p).setProgress(pcnt);
                        }
                    }
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 20, 20);
        if(PracticeServer.DATABASE) return;
        File file = new File(PracticeServer.plugin.getDataFolder(), "alignments.yml");
        YamlConfiguration config = new YamlConfiguration();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        try {
            config.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (config.getConfigurationSection("chaotic") != null) {
            for (String key : config.getConfigurationSection("chaotic").getKeys(false)) {
                time = config.getConfigurationSection("chaotic").getInt(key);
                chaotic.put(key, time);
            }
        }
        if (config.getConfigurationSection("neutral") != null) {
            for (String key : config.getConfigurationSection("neutral").getKeys(false)) {
                time = config.getConfigurationSection("neutral").getInt(key);
                neutral.put(key, time);
            }
        }
    }
    

    public void onDisable() {
        PracticeServer.log.info("[Alignments] has been disabled.");
        if(PracticeServer.DATABASE) return;
        File file = new File(PracticeServer.plugin.getDataFolder(), "alignments.yml");
        YamlConfiguration config = new YamlConfiguration();
        for (String s2 : chaotic.keySet()) {
            config.set("chaotic." + s2, chaotic.get(s2));
        }
        for (String s2 : neutral.keySet()) {
            config.set("neutral." + s2, neutral.get(s2));
        }
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChaoticSpawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (chaotic.containsKey(p.getName())) {
            p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " enter " + ChatColor.BOLD + "NON-PVP" + ChatColor.RED + " zones with a chaotic alignment.");
            e.setRespawnLocation(TeleportBooks.generateRandomSpawnPoint(p.getName()));
        } else {
            e.setRespawnLocation(TeleportBooks.DeadPeaks);
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onZoneMessage(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (isSafeZone(e.getFrom()) && chaotic.containsKey(p.getName())) {
            p.sendMessage(ChatColor.RED + "The guards have kicked you out of the " + ChatColor.UNDERLINE + "protected area" + ChatColor.RED + " due to your chaotic alignment.");
            ActionBar.sendActionBar(p, ChatColor.RED + "The guards have kicked you out of the " + ChatColor.UNDERLINE + "protected area" + ChatColor.RED + " due to your chaotic alignment.", 50);
            p.teleport(TeleportBooks.generateRandomSpawnPoint(p.getName()));
            return;
        }
        if (isSafeZone(e.getTo())) {
            if (chaotic.containsKey(p.getName())) {
                p.teleport(e.getFrom());
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " enter " + ChatColor.BOLD + "NON-PVP" + ChatColor.RED + " zones with a chaotic alignment.");
                ActionBar.sendActionBar(p, ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " enter " + ChatColor.BOLD + "NON-PVP" + ChatColor.RED + " zones with a chaotic alignment.", 3);
                return;
            }
            if (Listeners.combat.containsKey(p.getName()) && System.currentTimeMillis() - Listeners.combat.get(p.getName()) <= 10000) {
                p.teleport(e.getFrom());
                long combattime = Listeners.combat.get(p.getName());
                double left = (System.currentTimeMillis() - combattime) / 1000;
                int time = (int) (10 - Math.round(left));
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " leave a chaotic zone while in combat.");
                p.sendMessage(ChatColor.GRAY + "Out of combat in: " + ChatColor.BOLD + time + "s");
                ActionBar.sendActionBar(p, ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " leave a chaotic zone while in combat, " + ChatColor.GRAY + "Out of combat in: " + ChatColor.BOLD + time + "s", 3);
                return;
            }
        }
        if (!isSafeZone(e.getFrom()) && isSafeZone(e.getTo())) {
            StringUtil.sendCenteredMessage(p, ChatColor.GREEN.toString() + ChatColor.BOLD + "*** SAFE ZONE (DMG-OFF)***");
            ActionBar.sendActionBar(p, ChatColor.GREEN.toString() + ChatColor.BOLD + "*** SAFE ZONE ***" + ChatColor.GRAY + " (PVP-OFF) (MONSTERS-OFF)", 2);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25f, 0.3f);


        }
        if (isSafeZone(e.getFrom()) && !isSafeZone(e.getTo())) {
            StringUtil.sendCenteredMessage(p, ChatColor.RED.toString() + ChatColor.BOLD + "*** CHAOTIC ZONE (PVP-ON)***");
            ActionBar.sendActionBar(p, ChatColor.RED.toString() + ChatColor.BOLD + "*** CHAOTIC ZONE *** " + ChatColor.GRAY + "(PVP-ON) (MONSTERS-ON)", 2);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25f, 0.3f);
        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleportChaotic(PlayerTeleportEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player p = e.getPlayer();
        if (isSafeZone(e.getTo())) {
            if (chaotic.containsKey(p.getName())) {
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " enter " + ChatColor.BOLD + "NON-PVP" + ChatColor.RED + " zones with a chaotic alignment.");
                ActionBar.sendActionBar(p, ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " enter " + ChatColor.BOLD + "NON-PVP" + ChatColor.RED + " zones with a chaotic alignment.", 3);
                e.setCancelled(true);
                return;
            }
            if (Listeners.combat.containsKey(p.getName()) && System.currentTimeMillis() - Listeners.combat.get(p.getName()) <= 10000) {
                long combattime = Listeners.combat.get(p.getName());
                double left = (System.currentTimeMillis() - combattime) / 1000;
                int time = (int) (10 - Math.round(left));
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " leave a chaotic zone while in combat.");
                p.sendMessage(ChatColor.GRAY + "Out of combat in: " + ChatColor.BOLD + time + "s");
                ActionBar.sendActionBar(p, ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " leave a chaotic zone while in combat, " + ChatColor.GRAY + "Out of combat in: " + ChatColor.BOLD + time + "s", 3);
                e.setCancelled(true);
                return;
            }
        }
        if (!isSafeZone(e.getFrom()) && isSafeZone(e.getTo())) {
            StringUtil.sendCenteredMessage(p, ChatColor.GREEN.toString() + ChatColor.BOLD + "*** SAFE ZONE (DMG-OFF)***");
            ActionBar.sendActionBar(p, ChatColor.GREEN.toString() + ChatColor.BOLD + "*** SAFE ZONE ***" + ChatColor.GRAY + " (PVP-OFF) (MONSTERS-OFF)", 2);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25f, 0.3f);


        }
        if (isSafeZone(e.getFrom()) && !isSafeZone(e.getTo())) {
            StringUtil.sendCenteredMessage(p, ChatColor.RED.toString() + ChatColor.BOLD + "*** CHAOTIC ZONE (PVP-ON)***");
            ActionBar.sendActionBar(p, ChatColor.RED.toString() + ChatColor.BOLD + "*** CHAOTIC ZONE *** " + ChatColor.GRAY + "(PVP-ON) (MONSTERS-ON)", 2);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25f, 0.3f);
        }
    }

    public static boolean isSafeZone(Location loc) {
        ApplicableRegionSet locset = WGBukkit.getRegionManager(loc.getWorld()).getApplicableRegions(loc);
        return locset.queryState(null, DefaultFlag.PVP) == StateFlag.State.DENY;
    }

    public static void updatePlayerAlignment(Player p) {
        ChatColor cc = ChatColor.GRAY;
        cc = ModerationMechanics.getRank(p) == RankEnum.DEV ? ChatColor.GOLD : (ModerationMechanics.getRank(p) == RankEnum.MANAGER ? ChatColor.YELLOW : (ModerationMechanics.getRank(p) == RankEnum.GM ? ChatColor.AQUA : (neutral.containsKey(p.getName()) ? ChatColor.YELLOW : (chaotic.containsKey(p.getName()) ? ChatColor.RED : ChatColor.GRAY))));
        p.setDisplayName(cc + p.getName());
        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 10.0f, 1.0f);
        Scoreboards.updateAllColors();
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        new BukkitRunnable() {
            public void run() {
                updatePlayerAlignment(p);
                Scoreboards.updatePlayerHealth();
            }
        }.runTaskLater(PracticeServer.getInstance(), 1L);
    }

    public void onMobSpawn(CreatureSpawnEvent event) {
        if(event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onNeutral(EntityDamageByEntityEvent e) {
        if (e.getDamage() <= 0.0) {
            return;
        }
        if(Duels.stayLawful.containsKey(e.getDamager())) return;
        if (e.getDamager() instanceof Projectile && e.getEntity() instanceof Player) {
            if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
                Player d = (Player) ((Projectile) e.getDamager()).getShooter();
                Projectile projectile = (Projectile) e.getDamager();
                if (!chaotic.containsKey(d.getName())) {
                    if(Toggles.getToggles(d.getUniqueId()).contains("Anti PVP")) return;
                    if (neutral.containsKey(d.getName())) {
                        neutral.put(d.getName(), NEUTRAL_SECONDS);
                    } else {
                        d.sendMessage(ChatColor.YELLOW + "          * YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *");
                        d.sendMessage(ChatColor.GRAY + "While neutral, players who kill you will not become chaotic. You have a 50% chance of dropping your weapon, and a 25% chance of dropping each piece of equiped armor on death. Neutral alignment will expire 2 minutes after last hit on player.");
                        d.sendMessage(ChatColor.YELLOW + "          * YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *");
                        ActionBar.sendActionBar(d, ChatColor.YELLOW + "* YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *", 3);
                        neutral.put(d.getName(), NEUTRAL_SECONDS);
                        updatePlayerAlignment(d);
                    }
                }
            }
        }

        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Player d = (Player) e.getDamager();
            if (!chaotic.containsKey(d.getName())) {
                if(Toggles.getToggles(d.getUniqueId()).contains("Anti PVP")) return;
                if(Duels.duelers.containsKey(d)) return;
                if (neutral.containsKey(d.getName())) {
                    neutral.put(d.getName(), NEUTRAL_SECONDS);
                } else {
                    d.sendMessage(ChatColor.YELLOW + "          * YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *");
                    d.sendMessage(ChatColor.GRAY + "While neutral, players who kill you will not become chaotic. You have a 50% chance of dropping your weapon, and a 25% chance of dropping each piece of equiped armor on death. Neutral alignment will expire 2 minutes after last hit on player.");
                    d.sendMessage(ChatColor.YELLOW + "          * YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *");
                    ActionBar.sendActionBar(d, ChatColor.YELLOW + "* YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *", 3);
                    neutral.put(d.getName(), NEUTRAL_SECONDS);
                    updatePlayerAlignment(d);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChaotic(PlayerDeathEvent e) {

        int time;
        Player p = e.getEntity();
        if (logout){
            logout = false;
            return;
        }
        if (!Damage.lastphit.containsKey(p)) {
            return;
        }
        if (Damage.lasthit.containsKey(p) && System.currentTimeMillis() - Damage.lasthit.get(p) > 8000) {
            return;
        }
        Player d = Damage.lastphit.get(p);
        if(Duels.stayLawful.containsKey(p)) return;
        if (!neutral.containsKey(p.getName()) && !chaotic.containsKey(p.getName())) {
            if (chaotic.containsKey(d.getName())) {
                time = chaotic.get(d.getName());
                if(time + CHAOTIC_SECONDS > 1200) {
                    chaotic.put(d.getName(), CHAOTIC_SECONDS);
                }else{
                    chaotic.put(d.getName(), CHAOTIC_SECONDS);
                }
                d.sendMessage("\u00a7cLAWFUL player slain, chaotic timer reset.");
                ActionBar.sendActionBar(d, ChatColor.RED + "* YOU ARE NOW " + ChatColor.BOLD + "CHAOTIC" + ChatColor.RED + " ALIGNMENT *", 5);
                neutral.remove(d.getName());
                updatePlayerAlignment(d);
            } else {
                d.sendMessage(ChatColor.RED + "          * YOU ARE NOW " + ChatColor.BOLD + "CHAOTIC" + ChatColor.RED + " ALIGNMENT *");
                d.sendMessage(ChatColor.GRAY + "While chaotic, you cannot enter any major cities or safe zones. If you are killed while chaotic, you will lose everything in your inventory. Chaotic alignment will expire 10 minutes after your last player kill.");
                d.sendMessage(ChatColor.RED + "          * YOU ARE NOW " + ChatColor.BOLD + "CHAOTIC" + ChatColor.RED + " ALIGNMENT *");
                d.sendMessage(ChatColor.RED + "LAWFUL player slain, " + ChatColor.BOLD + "+" + CHAOTIC_SECONDS + "s" + ChatColor.RED + " added to Chaotic timer.");
                ActionBar.sendActionBar(d, ChatColor.RED + "* YOU ARE NOW " + ChatColor.BOLD + "CHAOTIC" + ChatColor.RED + " ALIGNMENT *", 5);
                chaotic.put(d.getName(), CHAOTIC_SECONDS);
                neutral.remove(d.getName());
                updatePlayerAlignment(d);
            }
        }
//      t  if (neutral.containsKey(p.getName()) && !chaotic.containsKey(p.getName()) && chaotic.containsKey(d.getName())) {
//            time = chaotic.get(d.getName());
//            if(time + (CHAOTIC_SECONDS / 2) > 1200) {
//       R         chaotic.put(d.getName(), 1200);
//            }else{
//                chaotic.put(d.getName(), time + (CHAOTIC_SECONDS / 2));
//            }
//            d.sendMessage(ChatColor.RED + "NEUTRAL player slain, " + ChatColor.BOLD + "+" + ((CHAOTIC_SECONDS / 2)) + "s" + ChatColor.RED + " added to Chaotic timer.");
//            neutral.remove(d.getName());
//            updatePlayerAlignment(d);
//        }
//        if (chaotic.containsKey(p.getName()) && chaotic.containsKey(d.getName())) {
//            time = chaotic.get(d.getName());
//            if (time <= 300) {
//                chaotic.remove(d.getName());
//                neutral.put(d.getName(), NEUTRAL_SECONDS);
//                updatePlayerAlignment(d);
//                d.sendMessage("\u00a7cCHAOTIC player slain, \u00a7l-300s \u00a7ctaken to Chaotic timer");
//                d.sendMessage(ChatColor.YELLOW + "          * YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *");
//                d.sendMessage(ChatColor.GRAY + "While neutral, players who kill you will not become chaotic. You have a 50% chance of dropping your weapon, and a 25% chance of dropping each piece of equiped armor on death. Neutral alignment will expire 2 minutes after last hit on player.");
//                d.sendMessage(ChatColor.YELLOW + "          * YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *");
//            } else {
//                chaotic.put(d.getName(), time -= 300);
//                d.sendMessage(ChatColor.GREEN + "Chaotic player slain, " + ChatColor.BOLD + "-300s" + ChatColor.GREEN + " removed from Chatoic timer.");
//            }
//        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeathMessage(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if(p == null) return;
        String reason = " has died";
        ItemStack weapon = null;
        if (p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null) {
            EntityDamageByEntityEvent et;
            if (p.getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.LAVA) || p.getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.FIRE) || p.getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK)) {
                reason = " burned to death";
            }
            if (p.getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.SUICIDE)) {
                reason = " ended their own life";
            }
            if (p.getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                reason = " fell to their death";
            }
            if (p.getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION)) {
                reason = " was crushed to death";
            }
            if (p.getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.DROWNING)) {
                reason = " drowned to death";
            }
            GuildPlayer guildPlayerPlayer = GuildPlayers.getInstance().get(p.getUniqueId());
            if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent && (et = (EntityDamageByEntityEvent) p.getLastDamageCause()).getDamager() instanceof LivingEntity) {
                if(et.getDamager() instanceof Player) {
                    Player killer = (Player) et.getDamager();
                    reason = " was killed by " + ChatColor.RESET + killer.getDisplayName();
                    if (killer.getInventory().getItemInMainHand().getType() != Material.AIR || killer.getInventory().getItemInMainHand().getType() != null) {
                        weapon = killer.getInventory().getItemInMainHand();
                        reason = " was killed by " + killer.getDisplayName() + ChatColor.WHITE + " with a(n) ";
                        if (WepTrak.isStatTrak(weapon)) {
                            WepTrak.incrementStat(weapon, "pk");
                        }
                    }
                    GuildPlayer guildPlayerKiller = GuildPlayers.getInstance().get(killer.getUniqueId());
                    guildPlayerKiller.setPlayerKills((guildPlayerKiller.getPlayerKills() + 1));

                } else if (et.getDamager() instanceof LivingEntity) {
                    LivingEntity l = (LivingEntity) et.getDamager();
                    String name = "";
                    if (l.hasMetadata("name")) {
                        name = l.getMetadata("name").get(0).asString();
                    }
                    reason = " was killed by a(n) " + ChatColor.UNDERLINE + name;

                }
            }
            if(guildPlayerPlayer != null) guildPlayerPlayer.setDeaths((guildPlayerPlayer.getDeaths() + 1));

            final JSONMessage normal = new JSONMessage(p.getDisplayName() + ChatColor.RESET + reason, ChatColor.WHITE);
            List<String> hoveredChat = new ArrayList<>();
            ItemMeta meta;
            try{
                meta = weapon.getItemMeta();
            }catch(Exception ex){
                System.out.println("Null Weapon on Death");
                return;
            }
            hoveredChat.add(meta.getDisplayName());
            if (meta.hasLore()) hoveredChat.addAll(meta.getLore());
            normal.addHoverText(hoveredChat, ChatColor.getLastColors(weapon.getItemMeta().getDisplayName()) + ChatColor.BOLD.toString() +  ChatColor.UNDERLINE + "SHOW");
            for (Entity near : p.getNearbyEntities(50.0, 50.0, 50.0)) {
                if (near instanceof Player) {
                    Player nearPlayers = (Player) near;
                    if (weapon != null && normal != null) {
                        normal.sendToPlayer(nearPlayers);
                    }else{
                        nearPlayers.sendMessage(p.getDisplayName() + ChatColor.RESET + reason);
                    }
                }
            }
        }
    }

    public static void setLawful(Player p){
        chaotic.remove(p.getName());
        neutral.remove(p.getName());
        updatePlayerAlignment(p);
        updatePlayerAlignment(p);
        p.sendMessage(ChatColor.GREEN + "          * YOU ARE NOW " + ChatColor.BOLD + "LAWFUL" + ChatColor.GREEN + " ALIGNMENT *");
        p.sendMessage(ChatColor.GRAY + "While lawful, you will not lose any equipped armor on death, instead, all armor will lose 30% of its durability when you die. Any players who kill you while you're lawfully aligned will become chaotic.");
    }

    public static void setNeutral(Player p){
        chaotic.remove(p.getName());
        neutral.put(p.getName(), 120);
        updatePlayerAlignment(p);
        p.sendMessage(ChatColor.YELLOW + "          * YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *");
        p.sendMessage(ChatColor.GRAY + "While neutral, players who kill you will not become chaotic. You have a 50% chance of dropping your weapon, and a 25% chance of dropping each piece of equiped armor on death. Neutral alignment will expire 2 minutes after last hit on player.");
        updatePlayerAlignment(p);
    }

    public static void setChaotic(Player p, int time){
        chaotic.put(p.getName(), time);
        p.sendMessage(ChatColor.RED + "          * YOU ARE NOW " + ChatColor.BOLD + "CHAOTIC" + ChatColor.RED + " ALIGNMENT *");
        p.sendMessage(ChatColor.GRAY + "While chaotic, you cannot enter any major cities or safe zones. If you are killed while chaotic, you will lose everything in your inventory. Chaotic alignment will expire 10 minutes after your last player kill.");
        updatePlayerAlignment(p);

    }


    public static String get(Player player) {
        if (chaotic.containsKey(player.getName()))
            return "&cCHAOTIC";
        if (neutral.containsKey(player.getName()))
            return "&eNEUTRAL";

        return "&aLAWFUL";
    }

    public static int getAlignTime(Player p) {
        if (chaotic.containsKey(p.getName())) {
            return Alignments.chaotic.get(p.getName());
        }
        if (neutral.containsKey(p.getName())) {
            return Alignments.neutral.get(p.getName());
        } else {
            return 0;
        }
    }

}

