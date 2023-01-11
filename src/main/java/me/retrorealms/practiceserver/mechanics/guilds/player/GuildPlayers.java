package me.retrorealms.practiceserver.mechanics.guilds.player;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.guilds.GuildMechanics;
import me.retrorealms.practiceserver.mechanics.guilds.guild.Guild;
import me.retrorealms.practiceserver.mechanics.guilds.guild.GuildManager;
import me.retrorealms.practiceserver.mechanics.money.Economy.Economy;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GuildPlayers {
    private static GuildPlayers instance;
    public static Map<UUID, GuildPlayer> guildPlayerMap;

    public static GuildPlayers getInstance() {
        if (instance == null) {
            instance = new GuildPlayers();
        }
        return instance;
    }

    public GuildPlayers() {
        instance = this;
        this.guildPlayerMap = new ConcurrentHashMap<>();
    }


    public List<GuildPlayer> getPendingInvitePlayers() {
        return guildPlayerMap.values().stream().filter(guildPlayer -> guildPlayer.getGuildInviteName() != null && (guildPlayer.getGuildInviteTime() != 0L) && (System.currentTimeMillis() > guildPlayer.getGuildInviteTime())).collect(Collectors.toList());
    }

    public void loadProfile(Player player) {
        if(!PracticeServer.DATABASE){
            new BukkitRunnable() {

                @Override
                public void run() {
                    File file = new File(GuildMechanics.getInstance().getDataFolder() + "/", "players.yml");
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                    if (configuration.isSet("players." + player.getUniqueId().toString())) {
                        ConfigurationSection section = configuration.getConfigurationSection("players." + player.getUniqueId().toString());
                        String guildName = section.getString("guildName");
                        int playerKills = section.getInt("stats.kills.player");
                        int gems = Economy.getBalance(player.getUniqueId());
                        int t1Kills = section.getInt("stats.kills.t1");
                        int t2Kills = section.getInt("stats.kills.t2");
                        int t3Kills = section.getInt("stats.kills.t3");
                        int t4Kills = section.getInt("stats.kills.t4");
                        int t5Kills = section.getInt("stats.kills.t5");
                        int t6Kills = section.getInt("stats.kills.t6");
                        int lootChestsOpen = section.getInt("stats.lootChests");
                        int oreMined = section.getInt("stats.oreMined");
                        int deaths = section.getInt("stats.deaths");
                        int fishCaught = section.getInt("stats.fishCaught");
                        configuration.set("players." + player.getUniqueId().toString() + ".username", player.getName());
                        add(new GuildPlayer(player.getUniqueId(), player.getName(), guildName, playerKills, t1Kills, t2Kills, t3Kills, t4Kills, t5Kills, t6Kills, lootChestsOpen, oreMined, deaths, fishCaught));
                    } else {
                        configuration.set("players." + player.getUniqueId().toString() + ".username", player.getName());
                        configuration.set("players." + player.getUniqueId().toString() + ".guildName", "");
                        configuration.set("players." + player.getUniqueId().toString() + ".stats.kills.t1", 0);
                        configuration.set("players." + player.getUniqueId().toString() + ".stats.kills.t2", 0);
                        configuration.set("players." + player.getUniqueId().toString() + ".stats.kills.t3", 0);
                        configuration.set("players." + player.getUniqueId().toString() + ".stats.kills.t4", 0);
                        configuration.set("players." + player.getUniqueId().toString() + ".stats.kills.t5", 0);
                        configuration.set("players." + player.getUniqueId().toString() + ".stats.kills.t6", 0);
                        configuration.set("players." + player.getUniqueId().toString() + ".stats.kills.player", 0);
                        configuration.set("players." + player.getUniqueId().toString() + ".stats.lootChests", 0);
                        configuration.set("players." + player.getUniqueId().toString() + ".stats.oreMined", 0);
                        configuration.set("players." + player.getUniqueId().toString() + ".stats.deaths", 0);
                        configuration.set("players." + player.getUniqueId().toString() + ".stats.fishCaught", 0);
                        add(new GuildPlayer(player.getUniqueId(), player.getName(), ""));
                    }
                    try {
                        configuration.save(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    GuildPlayer guildPlayer = get(player.getUniqueId());
                    if (guildPlayer.isInGuild()) {
                        Guild guild = GuildManager.getInstance().get(guildPlayer.getGuildName());
                        guild.sendMessage(ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + guild.getTag() + ChatColor.DARK_AQUA + "> " + player.getName() + ChatColor.DARK_AQUA + " has logged in");
                    }
                }
            }.runTaskAsynchronously(PracticeServer.getInstance());
        }
    }

    public void save(GuildPlayer guildPlayer) {
        if(!PracticeServer.DATABASE){
            if (this.guildPlayerMap.containsKey(guildPlayer.getUuid())) {

                File file = new File(GuildMechanics.getInstance().getDataFolder() + "/", "players.yml");
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                String name;
                if (!guildPlayer.isInGuild()) {
                    name = "";
                } else {
                    name = guildPlayer.getGuildName();
                }
                configuration.set("players." + guildPlayer.getUuid().toString() + ".username", guildPlayer.getUsername());
                configuration.set("players." + guildPlayer.getUuid().toString() + ".guildName", name);
                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.t1", guildPlayer.getT1Kills());
                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.t2", guildPlayer.getT2Kills());
                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.t3", guildPlayer.getT3Kills());
                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.t4", guildPlayer.getT4Kills());
                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.t5", guildPlayer.getT5Kills());
                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.t6", guildPlayer.getT6Kills());
                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.player", guildPlayer.getPlayerKills());
                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.lootChests", guildPlayer.getLootChestsOpen());
                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.oreMined", guildPlayer.getOreMined());
                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.deaths", guildPlayer.getDeaths());
                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.fishCaught", guildPlayer.getFishCaught());
                try {
                    configuration.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                remove(guildPlayer.getUuid());
            }
        }
    }

    public static void add(GuildPlayer guildPlayer) {
        GuildPlayers.guildPlayerMap.put(guildPlayer.getUuid(), guildPlayer);
    }

    public GuildPlayer get(UUID uuid) {
        return guildPlayerMap.get(uuid);
    }

    public void remove(UUID uuid) {
        if (this.guildPlayerMap.containsKey(uuid)) {
            this.guildPlayerMap.remove(uuid);
        }
    }

//    public void saveAll() {
//        if(PracticeServer.DATABASE){
//            for (GuildPlayer gp : this.guildPlayerMap.values()) {
//                if(Bukkit.getOfflinePlayer(gp.getUuid()).isOnline()){
//                    SQLMain.updateGuild(gp.getUuid(), gp.getGuildName());
//                }
//            }
//        }else{
//            File file = new File(GuildMechanics.getInstance().getDataFolder() + "/", "players.yml");
//            if (!file.exists()) {
//                try {
//                    file.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
//            for (GuildPlayer guildPlayer : this.guildPlayerMap.values()) {
//                String name;
//                if (!guildPlayer.isInGuild()) {
//                    name = "";
//                } else {
//                    name = guildPlayer.getGuildName();
//                }
//                configuration.set("players." + guildPlayer.getUuid().toString() + ".username", guildPlayer.getUsername());
//                configuration.set("players." + guildPlayer.getUuid().toString() + ".guildName", name);
//                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.t1", guildPlayer.getT1Kills());
//                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.t2", guildPlayer.getT2Kills());
//                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.t3", guildPlayer.getT3Kills());
//                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.t4", guildPlayer.getT4Kills());
//                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.t5", guildPlayer.getT5Kills());
//                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.t6", guildPlayer.getT6Kills());
//                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.kills.player", guildPlayer.getPlayerKills());
//                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.lootChests", guildPlayer.getLootChestsOpen());
//                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.oreMined", guildPlayer.getOreMined());
//                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.deaths", guildPlayer.getDeaths());
//                configuration.set("players." + guildPlayer.getUuid().toString() + ".stats.fishCaught", guildPlayer.getFishCaught());
//            }
//            try {
//                configuration.save(file);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }

    public void setGuildName(UUID uuid, String name) {
        if(PracticeServer.DATABASE) {
            SQLMain.updateGuild(uuid, name);
            if(guildPlayerMap.containsKey(uuid)) get(uuid).setGuildName(name);
            return;
        }
        if (!guildPlayerMap.containsKey(uuid)) {
            File file = new File(GuildMechanics.getInstance().getDataFolder() + "/", "players.yml");
            if (!file.exists()) {
                return;
            }
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            if (name == null) {
                name = "";
            }
            configuration.set("players." + uuid.toString() + ".guildName", name);
            try {
                configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            get(uuid).setGuildName(name);
            File file = new File(GuildMechanics.getInstance().getDataFolder() + "/", "players.yml");
            if (!file.exists()) {
                return;
            }
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            if (name == null) {
                name = "";
            }
            configuration.set("players." + uuid.toString() + ".guildName", name);
            try {
                configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<UUID> guildQuitPromptList = new ArrayList<>();

    public void addGuildQuitPrompt(UUID uuid) {
        if (!this.guildQuitPromptList.contains(uuid)) {
            this.guildQuitPromptList.add(uuid);
        }
    }

    public void removeGuildQuitPrompt(UUID uuid) {
        if (this.guildQuitPromptList.contains(uuid)) {
            this.guildQuitPromptList.remove(uuid);
        }
    }

    public boolean isGuildQuitPrompt(UUID uuid) {
        return guildQuitPromptList.contains(uuid);
    }
}
