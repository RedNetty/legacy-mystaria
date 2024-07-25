package me.retrorealms.practiceserver.mechanics.guilds.guild;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.guilds.GuildMechanics;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GuildManager {
    private static GuildManager instance;
    public static Map<String, Guild> guildMap;

    public static GuildManager getInstance() {
        if (instance == null) {
            instance = new GuildManager();
        }
        return instance;
    }

    public GuildManager() {
        instance = this;
        this.guildMap = new ConcurrentHashMap<>();
        if(PracticeServer.DATABASE) {
            SQLMain.loadGuilds();
            Bukkit.getScheduler().scheduleAsyncRepeatingTask(PracticeServer.getInstance(), this::updateCache, 3600L, 3600L);
            return;
        }
        File file = new File(GuildMechanics.getInstance().getDataFolder() + "/guild/");
        if (!file.exists()) {
            file.mkdirs();
        } else {
            if (file.listFiles().length > 0) {
                for (File file1 : file.listFiles()) {
                    if (file1.isDirectory()) continue;
                    String name = file1.getName().replaceAll(".yml", "").trim();
                    Guild guild = new Guild(name);
                    this.guildMap.put(name, guild);
                }
            }
        }
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(PracticeServer.getInstance(), this::updateCache, 300L, 300L);
    }

    private void updateCache() {
        guildMap = new ConcurrentHashMap<>();
        if(PracticeServer.DATABASE) SQLMain.loadGuilds();
        this.guildMap.values().forEach(guild -> guild.getPlayerRoleMap().keySet().forEach(uuid -> {
            if (GuildPlayers.getInstance().get(uuid) == null) {
                if (guild.getOnlineList().contains(uuid)) {
                    guild.removeOnline(uuid);
                }
            } else {
                if (!guild.getOnlineList().contains(uuid)) {
                    guild.addOnline(uuid);
                }
            }
        }));
    }

    public boolean isGuild(String name) {
        return guildMap.containsKey(name);
    }

    public void createGuild(String name, String tag, UUID owner) {
        Guild guild = new Guild(name, tag, owner);
        this.guildMap.put(name, guild);
    }

    public void disbandGuild(Guild guild) {
        this.guildMap.remove(guild.getName());
        if(PracticeServer.DATABASE){
            SQLMain.deleteGuild(guild);
            return;
        }
        File file = new File(GuildMechanics.getInstance().getDataFolder() + "/guild/", guild.getName() + ".yml");
        if (file.delete()) {
            for (UUID uuid : guild.getPlayerRoleMap().keySet()) {
                GuildPlayers.getInstance().setGuildName(uuid, "");
            }
        }
    }

    public boolean isGuild(String name, String tag) {
        if (isGuild(name)) {
            return true;
        }
        List<Guild> collect = guildMap.values().stream().filter(guild -> guild.getTag().equalsIgnoreCase(tag)).collect(Collectors.toList());
        if (collect.size() > 0) {
            return true;
        }
        return false;
    }

    public Guild get(String guildName) {
        return guildMap.get(guildName);
    }

    public Guild getIgnoreCase(String guildName) {
        for (Guild guild : guildMap.values()) {
            if (guild.getName().equalsIgnoreCase(guildName) || (guild.getTag().equalsIgnoreCase(guildName))) {
                return guild;
            }
        }
        return null;
    }

    public void wipe() {
        guildMap.values().forEach(this::disbandGuild);
    }
}