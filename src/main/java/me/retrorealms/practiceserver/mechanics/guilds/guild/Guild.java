package me.retrorealms.practiceserver.mechanics.guilds.guild;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.guilds.GuildMechanics;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayer;
import me.retrorealms.practiceserver.mechanics.guilds.player.GuildPlayers;
import me.retrorealms.practiceserver.utils.SQLUtil.SQLMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Guild {
    private String name;
    private String tag;
    private UUID owner;
    private String motd;
    private Map<UUID, Role> playerRoleMap;
    private List<UUID> onlineList;

    public Guild(String name) {
        this.name = name;
        this.onlineList = new ArrayList<>();
        this.playerRoleMap = new ConcurrentHashMap<>();
        if(PracticeServer.DATABASE) return;
        File file = new File(GuildMechanics.getInstance().getDataFolder() + "/guild/", name + ".yml");
        if (file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        this.tag = configuration.getString("tag");
        this.motd = configuration.getString("motd");
        this.owner = UUID.fromString(configuration.getString("owner"));
        for (String member : configuration.getStringList("members")) {
            UUID uuid = UUID.fromString(member.split(":")[0].trim());
            Role role = Role.valueOf(member.split(":")[1].trim());
            this.playerRoleMap.put(uuid, role);
        }
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public Guild(String name, String tag, UUID owner) {
        this.name = name;
        this.onlineList = new ArrayList<>();
        this.tag = tag;
        this.owner = owner;
        this.motd = "Default MOTD";
        this.playerRoleMap = new ConcurrentHashMap<>();
        this.playerRoleMap.put(owner, Role.LEADER);
        save();
    }

    public void removeOnline(UUID uuid) {
        onlineList.remove(uuid);
    }

    public void addOnline(UUID uuid) {
        onlineList.add(uuid);
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Map<UUID, Role> getPlayerRoleMap() {
        return playerRoleMap;
    }

    public void save() {
        if(PracticeServer.DATABASE){
            SQLMain.saveGuild(this);
            return;
        }
        File file = new File(GuildMechanics.getInstance().getDataFolder() + "/guild/", name + ".yml");
        if (file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        configuration.set("name", name);
        configuration.set("motd", motd);
        configuration.set("tag", tag);
        configuration.set("owner", owner.toString());
        List<String> memberStringList = new ArrayList<>();
        this.playerRoleMap.forEach((uuid, role) -> {
            memberStringList.add(uuid.toString() + ":" + role.toString());
        });
        configuration.set("members", memberStringList);
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerString(int finalIndex) {
        if (onlineList.size() < finalIndex) {
            return "";
        }
        if (finalIndex >= onlineList.size()) {
            return "";
        }
        UUID uuid = getOnlineList().get(finalIndex);
        if (uuid != null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && (player.isOnline())) {
                String prefix = "";
                Role role = playerRoleMap.get(uuid);
                if (role == Role.LEADER) {
                    prefix = ChatColor.DARK_AQUA + "♛ " + ChatColor.GRAY;
                } else if (role == Role.OFFICER) {
                    prefix = ChatColor.DARK_AQUA + "* " + ChatColor.GRAY;
                }
                return ChatColor.GREEN + " ⦿ " + prefix + ChatColor.GRAY + player.getName();
            }
        }
        return "";
    }

    public List<UUID> getOnlineList() {
        return onlineList;
    }

    public String getPlayerString(UUID uuid) {
        return getPlayerString(onlineList.indexOf(uuid));
    }

    public void removePlayer(Player player) {
        if (onlineList.contains(player.getUniqueId())) {
            onlineList.remove(player.getUniqueId());
        }
        GuildPlayer guildPlayer = GuildPlayers.getInstance().get(player.getUniqueId());
        Guild guild = GuildManager.getInstance().get(guildPlayer.getGuildName());
        guildPlayer.setGuildName("");
        playerRoleMap.remove(player.getUniqueId());
        guild.save();
        SQLMain.updateGuild(guildPlayer.getUuid(), guildPlayer.getGuildName());
    }

    public void sendMessage(String message) {
        for (UUID uuid : getOnlineList()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && (player.isOnline())) {
                player.sendMessage(message);
            }
        }
    }

    public Role getRole(UUID uuid) {
        return playerRoleMap.get(uuid);
    }

    public void addPlayer(Player player) {
        this.playerRoleMap.put(player.getUniqueId(), Role.MEMBER);
        sendMessage(ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + tag + ChatColor.DARK_AQUA + "> " + ChatColor.DARK_AQUA.toString() + player.getName() + ChatColor.GRAY.toString() + " has " + ChatColor.UNDERLINE + "joined" + ChatColor.GRAY + " your guild.");
        player.sendMessage(ChatColor.DARK_AQUA + "You have joined '" + ChatColor.BOLD + getName() + "'" + ChatColor.DARK_AQUA + ".");
        player.sendMessage(ChatColor.GRAY + "To chat with your new guild, use " + ChatColor.BOLD + "/g" + ChatColor.GRAY + " OR " + ChatColor.BOLD + " /g <message>");
        save();
        GuildPlayer guildPlayer = GuildPlayers.guildPlayerMap.get(player.getUniqueId());
        SQLMain.updateGuild(guildPlayer.getUuid(), guildPlayer.getGuildName());

    }

    public void setRole(UUID uuid, Role role) {
        if (playerRoleMap.containsKey(uuid)) {
            playerRoleMap.remove(uuid);
        }
        playerRoleMap.put(uuid, role);
    }
}
