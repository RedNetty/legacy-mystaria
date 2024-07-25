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
import me.retrorealms.practiceserver.mechanics.player.PersistentPlayers;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Alignments implements Listener {
    private static final int NEUTRAL_SECONDS = 120;
    private static final int CHAOTIC_SECONDS = 300;
    public static ConcurrentHashMap<String, Integer> neutral = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Integer> chaotic = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Long> tagged = new ConcurrentHashMap<>();
    public static boolean logout = false;
    public static HashMap<Player, BossBar> playerBossBars;
    private static final Map<UUID, Location> lastSafeLocations = new HashMap<>();

    public static boolean isSafeZone(Location loc) {
        ApplicableRegionSet locset = WGBukkit.getRegionManager(loc.getWorld()).getApplicableRegions(loc);
        return locset.queryState(null, DefaultFlag.PVP) == StateFlag.State.DENY;
    }

    public static void updatePlayerAlignment(Player p) {
        try {
            ChatColor cc = ChatColor.GRAY;
            RankEnum rank = ModerationMechanics.getRank(p);

            if (rank == RankEnum.DEV) {
                cc = ChatColor.GOLD;
            } else if (rank == RankEnum.MANAGER) {
                cc = ChatColor.YELLOW;
            } else if (rank == RankEnum.GM) {
                cc = ChatColor.AQUA;
            } else if (neutral.containsKey(p.getName())) {
                cc = ChatColor.YELLOW;
            } else if (chaotic.containsKey(p.getName())) {
                cc = ChatColor.RED;
            }

            p.setDisplayName(cc + p.getName());

            Bukkit.getScheduler().runTask(PracticeServer.getInstance(), () -> {
                try {
                    p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 10.0f, 1.0f);
                } catch (Exception e) {
                    PracticeServer.log.warning("Failed to play alignment update sound for " + p.getName() + ": " + e.getMessage());
                }
            });

            Scoreboards.updateAllColors();
            PracticeServer.log.info("Updated alignment for player " + p.getName() + " to " + cc.name());
        } catch (Exception e) {
            PracticeServer.log.severe("Error updating alignment for player " + p.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setLawful(Player p) {
        chaotic.remove(p.getName());
        neutral.remove(p.getName());
        updatePlayerAlignment(p);
        StringUtil.sendCenteredMessage(p, ChatColor.GREEN + "* YOU ARE NOW " + ChatColor.BOLD + "LAWFUL" + ChatColor.GREEN + " ALIGNMENT *");
        StringUtil.sendCenteredMessage(p, ChatColor.GRAY + "While lawful, you will not lose any equipped armor on death.");
        StringUtil.sendCenteredMessage(p, ChatColor.GRAY + "Instead, all armor will lose 30% of its durability when you die. ");
        StringUtil.sendCenteredMessage(p, ChatColor.GRAY + "Any players who kill you while you're lawfully aligned will become chaotic.");
        StringUtil.sendCenteredMessage(p, ChatColor.GREEN + "* YOU ARE NOW " + ChatColor.BOLD + "LAWFUL" + ChatColor.GREEN + " ALIGNMENT *");
    }

    public static void setNeutral(Player p) {
        chaotic.remove(p.getName());
        neutral.put(p.getName(), NEUTRAL_SECONDS);
        updatePlayerAlignment(p);
        StringUtil.sendCenteredMessage(p, ChatColor.YELLOW + "* YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *");
        StringUtil.sendCenteredMessage(p, ChatColor.GRAY + "While neutral, players who kill you will not become chaotic.");
        StringUtil.sendCenteredMessage(p, ChatColor.GRAY + "You have a 50% chance of dropping your weapon, and a 25% chance of dropping each piece of equipped armor on death.");
        StringUtil.sendCenteredMessage(p, ChatColor.GRAY + "Neutral alignment will expire 2 minutes after last hit on player.");
        StringUtil.sendCenteredMessage(p, ChatColor.YELLOW + "* YOU ARE NOW " + ChatColor.BOLD + "NEUTRAL" + ChatColor.YELLOW + " ALIGNMENT *");
    }

    public static void setChaotic(Player p, int time) {
        neutral.remove(p.getName());
        chaotic.put(p.getName(), time);
        StringUtil.sendCenteredMessage(p, ChatColor.RED + "* YOU ARE NOW " + ChatColor.BOLD + "CHAOTIC" + ChatColor.RED + " ALIGNMENT *");
        StringUtil.sendCenteredMessage(p, ChatColor.GRAY + "While chaotic, you cannot enter any major cities or safe zones. If you are killed while chaotic, you will lose everything in your inventory. Chaotic alignment will expire 5 minutes after your last player kill.");
        StringUtil.sendCenteredMessage(p, ChatColor.RED + "* YOU ARE NOW " + ChatColor.BOLD + "CHAOTIC" + ChatColor.RED + " ALIGNMENT *");
        updatePlayerAlignment(p);
    }

    public static String get(Player player) {
        if (chaotic.containsKey(player.getName())) return "&cCHAOTIC";
        if (neutral.containsKey(player.getName())) return "&eNEUTRAL";
        return "&aLAWFUL";
    }

    public static int getAlignTime(Player p) {
        if (chaotic.containsKey(p.getName())) {
            return Alignments.chaotic.get(p.getName());
        }
        if (neutral.containsKey(p.getName())) {
            return Alignments.neutral.get(p.getName());
        }
        return 0;
    }

    public void onEnable() {
        playerBossBars = Maps.newHashMap();
        PracticeServer.log.info("[Alignments] has been enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        startAlignmentTask();
        loadAlignments();
    }

    private void startAlignmentTask() {
        new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (p.isOnline()) {
                        if (isSafeZone(p.getLocation()) && isPlayerChaotic(p)) {
                            kickPlayerFromSafeZone(p);
                            return;
                        }
                        updateTimeCounters(p);
                        updatePlayerHealthBar(p);
                        if (isPlayerTagged(p)) continue;
                        healPlayer(p);
                    }
                }
            }
        }.runTaskTimer(PracticeServer.plugin, 20, 20);
    }

    private void loadAlignments() {
        if (PracticeServer.DATABASE) return;
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
                int time = config.getConfigurationSection("chaotic").getInt(key);
                chaotic.put(key, time);
            }
        }
        if (config.getConfigurationSection("neutral") != null) {
            for (String key : config.getConfigurationSection("neutral").getKeys(false)) {
                int time = config.getConfigurationSection("neutral").getInt(key);
                neutral.put(key, time);
            }
        }
    }

    public void onDisable() {
        PracticeServer.log.info("[Alignments] has been disabled.");
        if (PracticeServer.DATABASE) return;
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

    private void updateTimeCounters(Player p) {
        if (chaotic.containsKey(p.getName())) {
            int time = chaotic.get(p.getName());
            if (time <= 1) {
                chaotic.remove(p.getName());
                neutral.put(p.getName(), NEUTRAL_SECONDS);
                updatePlayerAlignment(p);
                setNeutral(p);
            } else {
                chaotic.put(p.getName(), --time);
            }
        }
        if (neutral.containsKey(p.getName())) {
            int time = neutral.get(p.getName());
            if (time == 1) {
                neutral.remove(p.getName());
                updatePlayerAlignment(p);
                setLawful(p);
            } else {
                neutral.put(p.getName(), --time);
            }
        }
    }

    private boolean isPlayerTagged(Player p) {
        return tagged.containsKey(p.getName()) &&
                (System.currentTimeMillis() - tagged.get(p.getName()) <= 10000) ||
                p.getHealth() <= 0.0;
    }

    private void healPlayer(Player p) {
        PlayerInventory i = p.getInventory();
        double amt = 5.0;
        int vit = 0;
        ItemStack[] armorContents = i.getArmorContents();
        for (ItemStack is : armorContents) {
            if (is != null && is.getType() != Material.AIR && is.hasItemMeta() && is.getItemMeta().hasLore()) {
                double added = Damage.getHps(is);
                amt += added;
                int addedvit = Damage.getElem(is, "VIT");
                vit += addedvit;
            }
        }
        if (vit > 0) {
            amt += (int) Math.round((double) vit * 0.1);
        }
        double healthToSet = p.getHealth() + amt;
        p.setHealth(Math.min(healthToSet, p.getMaxHealth()));
    }

    private void updatePlayerHealthBar(Player p) {
        double healthPercentage = (p.getHealth() / p.getMaxHealth());
        float pcnt = (float) (healthPercentage * 1.F);
        BarColor barColor = Damage.getBarColor(p);
        ChatColor titleColor = Damage.barTitleColor(p);
        if (Toggles.isToggled(p, "Level HP")) {
            if (!playerBossBars.containsKey(p)) {
                BossBar bossBar = Bukkit.createBossBar(titleColor + String.valueOf(ChatColor.BOLD) + titleColor + (int) p.getHealth() + titleColor + ChatColor.BOLD + " / " + titleColor + (int) p.getMaxHealth() + " HP ", barColor, BarStyle.SOLID);
                bossBar.addPlayer(p);
                playerBossBars.put(p, bossBar);
                playerBossBars.get(p).setProgress(pcnt);
            } else {
                playerBossBars.get(p).setTitle(titleColor + String.valueOf(ChatColor.BOLD) + titleColor + (int) p.getHealth() + titleColor + ChatColor.BOLD + " / " + titleColor + (int) p.getMaxHealth() + " HP ");
                playerBossBars.get(p).setProgress(pcnt);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChaoticSpawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (isPlayerChaotic(p)) {
            sendChaoticMessage(p);
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
        handleZoneChange(e, p);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        handlePlayerMovement(event.getPlayer(), event.getTo(), event.getFrom());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleportChaotic(PlayerTeleportEvent event) {
        handlePlayerMovement(event.getPlayer(), event.getTo(), event.getFrom());
    }

    private void handlePlayerMovement(Player player, Location to, Location from) {
        if (to == null || from == null || to.equals(from)) {
            return;
        }

        if (isSafeZone(to)) {
            if (isPlayerChaotic(player)) {
                if (!isSafeZone(from)) {
                    preventPlayerEnteringSafeZone(player, from);
                }
            } else if (isPlayerInCombat(player)) {
                if (!isSafeZone(from)) {
                    preventPlayerLeavingCombatZone(player, from);
                }
            } else {
                lastSafeLocations.put(player.getUniqueId(), to);
            }
        } else {
            lastSafeLocations.put(player.getUniqueId(), from);
        }
    }

    private void preventPlayerEnteringSafeZone(Player player, Location from) {
        if (!isSafeZone(player.getLocation())) {
            Location safeLocation = lastSafeLocations.getOrDefault(player.getUniqueId(), from);
            player.teleport(safeLocation);
            sendChaoticMessage(player);
        }
    }

    private void preventPlayerLeavingCombatZone(Player player, Location from) {
        if (!isSafeZone(player.getLocation())) {
            Location safeLocation = lastSafeLocations.getOrDefault(player.getUniqueId(), from);
            player.teleport(safeLocation);

            if (Listeners.combat.containsKey(player.getName())) {
                long combatTime = Listeners.combat.get(player.getName());
                double left = (System.currentTimeMillis() - combatTime) / 1000.0;
                int time = (int) (10 - Math.round(left));
                StringUtil.sendCenteredMessage(player, ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " leave a chaotic zone while in combat.");
                StringUtil.sendCenteredMessage(player, ChatColor.GRAY + "Out of combat in: " + ChatColor.BOLD + time + "s");
                ActionBar.sendActionBar(player, ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " leave a chaotic zone while in combat", 3);
            } else {
                StringUtil.sendCenteredMessage(player, ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " leave a chaotic zone.");
                ActionBar.sendActionBar(player, ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " leave a chaotic zone", 3);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        new BukkitRunnable() {
            public void run() {
                if (p.isDead()) return;
                updatePlayerAlignment(p);
                Scoreboards.updatePlayerHealth();
            }
        }.runTaskLater(PracticeServer.getInstance(), 1L);
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onNeutral(EntityDamageByEntityEvent e) {
        handleEntityDamage(e);
    }

    private boolean isPlayerChaotic(Player p) {
        return chaotic.containsKey(p.getName());
    }

    private void sendChaoticMessage(Player p) {
        StringUtil.sendCenteredMessage(p, ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " enter " + ChatColor.BOLD + "NON-PVP" + ChatColor.RED + " zones with a chaotic alignment.");
    }

    private void handleZoneChange(PlayerMoveEvent e, Player p) {
        if (isSafeZone(e.getTo())) {
            handlePlayerEnteringSafeZone(e, p);
        }
        if (!isSafeZone(e.getFrom()) && isSafeZone(e.getTo())) {
            notifyPlayerEnteredSafeZone(p);
        }
        if (isSafeZone(e.getFrom()) && !isSafeZone(e.getTo())) {
            notifyPlayerEnteredChaoticZone(p);
        }
    }

    private void handleEntityDamage(EntityDamageByEntityEvent e) {
        if (e.getDamage() <= 0.0) {
            return;
        }
        if (Duels.stayLawful.containsKey(e.getDamager())) return;
        if (e.getDamager() instanceof Projectile && e.getEntity() instanceof Player) {
            handleProjectileDamage(e);
        }
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            handlePlayerDamage(e);
        }
    }

    private void handlePlayerEnteringSafeZone(PlayerMoveEvent e, Player p) {
        if (isPlayerChaotic(p)) {
            preventPlayerEnteringSafeZone(e, p);
            return;
        }
        if (isPlayerInCombat(p)) {
            preventPlayerLeavingCombatZone(e, p);
        }
    }

    private void handleProjectileDamage(EntityDamageByEntityEvent e) {
        if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
            Player d = (Player) ((Projectile) e.getDamager()).getShooter();
            if (!isPlayerChaotic(d)) {
                if (isPlayerAntiPVP(d)) return;
                if (isPlayerNeutral(d)) {
                    resetNeutralTimer(d);
                } else {
                    setNeutral(d);
                }
            }
        }
    }

    private void preventPlayerEnteringSafeZone(PlayerMoveEvent e, Player p) {
        p.teleport(e.getFrom());
        sendChaoticMessage(p);
        ActionBar.sendActionBar(p, ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " enter " + ChatColor.BOLD + "NON-PVP" + ChatColor.RED + " zones with a chaotic alignment.", 3);
    }

    private void preventPlayerLeavingCombatZone(PlayerMoveEvent e, Player p) {
        p.teleport(e.getFrom());
        long combattime = Listeners.combat.get(p.getName());
        double left = (System.currentTimeMillis() - combattime) / 1000;
        int time = (int) (10 - Math.round(left));
        StringUtil.sendCenteredMessage(p, ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " leave a chaotic zone while in combat.");
        StringUtil.sendCenteredMessage(p, ChatColor.GRAY + "Out of combat in: " + ChatColor.BOLD + time + "s");
        ActionBar.sendActionBar(p, ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " leave a chaotic zone while in combat", 3);
    }

    private void notifyPlayerEnteredSafeZone(Player p) {
        StringUtil.sendCenteredMessage(p, ChatColor.GREEN.toString() + ChatColor.BOLD + "*** SAFE ZONE (DMG-OFF)***");
        ActionBar.sendActionBar(p, ChatColor.GREEN.toString() + ChatColor.BOLD + "*** SAFE ZONE ***" + ChatColor.GRAY + " (PVP-OFF) (MONSTERS-OFF)", 2);
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25f, 0.3f);
    }

    private void notifyPlayerEnteredChaoticZone(Player p) {
        StringUtil.sendCenteredMessage(p, ChatColor.RED.toString() + ChatColor.BOLD + "*** CHAOTIC ZONE (PVP-ON)***");
        ActionBar.sendActionBar(p, ChatColor.RED.toString() + ChatColor.BOLD + "*** CHAOTIC ZONE *** " + ChatColor.GRAY + "(PVP-ON) (MONSTERS-ON)", 2);
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25f, 0.3f);
    }

    private boolean isPlayerInCombat(Player p) {
        return Listeners.combat.containsKey(p.getName()) && System.currentTimeMillis() - Listeners.combat.get(p.getName()) <= 10000;
    }

    private boolean isPlayerAntiPVP(Player p) {
        return Toggles.isToggled(p, "Anti PVP");
    }

    private boolean isPlayerInDuel(Player p) {
        return Duels.duelers.containsKey(p);
    }

    private boolean isPlayerNeutral(Player p) {
        return neutral.containsKey(p.getName());
    }

    private void resetNeutralTimer(Player p) {
        neutral.put(p.getName(), NEUTRAL_SECONDS);
    }

    private void handlePlayerDamage(EntityDamageByEntityEvent e) {
        Player d = (Player) e.getDamager();
        if (!isPlayerChaotic(d)) {
            if (isPlayerAntiPVP(d)) return;
            if (isPlayerInDuel(d)) return;
            if (isPlayerNeutral(d)) {
                resetNeutralTimer(d);
            } else {
                setNeutral(d);
            }
        }
    }

    private void kickPlayerFromSafeZone(Player p) {
        StringUtil.sendCenteredMessage(p, ChatColor.RED + "The guards have kicked you out of the " + ChatColor.UNDERLINE + "protected area" + ChatColor.RED + " due to your chaotic alignment.");
        ActionBar.sendActionBar(p, ChatColor.RED + "The guards have kicked you out of the " + ChatColor.UNDERLINE + "protected area" + ChatColor.RED + " due to your chaotic alignment.", 50);
        p.teleport(TeleportBooks.generateRandomSpawnPoint(p.getName()));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChaotic(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (logout) {
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
        if (Duels.stayLawful.containsKey(p)) return;
        if (!neutral.containsKey(p.getName()) && !chaotic.containsKey(p.getName())) {
            if (chaotic.containsKey(d)) {
                chaotic.put(d.getName(), CHAOTIC_SECONDS);
                StringUtil.sendCenteredMessage(d, "&cPlayer slain, chaotic timer reset.");
            } else {
                setChaotic(d, CHAOTIC_SECONDS);
                ActionBar.sendActionBar(d, ChatColor.RED + "* YOU ARE NOW " + ChatColor.BOLD + "CHAOTIC" + ChatColor.RED + " ALIGNMENT *", 5);
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeathMessage(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (p == null) return;
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
                if (et.getDamager() instanceof Player) {
                    Player killer = (Player) et.getDamager();
                    if(PersistentPlayers.getCurrentQuest(killer).startsWith("Defeat")) {
                        PersistentPlayers.updateQuestProgress(killer, 1);
                    }
                    reason = " was killed by " + ChatColor.RESET + killer.getDisplayName();
                    if (killer.getInventory().getItemInMainHand().getType() != Material.AIR || killer.getInventory().getItemInMainHand().getType() != null) {
                        weapon = killer.getInventory().getItemInMainHand();
                        reason = " was killed by " + killer.getDisplayName() + ChatColor.WHITE + " with a(n) ";
                        if (WepTrak.isStatTrak(weapon)) {
                            WepTrak.incrementStat(weapon, "pk");
                        }
                    }
                    if (GuildPlayers.getInstance().get(killer.getUniqueId()) != null) {
                        GuildPlayer guildPlayerKiller = GuildPlayers.getInstance().get(killer.getUniqueId());
                        guildPlayerKiller.setPlayerKills((guildPlayerKiller.getPlayerKills() + 1));
                    }

                } else if (et.getDamager() instanceof LivingEntity) {
                    LivingEntity l = (LivingEntity) et.getDamager();
                    String name = "";
                    if (l.hasMetadata("name")) {
                        name = l.getMetadata("name").get(0).asString();
                    }
                    reason = " was killed by a(n) " + ChatColor.UNDERLINE + name;

                }
            }
            if (guildPlayerPlayer != null) guildPlayerPlayer.setDeaths((guildPlayerPlayer.getDeaths() + 1));

            final JSONMessage normal = new JSONMessage(p.getDisplayName() + ChatColor.RESET + reason, ChatColor.WHITE);
            List<String> hoveredChat = new ArrayList<>();
            ItemMeta meta;
            try {
                meta = weapon.getItemMeta();
            } catch (Exception ex) {
                return;
            }
            hoveredChat.add(meta.getDisplayName());
            if (meta.hasLore()) hoveredChat.addAll(meta.getLore());
            normal.addHoverText(hoveredChat, ChatColor.getLastColors(weapon.getItemMeta().getDisplayName()) + ChatColor.BOLD + ChatColor.UNDERLINE + "SHOW");
            for (Entity near : p.getNearbyEntities(50.0, 50.0, 50.0)) {
                if (near instanceof Player) {
                    Player nearPlayers = (Player) near;
                    if (weapon != null && normal != null) {
                        normal.sendToPlayer(nearPlayers);
                    } else {
                        nearPlayers.sendMessage(p.getDisplayName() + ChatColor.RESET + reason);
                    }
                }
            }
        }
    }

}

